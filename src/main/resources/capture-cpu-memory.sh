#!/usr/bin/env bash
PID=""
capture_openfile() {
  COUNT=$(lsof -p $PID | wc -l)
  echo "Open files: $COUNT"
}
capture_cpu() {
  UTIZ=$(mpstat | awk '{print $NF}' | tail -1 | cut -f 1 -d ".")
  if [ $UTIZ -lt 20 ]; then
    #top -b -n 1 -H -p $i
    for i in $(ps -eo pcpu,pid,user,args | grep java | awk 'NF==1 {PS=$0} NF>1 && $1 > 80 {print $2}'); do
      PID=$i
      capture_openfile
      top -b -n 1 -H -p $PID
    done
  fi
}
capture_disk() {
  df -m | awk 'NF==1 {fs=$0} NF>1 && $(NF-1)+0 > 80 {print fs"\n"$0}'
}

# shellcheck disable=SC2170
capture_disk
capture_cpu
