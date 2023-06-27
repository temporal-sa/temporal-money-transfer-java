package io.temporal.samples.moneytransfer.dataclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultObj {
  private ChargeResponse chargeResponse;

  // no-arg constructor
  public ResultObj() {}

  // Constructor with JsonProperty annotation
  @JsonCreator
  public ResultObj(@JsonProperty("chargeResponse") ChargeResponse chargeResponse) {
    this.chargeResponse = chargeResponse;
  }

  // Getter
  public ChargeResponse getChargeResponse() {
    return chargeResponse;
  }

  // Setter
  public void setChargeResponse(ChargeResponse chargeResponse) {
    this.chargeResponse = chargeResponse;
  }
}
