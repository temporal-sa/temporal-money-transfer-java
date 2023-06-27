package io.temporal.samples.moneytransfer.dataclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StateObj {
  private int progressPercentage;
  private String transferState;
  private ChargeResponse chargeResult; // this can be null

  // no-arg constructor
  public StateObj() {}

  // Constructor with JsonProperty annotation
  @JsonCreator
  public StateObj(
      @JsonProperty("progressPercentage") int progressPercentage,
      @JsonProperty("transferState") String transferState,
      @JsonProperty("chargeResult") ChargeResponse chargeResult) {
    this.progressPercentage = progressPercentage;
    this.transferState = transferState;
    this.chargeResult = chargeResult;
  }

  // Getters
  public int getProgressPercentage() {
    return progressPercentage;
  }

  public String getTransferState() {
    return transferState;
  }

  public ChargeResponse getChargeResult() {
    return chargeResult;
  }

  // Setters
  public void setProgressPercentage(int progressPercentage) {
    this.progressPercentage = progressPercentage;
  }

  public void setTransferState(String transferState) {
    this.transferState = transferState;
  }

  public void setChargeResult(ChargeResponse chargeResult) {
    this.chargeResult = chargeResult;
  }
}
