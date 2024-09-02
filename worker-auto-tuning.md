## Worker Auto-tuning

`AccountTransferWorker.java`
```java
    WorkerOptions workerOptions =
        WorkerOptions.newBuilder()
            .setWorkerTuner(
                ResourceBasedTuner.newBuilder()
                    .setControllerOptions(
                        ResourceBasedControllerOptions.newBuilder(0.7, 0.7).build())
                    .build())
            .build();
```
Sets the worker tuner to ResourceBasedTuner with controller options of 0.7 and 0.7 (or 70% of memory and CPU).

Run the worker with Xms and Xmx options:
```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker --console=plain -Dorg.gradle.jvmargs="-Xms8m -Xmx1024m"
```
This means that your JVM will be started with Xms amount of memory and will be able to use a maximum of Xmx amount of memory.

Run bash convenience script `get_worker_memory_usage.sh` to get the memory usage of each worker.