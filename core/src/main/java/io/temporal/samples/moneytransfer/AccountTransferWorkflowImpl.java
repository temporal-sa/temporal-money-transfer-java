package io.temporal.samples.moneytransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.dataclasses.ChargeResponse;
import io.temporal.samples.moneytransfer.dataclasses.ResultObj;
import io.temporal.samples.moneytransfer.dataclasses.StateObj;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowParameterObj;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import java.time.Duration;

@WorkflowInterface
public class AccountTransferWorkflowImpl implements AccountTransferWorkflow {

  @Override
  public String getStateQuery() throws JsonProcessingException {
    StateObj stateObj = new StateObj(progressPercentage, transferState, chargeResult.getChargeId());
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(stateObj);

    return json;
  }

  private final ActivityOptions options =
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .setRetryOptions(
              RetryOptions.newBuilder().setDoNotRetry("StripeInvalidRequestError").build())
          .build();

  private final TransferService transferService =
      Workflow.newActivityStub(TransferService.class, options);

  private int progressPercentage = 25;
  private String transferState = "starting";
  private ChargeResponse chargeResult = new ChargeResponse("");

  @Override
  public ResultObj transfer(WorkflowParameterObj params) {

    Workflow.sleep(Duration.ofSeconds(2));

    progressPercentage = 75;
    transferState = "running";

    String idempotencyKey = Workflow.randomUUID().toString();

    try {
      chargeResult = transferService.createCharge(idempotencyKey, params.getAmountCents());
    } catch (Exception err) {
      String message = "Failed to charge customer for. Error: " + err.getMessage();
      throw ApplicationFailure.newNonRetryableFailure(message, "Exception");
    }

    Workflow.sleep(Duration.ofSeconds(5));

    progressPercentage = 100;
    transferState = "finished";

    return new ResultObj(chargeResult);
  }
}
