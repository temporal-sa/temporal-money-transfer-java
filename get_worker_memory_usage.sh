#!/bin/bash

# Step 1: Get all processes matching the specific Gradle command
PIDS=$(pgrep -f 'org.gradle.wrapper.GradleWrapperMain -q execute -PmainClass=io.temporal.samples.moneytransfer.AccountTransferWorker --console=plain')

if [ -z "$PIDS" ]; then
  echo "No matching processes found."
  exit 1
fi

# Step 2: For each PID, print the PID, -Xmx and -Xms values, and explanation text
echo "Checking JVM memory settings for each process..."

for PID in $PIDS; do
  echo "--------------------------------------------"
  echo "Process ID: $PID"

  # Extract the -Xmx and -Xms values using ps and sed
  XMX=$(ps -p $PID -o args= | sed -n 's/.*\(-Xmx[^ ]*\).*/\1/p')
  XMS=$(ps -p $PID -o args= | sed -n 's/.*\(-Xms[^ ]*\).*/\1/p')

  if [ -n "$XMS" ]; then
    echo "-Xms: $XMS (Initial heap size for the JVM)"
  else
    echo "-Xms: Not set"
  fi

  # Print the values with explanation
  if [ -n "$XMX" ]; then
    echo "-Xmx: $XMX (Maximum heap size for the JVM)"
  else
    echo "-Xmx: Not set"
  fi

  # Step 3: Report the actual memory usage in megabytes
  MEM_USAGE=$(ps -p $PID -o rss= | awk '{print $1/1024 " MB"}')
  echo "Actual memory usage: $MEM_USAGE"

done
