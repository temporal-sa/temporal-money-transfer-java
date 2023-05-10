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

public class TransferServiceImpl implements TransferService {

  @Override
  public Account withdraw(Account fromAccount, String referenceId, int amountDollars) {
    System.out.printf(
        "\n\n/API/withdraw from %s of $%d requested. (Ref=%s)\n",
        fromAccount.getAccountId(), amountDollars, referenceId);

    if (fromAccount.getBalance() < amountDollars) {
      throw new RuntimeException(
          "FAILURE - Insufficient Funds (simulated) for account: " + fromAccount.getAccountId());
    } else {
      // withdraw money from the account
      fromAccount.setBalance(fromAccount.getBalance() - amountDollars);
    }

    return fromAccount;
  }

  @Override
  public Account deposit(Account toAccount, String referenceId, int amountDollars) {
    System.out.printf(
        "\n\n/API/deposit to %s of $%d requested. (Ref=%s)\n",
        toAccount.getAccountId(), amountDollars, referenceId);

    if ("acct2invalid".equals(toAccount.getAccountId())) {
      throw new RuntimeException(
          "FAILURE - Invalid Account ID (simulated) for account: " + toAccount.getAccountId());
    } else {
      // deposit money to the account
      toAccount.setBalance(toAccount.getBalance() + amountDollars);
    }

    return toAccount;
  }

  // Implement compensation methods
  @Override
  public Account undoWithdraw(Account fromAccount, String referenceId, int amountDollars) {
    System.out.printf(
        "\n\nMarking withdrawal of $%d from account %s as undone. (Ref=%s)\n",
        amountDollars, fromAccount.getAccountId(), referenceId);

    return fromAccount;
  }

  @Override
  public Account undoDeposit(Account toAccount, String referenceId, int amountDollars) {
    System.out.printf(
        "\n\nUndoing deposit of $%d into account %s. (Ref=%s)\n",
        amountDollars, toAccount.getAccountId(), referenceId);

    // undo deposit
    toAccount.setBalance(toAccount.getBalance() - amountDollars);

    return toAccount;
  }
}
