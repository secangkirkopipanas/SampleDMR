package org.jboss.health.osops;

import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.lang.management.ManagementFactory;

public class GetOSInfo {
    private static GetOSInfo getOSInfo = null;

    private GetOSInfo() {
        //System.out.println("Initialized "+this.getClass().getName());
    }

    public void getFileUtilization() {
        //System.out.println(System.getenv("JBOSS_PARTITION"));
        String file = System.getenv("JBOSS_PARTITION");
        File aDrive = new File(file);
        System.out.println("Partition : " + aDrive.getPath());
        System.out.println(String.format("Total Storage: %.2f GB", (double) aDrive.getTotalSpace() / 1073741824));
        System.out.println(String.format("Free Storage : %.2f GB", (double) aDrive.getFreeSpace() / 1073741824));
        System.out.println(String.format("Usable Storage : %.2f GB", (double) aDrive.getUsableSpace() / 1073741824));
    }

    public void getCpuUtilization() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        System.out.println("Operating System: " + osBean.getName());
        System.out.println("Available Processor: " + osBean.getAvailableProcessors());
        System.out.println("OS Version: " + osBean.getVersion());
        System.out.println("System CPU Load: " + osBean.getSystemCpuLoad());
        System.out.println("Load Average: " + osBean.getSystemLoadAverage());
        System.out.println(String.format("Free Physical Memory : %.2f GB", (double) osBean.getFreePhysicalMemorySize() / 1073741824));
        System.out.println(String.format("Total Physical Memory : %.2f GB", (double) osBean.getTotalPhysicalMemorySize() / 1073741824));
        System.out.println(String.format("Total Swap Memory: %.2f GB", (double) osBean.getTotalSwapSpaceSize() / 1073741824));
        System.out.println(String.format("Free Swap Memory: %.2f GB", (double) osBean.getFreeSwapSpaceSize() / 1073741824));
    }

    public static GetOSInfo getInstance() {
        if (getOSInfo == null) {
            getOSInfo = new GetOSInfo();
        }
        return getOSInfo;
    }
}
