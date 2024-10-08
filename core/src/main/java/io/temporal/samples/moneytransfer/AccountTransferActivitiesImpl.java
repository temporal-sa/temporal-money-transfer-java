/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.temporal.samples.moneytransfer;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.samples.moneytransfer.dataclasses.ChargeResponseObj;
import io.temporal.samples.moneytransfer.dataclasses.ExecutionScenarioObj;
import io.temporal.samples.moneytransfer.web.ServerInfo;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountTransferActivitiesImpl implements AccountTransferActivities {
  private static final Logger log = LoggerFactory.getLogger(AccountTransferActivitiesImpl.class);

  @Override
  public Boolean validate(ExecutionScenarioObj scenario) {
    log.info("\n\nAPI /validate\n");

    if (scenario == ExecutionScenarioObj.HUMAN_IN_LOOP) {
      return false;
    }

    if (scenario == ExecutionScenarioObj.STRESS_TEST) {
      ActivityExecutionContext ctx = Activity.getExecutionContext();
      ActivityInfo info = ctx.getInfo();
      if (info.getAttempt() <= 1) {
        stressCpuAndMemory(50 * 1024 * 1024); // 50 MB in bytes
      }
    }
    return true;
  }

  @Override
  public String withdraw(float amountDollars, ExecutionScenarioObj scenario) {
    log.info("\n\nAPI /withdraw amount = " + amountDollars + " \n");

    ActivityExecutionContext ctx = Activity.getExecutionContext();
    ActivityInfo info = ctx.getInfo();

    if (scenario == ExecutionScenarioObj.API_DOWNTIME) {
      log.info("\n\n*** Simulating API Downtime\n");
      if (info.getAttempt() < 5) {
        log.info("\n*** Activity Attempt: #" + info.getAttempt() + "***\n");
        int delaySeconds = 7;
        log.info("\n\n/API/simulateDelay Seconds" + delaySeconds + "\n");
        simulateDelay(delaySeconds);
      }
    }

    return "SUCCESS";
  }

  @Override
  public ChargeResponseObj deposit(
      String idempotencyKey, float amountDollars, ExecutionScenarioObj scenario) {

    log.info("\n\nAPI /deposit amount = " + amountDollars + " \n");

    if (scenario == ExecutionScenarioObj.INVALID_ACCOUNT) {
      InvalidAccountException invalidAccountException =
          new InvalidAccountException("Invalid Account");
      throw Activity.wrap(invalidAccountException);
    }

    ChargeResponseObj response = new ChargeResponseObj("example-charge-id");

    return response;
  }

  @Override
  public boolean undoWithdraw(float amountDollars) {
    log.info("\n\nAPI /undoWithdraw amount = " + amountDollars + " \n");

    return true;
  }

  private static String simulateDelay(int seconds) {
    String url = ServerInfo.getWebServerURL() + "/simulateDelay?s=" + seconds;
    log.info("\n\n/API/simulateDelay URL: " + url + "\n");
    Request request = new Request.Builder().url(url).build();
    try (Response response = new OkHttpClient().newCall(request).execute()) {
      return response.body().string();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // InvalidAccountException
  public static class InvalidAccountException extends RuntimeException {
    public InvalidAccountException(String message) {
      super(message);
    }
  }

  public static void stressCpuAndMemory(int sizeInBytes) {
    // Allocate a byte array of the specified size
    byte[] memoryChunk = new byte[sizeInBytes];

    long startTime = System.currentTimeMillis();
    long duration = 30 * 1000; // 30 seconds in milliseconds

    // Perform CPU-intensive task for approximately 30 seconds
    while (System.currentTimeMillis() - startTime < duration) {
      // Fill the array with some data to simulate memory usage
      for (int i = 0; i < memoryChunk.length; i++) {
        memoryChunk[i] = (byte) (i % 256);
      }

      // Simple CPU work: sum up all elements in the array
      long sum = 0;
      for (byte b : memoryChunk) {
        sum += b;
      }

      // Print the sum to avoid the compiler optimizing away the loop
      System.out.println("Current sum: " + sum);
    }

    System.out.println("Finished stressing CPU and memory.");
  }
}
