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

import static io.temporal.samples.moneytransfer.AccountActivityWorker.TASK_QUEUE;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class TransferRequester {

  @SuppressWarnings("CatchAndPrintStackTrace")
  public static void main(String[] args) {

    // generate a random reference number
    String referenceNumber = generateReferenceNumber(); // random reference number
    int amountDollars = 620; // amount to transfer

    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    // client that can be used to start and signal workflows
    WorkflowClient workflowClient = WorkflowClient.newInstance(service);

    // now we can start running instances of the saga - its state will be persisted
    WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setWorkflowId(referenceNumber)
            .setTaskQueue(TASK_QUEUE)
            .build();
    AccountTransferWorkflow transferWorkflow =
        workflowClient.newWorkflowStub(AccountTransferWorkflow.class, options);

    String fromAccountId = "acct1";
    Account fromAccount = new Account(fromAccountId, 1000);
    // Account fromAccount = new Account(fromAccountId, 1000); // for invalid balance

    String toAccountId = "acct2";
    // Account toAccount = new Account(toAccountId, 290);

    // UNCOMMENT THIS LINE TO TEST INVALID ACCOUNT
    Account toAccount = new Account("acct2invalid", 290); // for invalid account

    WorkflowClient.start(
        transferWorkflow::transfer, fromAccount, toAccount, referenceNumber, amountDollars);
    System.out.printf(
        "\n\nTransfer of $%d from %s to %s requested",
        amountDollars, fromAccount.getAccountId(), toAccount.getAccountId());
    System.exit(0);
  }

  private static String generateReferenceNumber() {
    return String.format(
        "REF-%s-%03d",
        (char) (Math.random() * 26 + 'A')
            + ""
            + (char) (Math.random() * 26 + 'A')
            + ""
            + (char) (Math.random() * 26 + 'A'),
        (int) (Math.random() * 999));
  }
}
