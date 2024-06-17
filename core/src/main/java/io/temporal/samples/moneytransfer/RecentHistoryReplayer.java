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

import io.temporal.api.workflowservice.v1.*;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.WorkflowExecutionHistory;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.samples.moneytransfer.dataconverter.CryptCodec;
import io.temporal.samples.moneytransfer.web.ServerInfo;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.testing.TestEnvironmentOptions;
import io.temporal.testing.TestWorkflowEnvironmentInternal;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.worker.Worker;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;

public class RecentHistoryReplayer {

  public static List<WorkflowExecutionHistory> getWorkflowHistories()
      throws FileNotFoundException, SSLException {

    WorkflowServiceStubs service = getWorkflowServiceStubs();

    String query = "WorkflowType = 'moneyTransferWorkflow'";

    ListWorkflowExecutionsRequest listWorkflowExecutionRequest =
        ListWorkflowExecutionsRequest.newBuilder()
            .setNamespace(ServerInfo.getNamespace())
            .setPageSize(5)
            .setQuery(query)
            .build();
    ListWorkflowExecutionsResponse listWorkflowExecutionsResponse =
        service.blockingStub().listWorkflowExecutions(listWorkflowExecutionRequest);

    List<WorkflowExecutionHistory> histories =
        listWorkflowExecutionsResponse.getExecutionsList().stream()
            .map(
                (info) -> {
                  GetWorkflowExecutionHistoryResponse weh =
                      service
                          .blockingStub()
                          .getWorkflowExecutionHistory(
                              GetWorkflowExecutionHistoryRequest.newBuilder()
                                  .setNamespace(ServerInfo.getNamespace())
                                  .setExecution(info.getExecution())
                                  .build());
                  return new WorkflowExecutionHistory(
                      weh.getHistory(), info.getExecution().getWorkflowId());
                })
            .collect(Collectors.toList());

    return histories;
  }

  public static void main(String[] args) throws Exception {

    List<WorkflowExecutionHistory> histories = getWorkflowHistories();

    // Make replayer compatible with data converter
    TestWorkflowEnvironmentInternal testEnv =
        new TestWorkflowEnvironmentInternal(
            TestEnvironmentOptions.newBuilder()
                .setWorkflowClientOptions(
                    WorkflowClientOptions.newBuilder()
                        .setDataConverter(
                            new CodecDataConverter(
                                DefaultDataConverter.newDefaultInstance(),
                                Collections.singletonList(new CryptCodec()),
                                true /* encode failure attributes */))
                        .build())
                .build());

    Worker worker = testEnv.newWorker("my-task-queue");
    worker.registerWorkflowImplementationTypes(AccountTransferWorkflowImpl.class);

    // history length
    System.out.println("Replaying " + histories.size() + " most recent workflow executions.");

    for (WorkflowExecutionHistory history : histories) {
      System.out.println("Replaying workflow: " + history.getWorkflowExecution().getWorkflowId());

      try {
        WorkflowReplayer.replayWorkflowExecution(history, worker);
        System.out.println("Replay completed successfully");
      } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println(
            "Replay failed, check above output for io.temporal.worker.NonDeterministicException");
      }
    }

    testEnv.close();
  }
}
