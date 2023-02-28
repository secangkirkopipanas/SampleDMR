#!/usr/bin/env bash

PID=""
generate_dump() {
  echo "Dump will be generated"
  LOOP=5
  INTERVAL=10
  #echo "PID: $PID"
  jmap -dump:live,format=b,file=$PID.hprof $PID
  for ((i = 1; i < $LOOP; i++)); do
    jstack -l $PID >> thread_dump$PID.txt
    if [ $i -lt $LOOP ]; then
      echo "Sleeping..."
      sleep $INTERVAL
    fi
  done
}

if [ $# -eq 1 ]; then
  PID=$1
  generate_dump
fi
