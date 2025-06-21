/*
 * Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 * Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not
 * use this file except in compliance with the License. A copy of the License is
 * located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.temporal.samples.moneytransfer;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.client.schedules.ScheduleClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import java.io.FileNotFoundException;
import javax.net.ssl.SSLException;

public class TemporalClient {

  /**
   * Centralized method to create and configure WorkflowServiceStubs. This is the single source of
   * truth for connecting to Temporal.
   */
  private static WorkflowServiceStubs createWorkflowServiceStubs() {
    // These are the values for connecting to Temporal Cloud
    String temporalCloudEndpoint = System.getenv("TEMPORAL_ADDRESS");
    String temporalCloudNamespace = System.getenv("TEMPORAL_NAMESPACE");
    String temporalApiKey = System.getenv("TEMPORAL_API_KEY");

    // If the environment variables for cloud are not set, assume local connection.
    // This check makes the code work for both local dev and cloud deployments.
    boolean isCloudConnection =
        temporalCloudEndpoint != null
            && !temporalCloudEndpoint.isEmpty()
            && temporalCloudNamespace != null
            && !temporalCloudNamespace.isEmpty()
            && temporalApiKey != null
            && !temporalApiKey.isEmpty();

    if (isCloudConnection) {
      System.out.println("--- Connecting to Temporal Cloud ---");
      System.out.println("Endpoint: " + temporalCloudEndpoint);
      System.out.println("Namespace: " + temporalCloudNamespace);
      // System.out.println("API Key: " + temporalApiKey);
      System.out.println("---------------------------------");

      return WorkflowServiceStubs.newServiceStubs(
          WorkflowServiceStubsOptions.newBuilder()
              .setTarget(temporalCloudEndpoint)
              .setEnableHttps(true)
              .addApiKey(() -> temporalApiKey)
              .build());
    } else {
      System.out.println("--- Connecting to Local Temporal ---");
      return WorkflowServiceStubs.newLocalServiceStubs();
    }
  }

  /**
   * This method is preserved to prevent build failures across the project. It now uses the new,
   * correct connection logic.
   */
  public static WorkflowServiceStubs getWorkflowServiceStubs()
      throws FileNotFoundException, SSLException {
    return createWorkflowServiceStubs();
  }

  /** Gets a fully configured WorkflowClient. */
  public static WorkflowClient get() throws FileNotFoundException, SSLException {
    WorkflowServiceStubs service = createWorkflowServiceStubs();

    // Use the correct namespace for the client options
    String namespace = System.getenv("TEMPORAL_NAMESPACE");
    if (namespace == null || namespace.isEmpty()) {
      namespace = "default"; // Fallback for local development
    }

    WorkflowClientOptions clientOptions =
        WorkflowClientOptions.newBuilder()
            .setNamespace(namespace)
            // .setDataConverter(...) // Your custom data converter can be added here
            .build();

    return WorkflowClient.newInstance(service, clientOptions);
  }

  /** Gets a fully configured ScheduleClient. */
  public static ScheduleClient getScheduleClient() throws FileNotFoundException, SSLException {
    WorkflowServiceStubs service = createWorkflowServiceStubs();

    String namespace = System.getenv("TEMPORAL_NAMESPACE");
    if (namespace == null || namespace.isEmpty()) {
      namespace = "default"; // Fallback for local development
    }

    ScheduleClientOptions clientOptions =
        ScheduleClientOptions.newBuilder()
            .setNamespace(namespace)
            // .setDataConverter(...) // Your custom data converter can be added here
            .build();

    return ScheduleClient.newInstance(service, clientOptions);
  }
}
