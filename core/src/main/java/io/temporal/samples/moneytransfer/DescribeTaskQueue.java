package io.temporal.samples.moneytransfer;

import static io.temporal.samples.moneytransfer.TemporalClient.getWorkflowServiceStubs;

import io.temporal.api.enums.v1.TaskQueueKind;
import io.temporal.api.enums.v1.TaskQueueType;
import io.temporal.api.taskqueue.v1.TaskQueue;
import io.temporal.api.workflowservice.v1.DescribeTaskQueueRequest;
import io.temporal.api.workflowservice.v1.DescribeTaskQueueResponse;
import io.temporal.samples.moneytransfer.web.ServerInfo;
import io.temporal.serviceclient.WorkflowServiceStubs;
import java.io.FileNotFoundException;
import javax.net.ssl.SSLException;

public class DescribeTaskQueue {
  public static DescribeTaskQueueResponse getTaskQueueInfo()
      throws FileNotFoundException, SSLException {
    WorkflowServiceStubs service = getWorkflowServiceStubs();

    DescribeTaskQueueResponse res =
        service
            .blockingStub()
            .describeTaskQueue(
                DescribeTaskQueueRequest.newBuilder()
                    .setTaskQueue(
                        TaskQueue.newBuilder()
                            .setKind(TaskQueueKind.TASK_QUEUE_KIND_NORMAL)
                            .setName(ServerInfo.getTaskqueue())
                            .build())
                    .setTaskQueueType(TaskQueueType.TASK_QUEUE_TYPE_WORKFLOW)
                    .setNamespace(ServerInfo.getNamespace())
                    .setIncludeTaskQueueStatus(true)
                    .build());
    return res;
  }

  public static void main(String[] args) throws FileNotFoundException, SSLException {
    DescribeTaskQueueResponse res = getTaskQueueInfo();

    // https://docs.temporal.io/cli/task-queue#describe
    // Workers are removed if 5 minutes have passed since the last poll request.
    System.out.println("Workflow Task Pollers: " + res.getPollersCount());
  }
}
