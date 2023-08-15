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

package io.temporal.samples.moneytransfer.dataclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowParameterObj {
  // in cents
  private int amount;

  private ExecutionScenarioObj scenario;

  // No-arg constructor
  public WorkflowParameterObj() {}

  // Constructor
  @JsonCreator
  public WorkflowParameterObj(
      @JsonProperty("amountCents") int amount,
      @JsonProperty("scenario") ExecutionScenarioObj scenario) {
    this.amount = amount;
    this.scenario = scenario;
  }

  // Getters
  public int getAmount() {
    return amount;
  }

  public ExecutionScenarioObj getScenario() {
    return scenario;
  }

  // Setters
  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void setScenario(ExecutionScenarioObj scenario) {
    this.scenario = scenario;
  }
}
