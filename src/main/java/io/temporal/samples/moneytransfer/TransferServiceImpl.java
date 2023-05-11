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

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferServiceImpl implements TransferService {
  private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

  @Override
  public Account withdraw(Account fromAccount, String referenceId, int amountDollars) {

    log.info("WITHDRAW CALLED");
    log.info(fromAccount.toString());

    log.info(
        "\n\n/API/withdraw from "
            + fromAccount.getAccountId()
            + " of $"
            + amountDollars
            + " requested. (Ref="
            + referenceId
            + ")\n");

    if (fromAccount.getBalance() < amountDollars) {
      throw new RuntimeException(
          "FAILURE - Insufficient Funds (simulated) for account: " + fromAccount.getAccountId());
    }

    // withdraw money from the account
    fromAccount.setBalance(fromAccount.getBalance() - amountDollars);

    return fromAccount;
  }

  @Override
  public Account deposit(
      Account toAccount, String referenceId, int amountDollars, boolean simulateRetryFailures) {

    log.info(
        "\n\n/API/deposit to "
            + toAccount.getAccountId()
            + " of $"
            + amountDollars
            + " requested. (Ref="
            + referenceId
            + ")\n");

    if (simulateRetryFailures) {
      ActivityExecutionContext ctx = Activity.getExecutionContext();
      ActivityInfo info = ctx.getInfo();
      log.info("\n*** RETRY ATTEMPT: " + info.getAttempt() + "***\n");

      if (info.getAttempt() < 5) {
        throw new RuntimeException(
            "FAILURE - Simulated failure for account: " + toAccount.getAccountId());
      }

      log.info("*** RETRY SUCCESSFUL ***\n");
    }

    if ("acct2invalid".equals(toAccount.getAccountId())) {
      throw new RuntimeException(
          "FAILURE - Invalid Account ID (simulated) for account: " + toAccount.getAccountId());
    }

    toAccount.setBalance(toAccount.getBalance() + amountDollars);

    return toAccount;
  }

  // Implement compensation methods
  @Override
  public Account undoWithdraw(Account fromAccount, String referenceId, int amountDollars) {

    log.info(
        "\n\nUndoing withdrawal of "
            + amountDollars
            + " into account "
            + fromAccount.getAccountId()
            + ". (Ref="
            + referenceId
            + ")");

    // undo withdrawal

    fromAccount.setBalance(fromAccount.getBalance() + amountDollars);

    return fromAccount;
  }

  @Override
  public Account undoDeposit(Account toAccount, String referenceId, int amountDollars) {
    log.info(
        "\n\nUndoing deposit of "
            + amountDollars
            + " into account "
            + toAccount.getAccountId()
            + ". (Ref="
            + referenceId
            + ")");

    // undo deposit
    toAccount.setBalance(toAccount.getBalance() - amountDollars);

    return toAccount;
  }
}
