## Worker Auto-tuning

[Blog Announcement](https://temporal.io/change-log/announcing-auto-tuning-for-workers-in-pre-release)

### Testing worker performance with different worker tuning memory and CPU settings

This guide demonstrates how to use the ResourceBasedTuner to automatically tune the worker based on the available resources.

The worker has been set up to use 70% of memory and CPU by default. This can be changed in the worker options:

[AccountTransferWorker.java](./core/src/main/java/io/temporal/samples/moneytransfer/AccountTransferWorker.java)

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
## Test Scenario

### 70% memory and CPU

Start a worker:
```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker
```

This will start a worker that will use 70% of memory and CPU (this app's default).

Start 10 transfers with the STRESS_TEST scenario:
```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferRequester
```
This scenario will allocate some memory stress test the CPU for a number of seconds. The activity will retry and complete. 

The worker should be able to handle the 10 transfers with 70% of memory and CPU. In my test on my fast M2 Macbook, it took 6 seconds.

`./get_worker_memory_usage.sh` should show actual memory usage of the worker. Note this value, as it will be lower once we reconfigure the worker to use less memory.

Example:
```
Checking JVM memory settings for each process...
--------------------------------------------
Process ID: 4713
-Xms: -Xms64m (Initial heap size for the JVM)
-Xmx: -Xmx64m (Maximum heap size for the JVM)
Actual memory usage: 136.578 MB
```

### 0.1% memory and CPU

Stop the worker and start it again with 0.1% of memory and CPU:
[AccountTransferWorker.java](./core/src/main/java/io/temporal/samples/moneytransfer/AccountTransferWorker.java)

Replace the `ResourceBasedControllerOptions` line
```java
ResourceBasedControllerOptions.newBuilder(0.001, 0.001).build())
```

This will set the worker to use 0.1% of memory and CPU.

Start the worker again (ensure your previous worker is not running):
```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker
```

Start 10 more transfers:
```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferRequester
```

This time, the validate activity will slow down under stress and the worker will not be able to handle the 10 transfers. 

`./get_worker_memory_usage.sh` should show memory usage as lower than last time:

```
Checking JVM memory settings for each process...
--------------------------------------------
Process ID: 5550
-Xms: -Xms64m (Initial heap size for the JVM)
-Xmx: -Xmx64m (Maximum heap size for the JVM)
Actual memory usage: 106.297 MB
```

Compare this to the 136MB used from the previous run.

In my test on my M2 Macbook, it took 5+ minutes for the validation activity attempt to complete in each transfer before succeeding on 2nd attempt.

## Set JVM memory limits (unused)
_Default is 64mb and should be fine for the above test_

If you find that your worker is running with the same performance in both 70% and 0.1% memory/CPU scenarios above, then explicitly configure your workers to use these Xms and Xmx options:
```bash
./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker \
-Dorg.gradle.jvmargs="-Xms64m -Xmx64m"
```
This means that your JVM will be started with 64mb amount of memory and will be able to use a maximum of 64mb amount of memory.