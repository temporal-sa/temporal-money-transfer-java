package io.temporal.samples.moneytransfer;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.samples.moneytransfer.dataclasses.ChargeResponse;
import io.temporal.samples.moneytransfer.dataclasses.ResultObj;
import io.temporal.samples.moneytransfer.dataclasses.StateObj;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowParameterObj;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class AccountTransferWorkflowImpl implements AccountTransferWorkflow {
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

        if (params.getAmount() == 101) {
            throw new RuntimeException("simulated"); // Uncomment to simulate failure
        }

        // run activity
        chargeResult = transferService.createCharge(idempotencyKey, params.getAmount());

        Workflow.sleep(Duration.ofSeconds(5));

        progressPercentage = 100;
        transferState = "finished";

        return new ResultObj(chargeResult);
    }

    @Override
    public StateObj getStateQuery() {
        StateObj stateObj = new StateObj(progressPercentage, transferState, "", chargeResult);
        return stateObj;
    }
}
