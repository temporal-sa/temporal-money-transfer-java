package io.temporal.samples.moneytransfer.dataclasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowParameterObj {
  private int amountCents;

  // No-arg constructor
  public WorkflowParameterObj() {}

  // Constructor
  @JsonCreator
  public WorkflowParameterObj(@JsonProperty("amountCents") int amountCents) {
    this.amountCents = amountCents;
  }

  // Getter
  public int getAmountCents() {
    return amountCents;
  }

  // Setter
  public void setAmountCents(int amountCents) {
    this.amountCents = amountCents;
  }
}
