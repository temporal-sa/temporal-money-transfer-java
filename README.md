## Money Transfer Example

The Money Transfer sample has four separate Gradle tasks.
One to host Workflow Executions, another to host Activity Executions, a Web UI for running transfers, and a CLI for doing the same.

### Connecting to a Temporal Server

The sample is configured by default to connect to a [local Temporal Server](https://docs.temporal.io/cli#starting-the-temporal-server) running on localhost:7233.

To instead connect to Temporal Cloud, set the following environment variables, replacing them with your own Temporal Cloud credentials:

```bash
TEMPORAL_ADDRESS=testnamespace.sdvdw.tmprl.cloud:7233
TEMPORAL_NAMESPACE=testnamespace.sdvdw
TEMPORAL_CERT_PATH="/path/to/file.pem"
TEMPORAL_KEY_PATH="/path/to/file.key"
````

### Running the Workflow

Start Workflow Worker:

```bash
ENCRYPT_PAYLOADS=true ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker --console=plain
```

Start Activity Worker:

```bash
ENCRYPT_PAYLOADS=true ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountActivityWorker --console=plain
```

Run the money transfer form UI:

```bash
ENCRYPT_PAYLOADS=true ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.web.WebServer --console=plain
```
Then navigate to `http://localhost:7070/`

OR

Execute a workflow from the CLI:

```bash
ENCRYPT_PAYLOADS=true ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferRequester
```

Remove the `ENCRYPT_PAYLOADS` variable in each command to run without encryption.

You can decrypt these payloads in Temporal Cloud's UI/cli using the codec server: `https://codec.tmprl-demo.cloud/` ([source](https://github.com/steveandroulakis/temporal-codec-server)). Ensure you switch on "Pass the user access token with your endpoint". Note: The codec server is only compatible with workflows running in Temporal Cloud.

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
