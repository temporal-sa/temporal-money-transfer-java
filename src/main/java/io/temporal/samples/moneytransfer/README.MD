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

## **Simulating compensations**

Uncomment this line in `TransferRequester.java` to simulate a **withdrawal** failure:
```agsl
String from = "account1_FAIL";
```

Uncomment this line in `TransferRequester.java` to simulate a **deposit** failure:
```agsl
String to = "account2_FAIL";
```