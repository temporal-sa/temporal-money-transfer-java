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
  public void withdraw(Account fromAccount, String referenceId, int amountCents) {
    System.out.printf(
        "Withdraw to %s of %d cents requested. ReferenceId=%s\n",
        fromAccount.getAccountId(), amountCents, referenceId);

    if ("account1_FAIL".equals(fromAccount.getAccountId())) {
      throw new RuntimeException(
          "Simulated failure on withdrawal for account: " + fromAccount.getAccountId());
    }
  }

  @Override
  public void deposit(Account toAccount, String referenceId, int amountCents) {
    System.out.printf(
        "Deposit to %s of %d cents requested. ReferenceId=%s\n",
        toAccount.getAccountId(), amountCents, referenceId);

    if ("acct2invalid".equals(toAccount.getAccountId())) {
      throw new RuntimeException(
          "Simulated failure on deposit for account: " + toAccount.getAccountId());
    }

    //    throw new RuntimeException("simulated");
  }

  // Implement compensation methods
  @Override
  public void undoWithdraw(Account fromAccount, String referenceId, int amountCents) {
    System.out.printf(
        "\nUndoing withdrawal of $%d from account %s. ReferenceId: %s\n",
        amountCents, fromAccount.getAccountId(), referenceId);
  }

  @Override
  public void undoDeposit(Account toAccount, String referenceId, int amountCents) {
    System.out.printf(
        "\nUndoing deposit of $%d into account %s. ReferenceId: %s\n",
        amountCents, toAccount.getAccountId(), referenceId);
  }
}
