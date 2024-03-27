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

package io.temporal.samples.moneytransfer.brief;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.AccountTransferActivities;
import io.temporal.samples.moneytransfer.AccountTransferActivitiesImpl;
import io.temporal.samples.moneytransfer.dataclasses.*;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class AccountTransferWorkflowImplBrief implements AccountTransferWorkflowBrief {

  // some state variables we'll modify as the workflow progresses
  private String transferState = "starting";
  private String workflowStatus = "running";
  private int progressPercentage = 10;
  private int approvalTime = 30;

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

  // workflow response object
  private ChargeResponseObj chargeResult = new ChargeResponseObj("");

  private boolean approved = false;

  // workflow
  @Override
  public ResultObj transfer(WorkflowParameterObj params) {

    // The validate activity will return false if approval is required
    if (!accountTransferActivities.validate(params.getScenario())) {

      // Wait for the approval signal for up to approvalTime
      boolean receivedSignal = Workflow.await(Duration.ofSeconds(30), () -> approved);

      // If the signal was not received within the timeout, fail the workflow
      if (!receivedSignal) {
        throw ApplicationFailure.newFailure(
            "Approval not received within 30 seconds", "ApprovalTimeout");
      }
    }

    // withdraw activity
    accountTransferActivities.withdraw(params.getAmount(), params.getScenario());

    try {
      String idempotencyKey = Workflow.randomUUID().toString();
      // deposit activity
      chargeResult =
          accountTransferActivities.deposit(
              idempotencyKey, params.getAmount(), params.getScenario());
    } catch (ActivityFailure e) {
      // if deposit() fails in an unrecoverable way, rollback the withdrawal
      // and fail the workflow
      // undoWithdraw activity
      accountTransferActivities.undoWithdraw(params.getAmount());

      // return failure message
      String message = ((ApplicationFailure) e.getCause()).getOriginalMessage();
      throw ApplicationFailure.newNonRetryableFailure(message, "DepositFailed");
    }

    return new ResultObj(chargeResult);
  }

  @Override // query
  public StateObj getStateQuery() {
    StateObj stateObj =
        new StateObj(progressPercentage, transferState, workflowStatus, chargeResult, approvalTime);
    return stateObj;
  }

  @Override // signal
  public void approveTransfer() {
    this.approved = true;
  }
}
