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

package io.temporal.samples.moneytransfer.web;

import static io.temporal.samples.moneytransfer.TransferLister.listWorkflows;
import static io.temporal.samples.moneytransfer.TransferRequester.*;
import static io.temporal.samples.moneytransfer.TransferScheduler.runSchedule;

import io.javalin.Javalin;
import io.temporal.samples.moneytransfer.dataclasses.*;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WebServer {
  public static void main(String[] args) {
    Javalin app =
        Javalin.create(
            config -> {
              config.staticFiles.add(
                  staticFiles -> {
                    staticFiles.hostedPath = "/";
                    staticFiles.directory = "svelte_ui/build";
                    // are located
                  });
            });

    app.get(
        "/serverinfo",
        ctx -> {
          // some code
          ctx.json(ServerInfo.getServerInfo());
        });

    app.post(
        "/runWorkflow",
        ctx -> {
          WorkflowParameterObj workflowParameterObj = ctx.bodyAsClass(WorkflowParameterObj.class);

          String transferId = runWorkflow(workflowParameterObj);

          ctx.json(new AbstractMap.SimpleEntry<>("transferId", transferId));
        });

    app.post(
        "/scheduleWorkflow",
        ctx -> {
          ScheduleParameterObj scheduleParameterObj = ctx.bodyAsClass(ScheduleParameterObj.class);

          String transferId = runSchedule(scheduleParameterObj);

          ctx.json(new AbstractMap.SimpleEntry<>("transferId", transferId));
        });

    app.post(
        "/runQuery",
        ctx -> {
          // get workflowId from request POST body
          WorkflowIdObj workflowIdObj = ctx.bodyAsClass(WorkflowIdObj.class);
          String workflowId = workflowIdObj.getWorkflowId();

          System.out.println("QUERY workflowId: " + workflowId);

          StateObj transferState = runQuery(workflowId);

          // System.out.println("state: " + transferState);

          ctx.json(transferState);
        });

    app.post(
        "/getWorkflowOutcome",
        ctx -> {
          if (ctx.formParam("workflowId") == null) {
            ctx.json(new AbstractMap.SimpleEntry<>("message", "workflowId is required"));
            return;
          }

          // get workflowId from request POST body
          String workflowId = ctx.formParam("workflowId");

          ResultObj workflowOutcome = getWorkflowOutcome(workflowId);

          System.out.println("outcome: " + workflowOutcome);

          ctx.json(workflowOutcome);
        });

    app.get(
        "/listWorkflows",
        ctx -> {
          List<WorkflowStatusObj> workflowList = listWorkflows();

          ctx.json(workflowList);
        });

    app.get("/test", ctx -> ctx.result("Hello Javalin!"));

    app.get(
        "/simulateDelay",
        ctx -> {
          String seconds_param = ctx.queryParam("s");

          if (seconds_param != null) {
            int seconds = Integer.parseInt(seconds_param);
            System.out.println("Simulating API response delay: " + seconds);
            TimeUnit.SECONDS.sleep(seconds);
            ctx.result("Delay finished after " + seconds + " seconds");
          } else {
            ctx.result("use query param s to specify seconds to delay");
          }
        });

    app.post(
        "/approveTransfer",
        ctx -> {
          // get workflowId from request POST body
          WorkflowIdObj workflowIdObj = ctx.bodyAsClass(WorkflowIdObj.class);
          String workflowId = workflowIdObj.getWorkflowId();

          runApproveSignal(workflowId);

          ctx.result("{\"signal\": \"sent\"}");
        });

    app.start(7070);
  }
}
