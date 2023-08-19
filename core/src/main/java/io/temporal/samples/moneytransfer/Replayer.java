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

import io.temporal.testing.WorkflowReplayer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Replayer {
  public static void main(String[] args) throws IOException {
    // get command line argument for string WorkflowId
    String historyFile = args[0];

    Path historyFilePath = Paths.get(historyFile);

    System.out.println("Reading from file: " + historyFilePath.toAbsolutePath());
    String historyFileString =
        new String(Files.readAllBytes(historyFilePath), StandardCharsets.UTF_8);

    System.out.println("History file string length: " + historyFileString.length());

    try {
      // read history file to string

      System.out.println("Replaying workflow: " + historyFile);

      WorkflowReplayer.replayWorkflowExecution(
          historyFileString, AccountTransferWorkflowImpl.class);

      System.out.println("Replay completed successfully");
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(
          "Replay failed, check above output for io.temporal.worker.NonDeterministicException");
    }
  }
}
