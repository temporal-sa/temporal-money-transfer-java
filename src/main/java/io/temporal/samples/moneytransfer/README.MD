The Money Transfer sample has three separate processes.
One to host Workflow Executions, another to host Activity Executions, and the third one to request transfers (start Workflow Executions).

## Running the Workflow

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

## **Simulating errors**

- Uncomment the invalidAccount line in `TransferRequester.java` to simulate a **deposit** failure and rollback.
```agsl
Account toAccount = new Account("acct2invalid", 290); // for invalid account
```

