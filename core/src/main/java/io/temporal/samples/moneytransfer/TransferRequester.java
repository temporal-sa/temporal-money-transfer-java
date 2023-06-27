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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.samples.moneytransfer.dataclasses.ResultObj;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowParameterObj;
import java.io.FileNotFoundException;
import javax.net.ssl.SSLException;

public class TransferRequester {

  public static ResultObj getWorkflowOutcome(String workflowId)
      throws FileNotFoundException, SSLException {

    WorkflowClient client = TemporalClient.get();

    WorkflowStub workflowStub = client.newUntypedWorkflowStub(workflowId);

    // Returns the result after waiting for the Workflow to complete.
    ResultObj result = workflowStub.getResult(ResultObj.class);
    return result;
  }

  public static String runQuery(String workflowId)
      throws FileNotFoundException, SSLException, JsonProcessingException {

    WorkflowClient client = TemporalClient.get();

    // print workflow ID
    System.out.println("Workflow ID runQuery helper: " + workflowId);

    WorkflowStub workflowStub = client.newUntypedWorkflowStub(workflowId);

    String result = workflowStub.query("AccountTransferWorkflowquery", String.class);

    return result;
  }

  public static String runWorkflow(WorkflowParameterObj workflowParameterObj)
      throws FileNotFoundException, SSLException {
    // generate a random reference number
    String referenceNumber = generateReferenceNumber(); // random reference number

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

    WorkflowClient client = TemporalClient.get();

    WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setWorkflowId(referenceNumber)
            .setTaskQueue(TASK_QUEUE)
            .build();
    AccountTransferWorkflow transferWorkflow =
        client.newWorkflowStub(AccountTransferWorkflow.class, options);

    WorkflowClient.start(transferWorkflow::transfer, workflowParameterObj);
    System.out.printf("\n\nTransfer of $%d requested\n", workflowParameterObj.getAmount());

    return referenceNumber;
  }

  @SuppressWarnings("CatchAndPrintStackTrace")
  public static void main(String[] args) throws Exception {

    int amountCents = 45; // amount to transfer

    WorkflowParameterObj params = new WorkflowParameterObj(amountCents);

    runWorkflow(params);

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
