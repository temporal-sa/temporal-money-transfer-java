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
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.samples.moneytransfer.dataconverter.CryptCodec;
import io.temporal.samples.moneytransfer.web.ServerInfo;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import javax.net.ssl.SSLException;

public class TemporalClient {

  /**
   * Centralized method to create and configure WorkflowServiceStubs. This is the single source of
   * truth for connecting to Temporal. Supports three connection methods: 1. Certificate-based
   * (mTLS) - requires TEMPORAL_CERT_PATH and TEMPORAL_KEY_PATH 2. API Key-based - requires
   * TEMPORAL_API_KEY 3. Local - fallback when neither certificates nor API key are provided
   */
  private static WorkflowServiceStubs createWorkflowServiceStubs()
      throws FileNotFoundException, SSLException {
    String endpoint = ServerInfo.getAddress();
    String namespace = ServerInfo.getNamespace();
    String apiKey = ServerInfo.getApiKey();
    String certPath = ServerInfo.getCertPath();
    String keyPath = ServerInfo.getKeyPath();

    System.out.println("TEMPORAL_ADDRESS: " + endpoint);
    System.out.println("TEMPORAL_NAMESPACE: " + namespace);

    WorkflowServiceStubsOptions.Builder optionsBuilder =
        WorkflowServiceStubsOptions.newBuilder().setTarget(endpoint);

    // Check if certificates are provided (mTLS connection)
    boolean hasCertificates = !certPath.isEmpty() && !keyPath.isEmpty();

    // Check if API key is provided
    boolean hasApiKey = apiKey != null && !apiKey.isEmpty();

    // Check if using local server
    boolean isLocal = "localhost:7233".equals(endpoint);

    if (hasCertificates) {
      System.out.println("--- Connecting with Certificate Authentication ---");
      System.out.println("Cert path: " + certPath);
      System.out.println("Key path: " + keyPath);
      System.out.println("Endpoint: " + endpoint);
      System.out.println("---------------------------------");

      InputStream clientCert = new FileInputStream(certPath);
      InputStream clientKey = new FileInputStream(keyPath);

      optionsBuilder.setSslContext(SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build());

      return WorkflowServiceStubs.newServiceStubs(optionsBuilder.build());

    } else if (hasApiKey && !isLocal) {
      System.out.println("--- Connecting with API Key Authentication ---");
      System.out.println("Endpoint: " + endpoint);
      System.out.println("API key length: " + apiKey.length());
      System.out.println("---------------------------------");

      return WorkflowServiceStubs.newServiceStubs(
          optionsBuilder.setEnableHttps(true).addApiKey(() -> apiKey).build());

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

    String namespace = ServerInfo.getNamespace();
    if (namespace == null || namespace.isEmpty()) {
      namespace = "default";
    }

    WorkflowClientOptions.Builder builder = WorkflowClientOptions.newBuilder();

    // If environment variable ENCRYPT_PAYLOADS is set to true, then use CryptCodec
    if (System.getenv("ENCRYPT_PAYLOADS") != null
        && System.getenv("ENCRYPT_PAYLOADS").equals("true")) {
      builder.setDataConverter(
          new CodecDataConverter(
              DefaultDataConverter.newDefaultInstance(),
              Collections.singletonList(new CryptCodec()),
              true /* encode failure attributes */));
    }

    System.out.println("<<<<SERVER INFO>>>>:\n " + ServerInfo.getServerInfo());
    WorkflowClientOptions clientOptions = builder.setNamespace(namespace).build();

    return WorkflowClient.newInstance(service, clientOptions);
  }

  /** Gets a fully configured ScheduleClient. */
  public static ScheduleClient getScheduleClient() throws FileNotFoundException, SSLException {
    WorkflowServiceStubs service = createWorkflowServiceStubs();

    String namespace = ServerInfo.getNamespace();
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
