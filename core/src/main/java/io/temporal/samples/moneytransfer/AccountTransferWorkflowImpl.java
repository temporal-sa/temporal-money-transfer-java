/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.temporal.samples.moneytransfer;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.dataclasses.*;
import io.temporal.samples.moneytransfer.web.ServerInfo;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountTransferWorkflowImpl implements AccountTransferWorkflow {

  static final SearchAttributeKey<String> WORKFLOW_STEP = SearchAttributeKey.forKeyword("Step");

  private static final Logger log = LoggerFactory.getLogger(AccountTransferWorkflowImpl.class);

  // activity retry policy
  private final ActivityOptions options =
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .setRetryOptions(
              RetryOptions.newBuilder()
                  .setDoNotRetry(
                      AccountTransferActivitiesImpl.InvalidAccountException.class.getName())
                  .build())
          .build();

  // activity stub
  private final AccountTransferActivities accountTransferActivities =
      Workflow.newActivityStub(AccountTransferActivities.class, options);

  // these variables are reflected in the UI
  private int progressPercentage = 10;
  private String transferState = "starting";

  // workflow response object
  private ChargeResponseObj chargeResult = new ChargeResponseObj("");

  // time to allow for transfer approval
  private int approvalTime = 30;

  private boolean approved = false;

  // workflow
  @Override
  public ResultObj transfer(WorkflowParameterObj params) {

    transferState = "starting";
    progressPercentage = 25;

    Workflow.sleep(Duration.ofSeconds(ServerInfo.getWorkflowSleepDuration()));

    progressPercentage = 50;
    transferState = "running";

    // The validate activity will return false if approval is required
    if (!accountTransferActivities.validate(params.getScenario())) {
      log.info(
          "\n\nWaiting on 'approveTransfer' Signal or Update for workflow ID: "
              + Workflow.getInfo().getWorkflowId()
              + "\n\n");
      transferState = "waiting";

      // Wait for the approval signal for up to approvalTime
      boolean receivedSignal = Workflow.await(Duration.ofSeconds(approvalTime), () -> approved);

      // If the signal was not received within the timeout, fail the workflow
      if (!receivedSignal) {
        log.error(
            "Approval not received within the "
                + approvalTime
                + "-second time window: "
                + "Failing the workflow.");
        throw ApplicationFailure.newFailure(
            "Approval not received within " + approvalTime + " seconds", "ApprovalTimeout");
      }
    }

    // these variables are reflected in the UI
    progressPercentage = 60;
    transferState = "running";

    // withdraw activity
    if (params.getScenario() == ExecutionScenarioObj.ADVANCED_VISIBILITY) {
      Workflow.upsertTypedSearchAttributes(WORKFLOW_STEP.valueSet("Withdraw"));
      Workflow.sleep(Duration.ofSeconds(5)); // for dramatic effect
    }

    accountTransferActivities.withdraw(params.getAmount(), params.getScenario());
    Workflow.sleep(Duration.ofSeconds(2)); // for dramatic effect

    // Simulate bug in workflow
    if (params.getScenario() == ExecutionScenarioObj.BUG_IN_WORKFLOW) {
      // throw an error to simulate a bug in the workflow
      // uncomment the following line and restart workers to 'fix' the bug
      log.info("\n\nSimulating workflow task failure.\n\n");
      throw new RuntimeException("Workflow Bug!");
    }

    if (params.getScenario() == ExecutionScenarioObj.ADVANCED_VISIBILITY) {
      Workflow.upsertTypedSearchAttributes(WORKFLOW_STEP.valueSet("Deposit"));
    }

    try {
      String idempotencyKey = Workflow.randomUUID().toString();
      // deposit activity
      chargeResult =
          accountTransferActivities.deposit(
              idempotencyKey, params.getAmount(), params.getScenario());
    }
    // if deposit() fails in an unrecoverable way, rollback the withdrawal and fail the workflow
    catch (ActivityFailure e) {
      log.info("\n\nDeposit failed unrecoverably, reverting withdraw\n\n");

      // undoWithdraw activity (rollback)
      accountTransferActivities.undoWithdraw(params.getAmount());

      // return failure message
      String message = ((ApplicationFailure) e.getCause()).getOriginalMessage();
      throw ApplicationFailure.newNonRetryableFailure(message, "DepositFailed");
    }

    // these variables are reflected in the UI
    progressPercentage = 80;
    Workflow.sleep(Duration.ofSeconds(6));
    progressPercentage = 100;
    transferState = "finished";

    return new ResultObj(chargeResult);
  }

  @Override
  public StateObj getStateQuery() {
    StateObj stateObj =
        new StateObj(progressPercentage, transferState, "", chargeResult, approvalTime);
    return stateObj;
  }

  @Override
  public void approveTransfer() {
    log.info("\n\nApprove Signal Received\n\n");

    if (this.transferState.equals("waiting")) {
      this.approved = true;
    } else {
      log.info("\n\nSignal not applied: Transfer is not waiting for approval.\n\n");
    }
  }

  @Override
  public String approveTransferUpdate() {
    log.info("\n\nApprove Update Validated: Approving Transfer\n\n");
    this.approved = true;
    return "successfully approved transfer";
  }

  @Override
  public void approveTransferUpdateValidator() {
    log.info("\n\nApprove Update Received: Validating\n\n");
    if (this.approved) {
      throw new IllegalStateException("Validation Failed: Transfer already approved");
    }
    if (!transferState.equals("waiting")) {
      throw new IllegalStateException("Validation Failed: Transfer doesn't require approval");
    }
  }
}
