## Money Transfer Example

The Money Transfer sample has four separate tasks.
One to host Workflow Executions, another to host Activity Executions, and a Web UI for running transfers and a CLI for doing the same.

### Running the Workflow

Start Workflow Worker:

```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker --console=plain
```

Start Activity Worker:

```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountActivityWorker --console=plain
```

Place build/ in resources/svelte_ui/
```bash
ENCRYPT_PAYLOADS=true ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.web.WebServer --console=plain
```

OR

Execute a workflow from the CLI:

```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferRequester
```

## Demo various failures and recovery

```
Set dollar amounts in the UI to these to demonstrate various functionality

## Recoverable Failures Demo
amount == 101 -> workflow exception (non-failure)
amount == 99 -> activity timeout then recovery on 5th attempt

## UnRecoverable failure 'insufficient funds' exception
amount > 1000 -> fails workflow

## Reset Workflows
### List failed workflows
temporal workflow list --env prod -q 'ExecutionStatus="Failed" OR ExecutionStatus="Terminated"'

## Simulating a reset that re-runs a failed workflow which becomes successful
temporal workflow show --env prod --workflow-id=<your failed workflow ID>
# from the event list, find a [WorkflowTaskScheduled +1, WorkflowTaskStarted + 1] event id before the charge activity
### comment out insufficient funds code in TransferServiceImpl so it succeeds on reset
# then reset to a point before that, e.g.
temporal workflow reset --workflow-id=your failed workflow ID> --event-id 8 --reason "fix"
```