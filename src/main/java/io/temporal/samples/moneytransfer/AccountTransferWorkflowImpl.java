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
          .setInitialInterval(Duration.ofSeconds(1))
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
      Account fromAccount, Account toAccount, String referenceId, int amountCents) {
    List<String> compensations = new ArrayList<>();
    try {
      compensations.add("undo_withdraw");
      transferService.withdraw(fromAccount, referenceId, amountCents);

      compensations.add("undo_deposit");
      transferService.deposit(toAccount, referenceId, amountCents);
    } catch (ActivityFailure e) {
      for (int i = compensations.size() - 1; i >= 0; i--) {
        String compensation = compensations.get(i);
        if ("undo_deposit".equals(compensation)) {
          transferService.undoDeposit(toAccount, referenceId, amountCents);
        } else if ("undo_withdraw".equals(compensation)) {
          transferService.undoWithdraw(fromAccount, referenceId, amountCents);
        }
      }
    }
  }
}
