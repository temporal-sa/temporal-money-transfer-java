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

import static io.temporal.samples.moneytransfer.TemporalClient.getWorkflowServiceStubs;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.api.workflowservice.v1.WorkflowServiceGrpc;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.samples.moneytransfer.dataclasses.ExecutionScenarioObj;
import io.temporal.samples.moneytransfer.dataclasses.ResultObj;
import io.temporal.samples.moneytransfer.dataclasses.StateObj;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowParameterObj;
import io.temporal.samples.moneytransfer.web.ServerInfo;
import io.temporal.serviceclient.WorkflowServiceStubs;
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

  public static StateObj runQuery(String workflowId) throws FileNotFoundException, SSLException {

    WorkflowClient client = TemporalClient.get();

    // print workflow ID
    System.out.println("Workflow STATUS: " + getWorkflowStatus(workflowId));

    WorkflowStub workflowStub = client.newUntypedWorkflowStub(workflowId);

    StateObj result = workflowStub.query("transferStatus", StateObj.class);

    if ("WORKFLOW_EXECUTION_STATUS_FAILED".equals(getWorkflowStatus(workflowId))) {
      result.setWorkflowStatus("FAILED");
    }

    return result;
  }

  public static void runApproveSignal(String workflowId) {

    try {
      WorkflowClient client = TemporalClient.get();

      WorkflowStub workflowStub = client.newUntypedWorkflowStub(workflowId);

      workflowStub.signal("approveTransfer");
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
  }

  public static String runWorkflow(WorkflowParameterObj workflowParameterObj)
      throws FileNotFoundException, SSLException {
    // generate a random reference number
    String referenceNumber = generateReferenceNumber(); // random reference number

    // Workflow execution code

    WorkflowClient client = TemporalClient.get();
    final String TASK_QUEUE = ServerInfo.getTaskqueue();

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

    WorkflowParameterObj params =
        new WorkflowParameterObj(amountCents, ExecutionScenarioObj.HAPPY_PATH);

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

  private static String getWorkflowStatus(String workflowId)
      throws FileNotFoundException, SSLException {
    WorkflowServiceStubs service = getWorkflowServiceStubs();
    WorkflowServiceGrpc.WorkflowServiceBlockingStub stub = service.blockingStub();
    DescribeWorkflowExecutionRequest request =
        DescribeWorkflowExecutionRequest.newBuilder()
            .setNamespace(ServerInfo.getNamespace())
            .setExecution(WorkflowExecution.newBuilder().setWorkflowId(workflowId))
            .build();
    DescribeWorkflowExecutionResponse response = stub.describeWorkflowExecution(request);
    return response.getWorkflowExecutionInfo().getStatus().name();
  }
}
