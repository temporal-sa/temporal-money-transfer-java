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

import com.google.common.base.Splitter;
import com.google.protobuf.Timestamp;
import io.temporal.api.filter.v1.StartTimeFilter;
import io.temporal.api.filter.v1.WorkflowTypeFilter;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.*;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowStatusObj;
import io.temporal.samples.moneytransfer.web.ServerInfo;
import io.temporal.serviceclient.WorkflowServiceStubs;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLException;

public class TransferLister {

  public static List<WorkflowStatusObj> listWorkflows() throws FileNotFoundException, SSLException {

    WorkflowServiceStubs service = getWorkflowServiceStubs();
    ListOpenWorkflowExecutionsResponse responseOpen =
        service
            .blockingStub()
            .listOpenWorkflowExecutions(
                ListOpenWorkflowExecutionsRequest.newBuilder()
                    .setStartTimeFilter(
                        StartTimeFilter.newBuilder().setEarliestTime(getOneHourAgo()).build())
                    .setTypeFilter(
                        WorkflowTypeFilter.newBuilder().setName("moneyTransferWorkflow").build())
                    .setNamespace(ServerInfo.getNamespace())
                    .build());

    ListClosedWorkflowExecutionsResponse responseClosed =
        service
            .blockingStub()
            .listClosedWorkflowExecutions(
                ListClosedWorkflowExecutionsRequest.newBuilder()
                    .setStartTimeFilter(
                        StartTimeFilter.newBuilder().setEarliestTime(getOneHourAgo()).build())
                    .setTypeFilter(
                        WorkflowTypeFilter.newBuilder().setName("moneyTransferWorkflow").build())
                    .setNamespace(ServerInfo.getNamespace())
                    .build());

    // array of WorkflowStatusObj
    List<WorkflowStatusObj> workflowStatusObjList = new ArrayList<>();

    for (WorkflowExecutionInfo workflowExecutionInfo : responseOpen.getExecutionsList()) {
      WorkflowStatusObj workflowStatusObjOpen = new WorkflowStatusObj();
      workflowStatusObjOpen.setWorkflowId(workflowExecutionInfo.getExecution().getWorkflowId());
      workflowStatusObjOpen.setWorkflowStatus(
          getWorkflowStatus(workflowExecutionInfo.getStatus().toString()));
      workflowStatusObjOpen.setUrl(
          getWorkflowUrl(workflowExecutionInfo.getExecution().getWorkflowId()));
      workflowStatusObjList.add(workflowStatusObjOpen);
    }

    for (WorkflowExecutionInfo workflowExecutionInfo : responseClosed.getExecutionsList()) {
      WorkflowStatusObj workflowStatusObjClosed = new WorkflowStatusObj();
      workflowStatusObjClosed.setWorkflowId(workflowExecutionInfo.getExecution().getWorkflowId());
      workflowStatusObjClosed.setWorkflowStatus(
          getWorkflowStatus(workflowExecutionInfo.getStatus().toString()));
      workflowStatusObjClosed.setUrl(
          getWorkflowUrl(workflowExecutionInfo.getExecution().getWorkflowId()));
      workflowStatusObjList.add(workflowStatusObjClosed);
    }

    return workflowStatusObjList;
  }

  // in the format the UI expects
  private static String getWorkflowStatus(String input) {
    if (input == null || input.isEmpty()) {
      return ""; // Return empty string if input is null or empty
    }

    List<String> parts = Splitter.on('_').splitToList(input);

    return parts.get(parts.size() - 1); // Return the last part
  }

  private static Timestamp getOneHourAgo() {
    // Get current date-time in UTC
    LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

    // Subtract one hour
    LocalDateTime oneHourAgo = now.minusHours(1);

    // Convert LocalDateTime to Instant
    Instant instant = oneHourAgo.atZone(ZoneId.of("UTC")).toInstant();

    // Convert Instant to Timestamp
    Timestamp timestamp =
        Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();

    return timestamp;
  }

  private static String getWorkflowUrl(String workflowId) {
    String url = "";
    if (ServerInfo.getAddress().endsWith(".tmprl.cloud:7233")) {
      url =
          "https://cloud.temporal.io/namespaces/"
              + ServerInfo.getNamespace()
              + "/workflows/"
              + workflowId;
    }
    return url;
  }

  public static void main(String[] args) throws FileNotFoundException, SSLException {
    List<WorkflowStatusObj> workflowStatusObjList = listWorkflows();
    for (WorkflowStatusObj workflowStatusObj : workflowStatusObjList) {
      System.out.println(
          workflowStatusObj.getWorkflowId()
              + " "
              + workflowStatusObj.getWorkflowStatus()
              + " "
              + workflowStatusObj.getUrl());
    }
  }
}
