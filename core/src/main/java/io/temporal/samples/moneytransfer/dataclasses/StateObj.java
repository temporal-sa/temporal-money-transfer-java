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

public class StateObj {
  private int approvalTime;
  private int progressPercentage;
  private String transferState;
  private String workflowStatus; // this is nullable
  private ChargeResponseObj chargeResult; // this can be null

  // no-arg constructor
  public StateObj() {}

  // Constructor with JsonProperty annotation
  @JsonCreator
  public StateObj(
      @JsonProperty("progressPercentage") int progressPercentage,
      @JsonProperty("transferState") String transferState,
      @JsonProperty("workflowStatus") String workflowStatus,
      @JsonProperty("chargeResult") ChargeResponseObj chargeResult,
      @JsonProperty("approvalTime") int approvalTime) {
    this.progressPercentage = progressPercentage;
    this.transferState = transferState;
    this.workflowStatus = workflowStatus;
    this.chargeResult = chargeResult;
    this.approvalTime = approvalTime;
  }

  // Getters
  public int getProgressPercentage() {
    return progressPercentage;
  }

  public String getTransferState() {
    return transferState;
  }

  public String getWorkflowStatus() {
    return workflowStatus;
  }

  public ChargeResponseObj getChargeResult() {
    return chargeResult;
  }

  public int getApprovalTime() {
    return approvalTime;
  }

  // Setters
  public void setProgressPercentage(int progressPercentage) {
    this.progressPercentage = progressPercentage;
  }

  public void setTransferState(String transferState) {
    this.transferState = transferState;
  }

  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  public void setChargeResult(ChargeResponseObj chargeResult) {
    this.chargeResult = chargeResult;
  }

  public void setApprovalTime(int approvalTime) {
    this.approvalTime = approvalTime;
  }
}
