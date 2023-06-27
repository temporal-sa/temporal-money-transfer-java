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

import static io.temporal.samples.moneytransfer.TransferRequester.*;

import io.javalin.Javalin;
import io.temporal.samples.moneytransfer.dataclasses.ResultObj;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowIdObj;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowParameterObj;
import java.util.AbstractMap;

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
          System.out.println("AMOUNT");
          System.out.println();

          String transferId = runWorkflow(workflowParameterObj);

          ctx.json(new AbstractMap.SimpleEntry<>("transferId", transferId));
        });

    app.post(
        "/runQuery",
        ctx -> {
          // get workflowId from request POST body
          WorkflowIdObj workflowIdObj = ctx.bodyAsClass(WorkflowIdObj.class);
          String workflowId = workflowIdObj.getWorkflowId();

          System.out.println("QUERY workflowId: " + workflowId);

          String transferState = runQuery(workflowId);

          System.out.println("state: " + transferState);

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

    app.get("/test", ctx -> ctx.result("Hello Javalin!"));

    app.start(7070);
  }
}
