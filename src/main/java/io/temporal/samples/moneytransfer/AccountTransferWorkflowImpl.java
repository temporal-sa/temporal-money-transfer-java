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

  private final RetryOptions retryoptions =
      RetryOptions.newBuilder()
          .setInitialInterval(Duration.ofSeconds(5))
          .setMaximumInterval(Duration.ofSeconds(100))
          .setBackoffCoefficient(2)
          .setMaximumAttempts(3)
          .build();
  private final ActivityOptions options =
      ActivityOptions.newBuilder()
          .setRetryOptions(retryoptions)
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .build();
  private final TransferService transferService =
      Workflow.newActivityStub(TransferService.class, options);

  @Override
  public void transfer(
      Account fromAccount, Account toAccount, String referenceId, int amountDollars) {
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
      System.out.println("\n\nStarting Balance");
      System.out.println(fromAccount.toString());
      System.out.println(toAccount.toString());
      Workflow.sleep(Duration.ofSeconds(5));

      // withdraw from fromAccount
      System.out.printf(
          "\n\nWithdrawing %d from account %s (please wait..)\n\n",
          amountDollars, fromAccount.getAccountId());
      Workflow.sleep(Duration.ofSeconds(5)); // simulated delay
      compensations.add("undo_withdraw");
      fromAccount = transferService.withdraw(fromAccount, referenceId, amountDollars);
      Workflow.sleep(Duration.ofSeconds(10)); // simulated delay

      // deposit to toAccount
      System.out.printf(
          "\n\nDepositing %d to account %s (please wait)\n\n",
          amountDollars, fromAccount.getAccountId());
      Workflow.sleep(Duration.ofSeconds(5)); // simulated delay
      compensations.add("undo_deposit");
      toAccount = transferService.deposit(toAccount, referenceId, amountDollars);
      Workflow.sleep(Duration.ofSeconds(10)); // simulated delay

    } catch (ActivityFailure e) {
      for (int i = compensations.size() - 1; i >= 0; i--) {
        String compensation = compensations.get(i);
        if ("undo_deposit".equals(compensation)) {
          System.out.printf(
              "\n\nUndoing deposit to account %s (check API response)\n\n",
              toAccount.getAccountId());
          Workflow.sleep(Duration.ofSeconds(5));
          toAccount = transferService.undoDeposit(toAccount, referenceId, amountDollars);
        } else if ("undo_withdraw".equals(compensation)) {
          System.out.printf(
              "\n\nUndoing withdrawal from account %s (check API response)\n\n",
                  toAccount.getAccountId());
          Workflow.sleep(Duration.ofSeconds(5));
          fromAccount = transferService.undoWithdraw(fromAccount, referenceId, amountDollars);
        }
      }
    }

    // print starting balance
    System.out.println("\nFinal Balance");
    System.out.println(fromAccount.toString());
    System.out.println(toAccount.toString());
    Workflow.sleep(Duration.ofSeconds(3)); // simulated delay

    System.out.println("\nWorkflow FINISHED.");
  }
}
