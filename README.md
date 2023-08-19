## Money Transfer Example

The Money Transfer sample has four separate Gradle tasks.
One to host Workflow Executions, another to host Activity Executions, a Web UI for running transfers, and a CLI for doing the same.

### Demos
- Activity retries (simulated API delay)
- Recoverable failures
- Unrecoverable failures

### Connecting to a Temporal Server

The sample is configured by default to connect to a [local Temporal Server](https://docs.temporal.io/cli#starting-the-temporal-server) running on localhost:7233.

To instead connect to Temporal Cloud, set the following environment variables, replacing them with your own Temporal Cloud credentials:

```bash
TEMPORAL_ADDRESS=testnamespace.sdvdw.tmprl.cloud:7233
TEMPORAL_NAMESPACE=testnamespace.sdvdw
TEMPORAL_CERT_PATH="/path/to/file.pem"
TEMPORAL_KEY_PATH="/path/to/file.key"
````

(optional) set a task queue name
```bash
export TEMPORAL_MONEYTRANSFER_TASKQUEUE="MoneyTransferSampleJava"
```

### Running the Workflow

Note: Use a Java 18 SDK.

Start a worker:

```bash
ENCRYPT_PAYLOADS=true ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker --console=plain
```

Run the money transfer web UI:

```bash
ENCRYPT_PAYLOADS=true ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.web.WebServer --console=plain
```
Then navigate to `http://localhost:7070/`

Send approval signal (for transfers > $100):

```bash
# where TRANSFER-EZF-249 is the workflowId
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferApprover -Parg=TRANSFER-XXX-XXX
````

You can also do this through the `temporal` cli:
```bash
temporal workflow signal \
 --env prod \
 --query 'WorkflowId="TRANSFER-XXX-XXX"' \
 --name approveTransfer \
 --reason 'approving transfer'
```

Or in the Temporal workflow UI.

## Encryption

Remove the `ENCRYPT_PAYLOADS` variable in each command to run without encryption.

You can decrypt these payloads in Temporal Cloud's UI/cli using the codec server: `https://codec.tmprl-demo.cloud` ([source](https://github.com/steveandroulakis/temporal-codec-server)). Ensure you switch on "Pass the user access token with your endpoint". Note: The codec server is only compatible with workflows running in Temporal Cloud.

## Demo various failures and recovery

UI dropdown simulates the following scenarios

```
## Require Human-In-Loop Approval
wait for approval signal # see 'Send approval signal' code above. Workflow will fail after 30s if not approved

## Bug in Workflow (recoverable failure)
comment out exception in workflow code (`AccountTransferWorkflowImpl.java`) and restart worker to fix

## API Downtime (recover on 5th attempt)
activity timeout then recovery on 5th attempt

## Insufficient Funds (unrecoverable failure)
Fails workflow
```

Advanced: You can also simulate these scenarios using the Temporal CLI
```
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
