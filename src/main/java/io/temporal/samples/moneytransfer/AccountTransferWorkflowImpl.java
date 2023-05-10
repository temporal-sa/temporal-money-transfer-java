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

public class AccountTransferWorkflowImpl implements AccountTransferWorkflow {

  private final RetryOptions retryOptions =
      RetryOptions.newBuilder()
          .setInitialInterval(Duration.ofSeconds(3))
          .setMaximumInterval(Duration.ofSeconds(100))
          .setBackoffCoefficient(1)
          .setMaximumAttempts(3)
          .build();
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
  private final TransferService transferService =
      Workflow.newActivityStub(TransferService.class, optionsSingleRetry);

  @Override
  public String transfer(
      Account fromAccount, Account toAccount, String referenceId, int amountDollars) {

    // use these to slow down the workflow for demos
    Duration shortTimer = Duration.ofSeconds(3);
    Duration longTimer = Duration.ofSeconds(6);

    List<String> compensations = new ArrayList<>();
    try {

      // make it easier to see a new transfer by printing some ***
      System.out.println(String.format("%0" + 12 + "d", 0).replace("0", "*"));
      System.out.println(String.format("%0" + 12 + "d", 0).replace("0", "*"));
      System.out.println(String.format("%0" + 12 + "d", 0).replace("0", "*"));

      System.out.printf(
          "\n\nTransfer workflow STARTED ($%d from %s to %s REF=%s)\n",
          amountDollars, fromAccount.getAccountId(), toAccount.getAccountId(), referenceId);

      // print starting balance
      Workflow.sleep(shortTimer);
      System.out.println("\n\nStarting Balance");
      System.out.println(fromAccount.toString());
      System.out.println(toAccount.toString());
      Workflow.sleep(shortTimer);

      // withdraw from fromAccount
      System.out.printf(
          "\n\nWithdrawing $%d from account %s (please wait..)\n\n",
          amountDollars, fromAccount.getAccountId());
      Workflow.sleep(shortTimer); // simulated delay
      fromAccount = transferService.withdraw(fromAccount, referenceId, amountDollars);
      compensations.add("undo_withdraw");
      Workflow.sleep(longTimer); // simulated delay

      System.out.println("Withdrawal Done. Balances:");
      System.out.println(fromAccount.toString());
      System.out.println(toAccount.toString());

      Workflow.sleep(longTimer); // simulated delay

      // deposit to toAccount
      System.out.printf(
          "\n\nDepositing $%d to account %s (please wait)\n\n",
          amountDollars, toAccount.getAccountId());
      Workflow.sleep(shortTimer); // simulated delay
      toAccount = transferService.deposit(toAccount, referenceId, amountDollars);
      compensations.add("undo_deposit");
      Workflow.sleep(longTimer); // simulated delay

      System.out.println("Deposit Done. Balances:");
      System.out.println(fromAccount.toString());
      System.out.println(toAccount.toString());

    } catch (ActivityFailure e) {
      for (int i = compensations.size() - 1; i >= 0; i--) {
        Workflow.sleep(shortTimer); // simulated delay
        String compensation = compensations.get(i);
        if ("undo_deposit".equals(compensation)) {
          System.out.printf(
              "\n\nUndoing deposit to account %s (check API response)\n\n",
              toAccount.getAccountId());
          Workflow.sleep(longTimer);
          toAccount = transferService.undoDeposit(toAccount, referenceId, amountDollars);
          Workflow.sleep(shortTimer);

        } else if ("undo_withdraw".equals(compensation)) {
          System.out.printf(
              "\n\nUndoing withdrawal from account %s (check API response)\n\n",
              fromAccount.getAccountId());
          Workflow.sleep(longTimer);
          fromAccount = transferService.undoWithdraw(fromAccount, referenceId, amountDollars);
          Workflow.sleep(shortTimer);
        }
      }
      Workflow.sleep(shortTimer); // simulated delay

      System.out.println("Transfer Rollback Complete. Final Account Balances");
      System.out.println(fromAccount.toString());
      System.out.println(toAccount.toString());
      System.out.println("Workflow FAILED, check logs.");
      return "FAIL";
    }

    // print starting balance
    System.out.println("\nFinal Account Balances");
    System.out.println(fromAccount.toString());
    System.out.println(toAccount.toString());
    Workflow.sleep(shortTimer); // simulated delay

    System.out.println("Workflow finished successfully.");

    return "SUCCESS";
  }
}
