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
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowParameterObj;
import io.temporal.samples.moneytransfer.dataconverter.CryptCodec;
import java.util.Collections;

public class TransferRequester {

  @SuppressWarnings("CatchAndPrintStackTrace")
  public static void main(String[] args) throws Exception {

    // generate a random reference number
    String referenceNumber = generateReferenceNumber(); // random reference number
    int amountCents = 45; // amount to transfer

    String fromAccountId = "acct1";
    Account fromAccount = new Account(fromAccountId, 1000);
    // Account fromAccount = new Account(fromAccountId, 1000); // for invalid balance

    String toAccountId = "acct2";
    // UNCOMMENT THIS LINE TO TEST A ***VALID*** ACCOUNT (path #1 - happy)
    // Account toAccount = new Account(toAccountId, 350);

    // set to true to simulate deposit retries (path #2 - retries)
    boolean simulateDepositRetries = false;

    // UNCOMMENT THIS LINE TO TEST AN ***INVALID*** ACCOUNT
    Account toAccount = new Account("acct2invalid", 290); // for invalid account (path #3 -
    // rollback)

    // path #4: Simulate a failure in the middle of the transfer
    // Wait for "Withdrawal done" in the console, then kill the worker
    // Look for 'WorkflowTaskTimedOut' event in the history

    // Workflow execution code

    WorkflowClientOptions.Builder builder = WorkflowClientOptions.newBuilder();

    // if environment variable ENCRYPT_PAYLOADS is set to true, then use CryptCodec
    if (System.getenv("ENCRYPT_PAYLOADS") != null
        && System.getenv("ENCRYPT_PAYLOADS").equals("true")) {
      builder.setDataConverter(
          new CodecDataConverter(
              DefaultDataConverter.newDefaultInstance(),
              Collections.singletonList(new CryptCodec()),
              true /* encode failure attributes */));
    }

    WorkflowClient client = TemporalClient.get();

    WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setWorkflowId(referenceNumber)
            .setTaskQueue(TASK_QUEUE)
            .build();
    AccountTransferWorkflow transferWorkflow =
        client.newWorkflowStub(AccountTransferWorkflow.class, options);

    WorkflowParameterObj params = new WorkflowParameterObj(amountCents);

    WorkflowClient.start(transferWorkflow::transfer, params);
    System.out.printf("\n\nTransfer of $%d requested\n", amountCents);
    System.exit(0);
  }

  private static String generateReferenceNumber() {
    return String.format(
        "TRANSFER-%s-%03d",
        (char) (Math.random() * 26 + 'A')
            + ""
            + (char) (Math.random() * 26 + 'A')
            + ""
            + (char) (Math.random() * 26 + 'A'),
        (int) (Math.random() * 999));
  }
}
