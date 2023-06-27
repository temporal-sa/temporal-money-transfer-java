package io.temporal.samples.moneytransfer.dataclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChargeResponse {
  private String chargeId;

  // no-arg constructor
  public ChargeResponse() {}

  // constructor with JsonProperty annotation
  @JsonCreator
  public ChargeResponse(@JsonProperty("chargeId") String chargeId) {
    this.chargeId = chargeId;
  }

  // getter
  public String getChargeId() {
    return chargeId;
  }

  // setter
  public void setChargeId(String chargeId) {
    this.chargeId = chargeId;
  }
}
