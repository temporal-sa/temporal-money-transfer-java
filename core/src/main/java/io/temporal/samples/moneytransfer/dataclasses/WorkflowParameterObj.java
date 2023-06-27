package io.temporal.samples.moneytransfer.dataclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowParameterObj {
  // in cents
  private int amount;

  // No-arg constructor
  public WorkflowParameterObj() {}

  // Constructor
  @JsonCreator
  public WorkflowParameterObj(@JsonProperty("amountCents") int amount) {
    this.amount = amount;
  }

  // Getter
  public int getAmount() {
    return amount;
  }

  // Setter
  public void setAmount(int amount) {
    this.amount = amount;
  }
}
