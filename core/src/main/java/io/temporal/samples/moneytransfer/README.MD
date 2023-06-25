## Money Transfer Example (with retries and compensation)

The Money Transfer sample has three separate processes.
One to host Workflow Executions, another to host Activity Executions, and the third one to request transfers (start Workflow Executions).

### Running the Workflow

Start Workflow Worker:

```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker --console=plain
```

Start Activity Worker:

```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountActivityWorker --console=plain
```

Execute once per requested transfer:

```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferRequester
```

### **Simulating errors**

- Path 1: Happy Path
  - Leave as-is
- Path 2: Simulate Multiple API call attempts before success
  - - In `TransferRequester.java`, set `simulateDepositRetries = true`
- Path 3: Rollback a deposit due to an invalid account 
  - In `TransferRequester.java`, uncomment the `new Account("acct2invalid"...` line to simulate a **deposit** failure and rollback.
- Path 4: Simulate resuming a workflow after losing all workers
  - Wait for "Withdrawal done.." in the console, then kill the worker
  - Restart the worker. The workflow will continue where it left off.
