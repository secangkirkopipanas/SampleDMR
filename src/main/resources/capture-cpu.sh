#!/bin/bash
PID=""
DUMP=false
generate_dump() {
  if $DUMP; then
    echo "Dump will be generated"
    LOOP=5
    INTERVAL=10
    #echo "PID: $PID"
    jmap -dump:live,format=b,file=dump.hprof $PID
    for ((i = 1; i < $LOOP; i++)); do
      jstack -l $PID
      if [ $i -lt $LOOP ]; then
        echo "Sleeping..."
        sleep $INTERVAL
      fi
    done
  fi
}
capture_openfile() {
  prs=$(ps aux | grep $PID | grep -v grep | awk '{print $NF}')
  echo "PID: $PID $prs"
  COUNT=$(lsof -p $PID | wc -l)
  echo "Open files: $COUNT"
}
capture_cpu() {
  for i in $(ps -ef | grep '[j]'boss-modules.jar | awk '{print $2}'); do
    PID=$i
    #Generate Heap and Thread dump
    generate_dump
    #Capture Open files
    capture_openfile
    #Capture CPU
    UTIZ=`mpstat | awk '{print $NF}' | tail -1`
    UTIZ=${UTIZ%.*}
    if [ $UTIZ -lt 20 ]; then
      top -b -n 1 -H -p $i
    fi
  done
}

# shellcheck disable=SC2170
if [[ $# -eq 1 && $1 -eq "generateDump" ]]; then
  DUMP=true
  capture_cpu
else
  capture_cpu
fi
