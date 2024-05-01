package io.temporal.samples.moneytransfer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.samples.moneytransfer.dataclasses.ChargeResponseObj;
import io.temporal.samples.moneytransfer.dataclasses.ExecutionScenarioObj;
import io.temporal.samples.moneytransfer.dataclasses.ResultObj;
import io.temporal.samples.moneytransfer.dataclasses.WorkflowParameterObj;
import io.temporal.testing.TestWorkflowRule;
import java.time.Duration;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class TransferWorkflowTest {

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          .setWorkflowTypes(AccountTransferWorkflowImpl.class)
          .setDoNotStart(true)
          .build();

  /** Test workflow with real activities */
  @Test
  public void testWorkflowHappyPath() {
    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(new AccountTransferActivitiesImpl());
    testWorkflowRule.getTestEnvironment().start();

    // Get a workflow stub using the same task queue the worker uses.
    AccountTransferWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                AccountTransferWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    // Execute a workflow waiting for it to complete.
    WorkflowParameterObj workflowParameterObj = new WorkflowParameterObj();
    workflowParameterObj.setAmount(100);
    workflowParameterObj.setScenario(ExecutionScenarioObj.HAPPY_PATH);

    ResultObj result = workflow.transfer(workflowParameterObj);
    assertEquals(
        new ResultObj(new ChargeResponseObj("example-charge-id"))
            .getChargeResponseObj()
            .getChargeId(),
        result.getChargeResponseObj().getChargeId());
  }

  /** Test human in the loop scenario */
  @Test
  public void testWorkflowHumanInLoop() {
    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(new AccountTransferActivitiesImpl());
    testWorkflowRule.getTestEnvironment().start();

    String WORKFLOW_ID = "HumanInLoopWorkflow";

    // Get a workflow stub using the same task queue the worker uses.
    AccountTransferWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                AccountTransferWorkflow.class,
                WorkflowOptions.newBuilder()
                    .setWorkflowId(WORKFLOW_ID)
                    .setTaskQueue(testWorkflowRule.getTaskQueue())
                    .build());
    // Execute a workflow waiting for it to complete.
    WorkflowParameterObj workflowParameterObj = new WorkflowParameterObj();
    workflowParameterObj.setAmount(100);
    workflowParameterObj.setScenario(ExecutionScenarioObj.HUMAN_IN_LOOP);

    WorkflowClient.start(workflow::transfer, workflowParameterObj);

    // Skip time so we're waiting for a signal
    testWorkflowRule.getTestEnvironment().sleep(Duration.ofSeconds(15));
    // signal the workflow
    workflow.approveTransfer();

    ResultObj resultObj = WorkflowStub.fromTyped(workflow).getResult(ResultObj.class);

    assertEquals(
        new ResultObj(new ChargeResponseObj("example-charge-id"))
            .getChargeResponseObj()
            .getChargeId(),
        resultObj.getChargeResponseObj().getChargeId());
  }

  /** Test workflow with mocked activities */
  @Test
  public void testMockedActivity() {
    AccountTransferActivities activities =
        mock(AccountTransferActivities.class, withSettings().withoutAnnotations());

    ChargeResponseObj chargeResponseObj = new ChargeResponseObj("example-charge-id");

    when(activities.validate(ExecutionScenarioObj.HAPPY_PATH)).thenReturn(true);
    when(activities.withdraw(100.0f, ExecutionScenarioObj.HAPPY_PATH)).thenReturn("SUCCESS");
    when(activities.deposit(anyString(), eq(100.0f), eq(ExecutionScenarioObj.HAPPY_PATH)))
        .thenReturn(chargeResponseObj);
    testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
    testWorkflowRule.getTestEnvironment().start();

    // Get a workflow stub using the same task queue the worker uses.
    AccountTransferWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                AccountTransferWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    // Execute a workflow waiting for it to complete.
    WorkflowParameterObj workflowParameterObj = new WorkflowParameterObj();
    workflowParameterObj.setAmount(100);
    workflowParameterObj.setScenario(ExecutionScenarioObj.HAPPY_PATH);

    ResultObj result = workflow.transfer(workflowParameterObj);
    assertEquals(
        new ResultObj(new ChargeResponseObj("example-charge-id"))
            .getChargeResponseObj()
            .getChargeId(),
        result.getChargeResponseObj().getChargeId());
  }

  // Clean up test environment after tests are completed
  @After
  public void tearDown() {
    testWorkflowRule.getTestEnvironment().shutdown();
  }
}
