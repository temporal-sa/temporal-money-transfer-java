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
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

public class AccountTransferWorkflowImpl implements AccountTransferWorkflow {

  public static final Logger log = Workflow.getLogger(AccountTransferWorkflowImpl.class);
  private final RetryOptions retryOptionsSingleRetry =
      RetryOptions.newBuilder()
          .setInitialInterval(Duration.ofSeconds(5))
          .setMaximumInterval(Duration.ofSeconds(100))
          .setBackoffCoefficient(2)
          .setMaximumAttempts(1)
          .build();
  private final ActivityOptions optionsSingleRetry =
      ActivityOptions.newBuilder()
          .setRetryOptions(retryOptionsSingleRetry)
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .build();

  // these options are for retrying activities
  private final RetryOptions retryOptionsMultipleRetries =
      RetryOptions.newBuilder()
          .setInitialInterval(Duration.ofSeconds(3))
          .setMaximumInterval(Duration.ofSeconds(100))
          .setBackoffCoefficient(2)
          .setMaximumAttempts(5)
          .build();

  private final ActivityOptions optionsMultipleRetries =
      ActivityOptions.newBuilder()
          .setRetryOptions(retryOptionsMultipleRetries)
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .build();

  // uncomment me for the default workflow (only trying services once)
  private final TransferService transferService =
      Workflow.newActivityStub(TransferService.class, optionsSingleRetry);

  // uncomment me for the workflow that makes multiple withdraw activity attempts
  private final TransferService transferServiceMultipleRetries =
      Workflow.newActivityStub(TransferService.class, optionsMultipleRetries);

  private static void printAccountBalances(String header, Account fromAccount, Account toAccount) {
    log.info(header);
    log.info("From Account: " + fromAccount.toString());
    log.info("To Account: " + toAccount.toString());
  }

  @Override
  public String transfer(
      Account fromAccount,
      Account toAccount,
      String referenceId,
      int amountDollars,
      boolean simulateDepositRetries) {

    // use these to slow down the workflow for demos
    Duration shortTimer = Duration.ofSeconds(7);
    Duration longTimer = Duration.ofSeconds(12);

    List<String> compensations = new ArrayList<>();
    try {

      log.info(
          "Transfer workflow STARTED ($"
              + amountDollars
              + " from "
              + fromAccount.getAccountId()
              + " to "
              + toAccount.getAccountId()
              + "["
              + referenceId
              + "])");

      // print starting balance
      Workflow.sleep(shortTimer); // simulated delay
      printAccountBalances("Starting Balance", fromAccount, toAccount);

      Workflow.sleep(shortTimer); // simulated delay

      // withdraw from fromAccount
      log.info(
          "Withdrawing $" + amountDollars + " from account " + fromAccount + " (please wait..)");

      Workflow.sleep(shortTimer); // simulated delay

      fromAccount = transferService.withdraw(fromAccount, referenceId, amountDollars);
      compensations.add("undo_withdraw");

      Workflow.sleep(longTimer); // simulated delay
      printAccountBalances("Withdrawal Done", fromAccount, toAccount);
      Workflow.sleep(longTimer); // simulated delay

      // deposit to toAccount
      log.info("Depositing $" + amountDollars + " to account " + fromAccount + " (please wait)");
      Workflow.sleep(shortTimer); // simulated delay

      // this code path is for the workflow that makes multiple withdraw activity attempts
      if (simulateDepositRetries) {
        toAccount =
            transferServiceMultipleRetries.deposit(
                toAccount, referenceId, amountDollars, simulateDepositRetries);
      } else {
        toAccount =
            transferService.deposit(toAccount, referenceId, amountDollars, simulateDepositRetries);
      }
      compensations.add("undo_deposit");

      Workflow.sleep(longTimer); // simulated delay
      printAccountBalances("Deposit Done", fromAccount, toAccount);

    } catch (ActivityFailure e) {
      for (int i = compensations.size() - 1; i >= 0; i--) {
        Workflow.sleep(shortTimer); // simulated delay
        String compensation = compensations.get(i);
        if ("undo_deposit".equals(compensation)) {
          log.info(
              "Undoing deposit to account " + toAccount.getAccountId() + " (check API response)");
          Workflow.sleep(longTimer); // simulated delay

          toAccount = transferService.undoDeposit(toAccount, referenceId, amountDollars);

          Workflow.sleep(shortTimer); // simulated delay

        } else if ("undo_withdraw".equals(compensation)) {
          log.info(
              "Undoing withdrawal from account "
                  + fromAccount.getAccountId()
                  + " (check API response)");
          Workflow.sleep(longTimer); // simulated delay

          fromAccount = transferService.undoWithdraw(fromAccount, referenceId, amountDollars);

          Workflow.sleep(shortTimer); // simulated delay
        }
      }
      Workflow.sleep(shortTimer); // simulated delay

      printAccountBalances(
          "Transfer Rollback Complete. Final Account Balances", fromAccount, toAccount);

      log.info("Workflow FAILED, check logs.");
      return "FAIL";
    }
    Workflow.sleep(shortTimer); // simulated delay

    log.info(
        "Transferred $"
            + amountDollars
            + " from "
            + fromAccount.getAccountId()
            + " to "
            + toAccount.getAccountId()
            + " ["
            + referenceId
            + "])");
    printAccountBalances("Final Account Balances", fromAccount, toAccount);

    log.info("Workflow finished successfully.");

    return "SUCCESS";
  }
}
