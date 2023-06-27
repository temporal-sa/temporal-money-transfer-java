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

import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.samples.moneytransfer.dataconverter.CryptCodec;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.util.Collections;

public class AccountTransferWorker {

  @SuppressWarnings("CatchAndPrintStackTrace")
  public static void main(String[] args) throws Exception {

    // client that can be used to start and signal workflows
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

    // worker factory that can be used to create workers for specific task queues
    WorkerFactory factory = WorkerFactory.newInstance(TemporalClient.get());
    Worker workerForCommonTaskQueue = factory.newWorker(TASK_QUEUE);
    workerForCommonTaskQueue.registerWorkflowImplementationTypes(AccountTransferWorkflowImpl.class);
    // Start all workers created by this factory.
    factory.start();
    System.out.println("Worker started for task queue: " + TASK_QUEUE);
  }
}
