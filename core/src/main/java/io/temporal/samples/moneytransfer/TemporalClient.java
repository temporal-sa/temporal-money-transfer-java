package io.temporal.samples.moneytransfer;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
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
  public static WorkflowClient get() throws FileNotFoundException, SSLException {
    // TODO support local server
    // Get worker to poll the common task queue.
    // gRPC stubs wrapper that talks to the local docker instance of temporal service.
    // WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    WorkflowServiceStubsOptions.Builder workflowServiceStubsOptionsBuilder =
        WorkflowServiceStubsOptions.newBuilder();

    if (ServerInfo.getCertPath() != null && ServerInfo.getKeyPath() != null) {
      InputStream clientCert = new FileInputStream(ServerInfo.getCertPath());

      InputStream clientKey = new FileInputStream(ServerInfo.getKeyPath());

      workflowServiceStubsOptionsBuilder.setSslContext(
          SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build());
    }

    // For temporal cloud this would likely be ${namespace}.tmprl.cloud:7233
    String targetEndpoint = ServerInfo.getAddress();
    // Your registered namespace.
    String namespace = ServerInfo.getNamespace();

    workflowServiceStubsOptionsBuilder.setTarget(targetEndpoint);

    // Create SSL enabled client by passing SslContext, created by SimpleSslContextBuilder.
    WorkflowServiceStubs service =
        WorkflowServiceStubs.newServiceStubs(workflowServiceStubsOptionsBuilder.build());

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

    WorkflowClientOptions clientOptions = builder.setNamespace(namespace).build();

    // client that can be used to start and signal workflows
    WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);
    return client;
  }
}
