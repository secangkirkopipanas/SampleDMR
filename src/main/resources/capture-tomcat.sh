#!/bin/bash
#Script to capture Tomcat statistics

capture_jvm_version(){
   echo "--------------------------------------------------------------------------------------------"
   echo -e "\t\t\tJVM Version"
   echo "--------------------------------------------------------------------------------------------"
   jcmd $PID VM.version| sed '1d'
   echo "--------------------------------------------------------------------------------------------"
}

capture_tomcat_version(){
   echo "--------------------------------------------------------------------------------------------"
   echo -e "\t\t\tTomcat Version"
   echo "--------------------------------------------------------------------------------------------"
   EXEC=$BASE_LOCATION/bin/version.sh
   echo $EXEC
   if  [[ ! -z $EXEC ]]
   then
     sh $EXEC
   fi
   echo "--------------------------------------------------------------------------------------------"
}

capture_heap(){
  #https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jstat.html
   echo "--------------------------------------------------------------------------------------------"
   echo -e "\t\t\tHeap Utilization"
   echo "--------------------------------------------------------------------------------------------"
   jstat -gcutil $PID
   echo "--------------------------------------------------------------------------------------------"
}

gc_statistics(){
   echo "--------------------------------------------------------------------------------------------"
   echo -e "\t\t\tGC statistics"
   echo "--------------------------------------------------------------------------------------------"
   jcmd $PID GC.heap_info | sed '1d'
   echo "--------------------------------------------------------------------------------------------"
}



capture_openfiles(){
   echo "--------------------------------------------------------------------------------------------"
   COUNT=$(lsof -p $PID -w| wc -l)
   echo "Open files: $COUNT"
   echo "--------------------------------------------------------------------------------------------"
}

capture_memory_cpu(){
   echo "--------------------------------------------------------------------------------------------"
   echo -e "\t\t\tCPU & Memory Utilization"
   echo "--------------------------------------------------------------------------------------------"
   ps -p $PID -o pcpu,size,%mem,pid,user,etime,args,comm
   echo "--------------------------------------------------------------------------------------------"
}
capture_filesystem(){
   echo "--------------------------------------------------------------------------------------------"
   echo -e "\t\t\tFile System Utilization"
   echo "--------------------------------------------------------------------------------------------"
   #df -h $BASE_LOCATION
   df -m | awk 'NF==1 {fs=$0} NF>1 && $(NF-1)+0 > 80 {print fs"\n"$0}'
   echo "--------------------------------------------------------------------------------------------"
}



#Main
#TOMCAT_PROC=`jps -lvm | grep org.apache.catalina.startup.Bootstrap`
PID=$1
if [[ ! -z $PID ]]
then
  BASE_LOCATION=`jcmd $PID VM.system_properties | grep -i catalina.home= | awk -F "=" '{print $NF}'`
  capture_jvm_version
  capture_tomcat_version
  capture_heap
  gc_statistics
  capture_openfiles
  capture_memory_cpu
  capture_filesystem
fi