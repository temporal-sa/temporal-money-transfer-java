## Money Transfer Example (with retries and compensation)

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

