package org.health.tomcat;

import com.opencsv.CSVWriter;
import com.sun.management.OperatingSystemMXBean;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import sun.tools.jconsole.LocalVirtualMachine;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sun.tools.attach.VirtualMachine.attach;

public class JmxMBeanServerConnection {
    private static MBeanServerConnection mBeanServerConnection = null;
    private static JmxMBeanServerConnection jmxMBeanServerConnection = null;

    private JmxMBeanServerConnection() {
    }

    public static JmxMBeanServerConnection getInstance() {
        if (jmxMBeanServerConnection == null) {
            jmxMBeanServerConnection = new JmxMBeanServerConnection();
        }
        return jmxMBeanServerConnection;
    }

    private void getmBeanServerConnection(String pid) {
        if (mBeanServerConnection == null) {
            try {
                VirtualMachine vm = attach(pid);
                String jmxUrl = vm.startLocalManagementAgent();
                JMXServiceURL url = new JMXServiceURL(jmxUrl);
                JMXConnector connector = JMXConnectorFactory.connect(url);
                mBeanServerConnection = connector.getMBeanServerConnection();
            } catch (IOException | AttachNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    private void queryObject(String mbean, String attribute) {
//        try {
//            ObjectName objectName = new ObjectName(mbean);
//            Set<ObjectInstance> beans = mBeanServerConnection.queryMBeans(objectName, null);
//            for (ObjectInstance bean : beans) {
//                getMbean(bean.getObjectName(), attribute);
//            }
//        } catch (MalformedObjectNameException | IOException e) {
//            e.printStackTrace();
//        }
//    }


    private Set<ObjectInstance> queryMbean(String mbean) {
        try {
            ObjectName objectName = new ObjectName(mbean);
            return mBeanServerConnection.queryMBeans(objectName, null);
        } catch (MalformedObjectNameException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    private void getMbean(ObjectName jmxobject, String attribute) {
//        try {
//            if (attribute.equals("port")) {
//                Object value = mBeanServerConnection.getAttribute(jmxobject, attribute);
//                System.out.printf("\n%s : %s", attribute, value);
//                Boolean secure = (Boolean) mBeanServerConnection.getAttribute(jmxobject, "secure");
//                System.out.printf("\nSecure: %b\n", secure);
//                if (secure) {
//                    String cipher = (String) mBeanServerConnection.getAttribute(jmxobject, "ciphers");
//                    System.out.printf("\nCiphers: %s\n", cipher);
//                    Inet4Address addr = (Inet4Address) mBeanServerConnection.getAttribute(jmxobject, "address");
//                    if (!addr.toString().isEmpty()) {
//                        System.out.printf("\nAddress: %s\n", addr.getHostAddress());
//                        SSLFactoryClient.getInstance(addr.getHostAddress(), value.toString()).printSSLDetails();
//                    }
//                }
//            } else {
//                Object value = mBeanServerConnection.getAttribute(jmxobject, attribute);
//                System.out.printf("\n%s : %s\n", attribute, value);
//            }
//        } catch (IOException | MBeanException | AttributeNotFoundException | InstanceNotFoundException |
//                 ReflectionException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private String[] getMemory() {
        try {
            MemoryMXBean memoryMXBeanProxy = JMX.newMXBeanProxy(
                    mBeanServerConnection, new ObjectName("java.lang:type=Memory"), MemoryMXBean.class);
            MemoryUsage heapUsage = memoryMXBeanProxy.getHeapMemoryUsage();
            int heapused = (int) heapUsage.getUsed() / 1024;
            int heapmax = (int) heapUsage.getMax() / 1024;
            int heapcommit = (int) heapUsage.getCommitted() / 1024;
            float heaputil = (float) heapused / heapmax * 100;
            System.out.printf("Heap Utilization: %2.02f%s%n", heaputil, "%");

            System.out.printf(
                    "\tUsed: %dKB\n" +
                            "\tCommitted: %dKB\n" +
                            "\tMax: %dKB\n", heapused, heapcommit, heapmax);

            return new String[]{String.valueOf(heapused), String.valueOf(heapcommit), String.valueOf(heapmax), String.format("%2.02f%s", heaputil, "%")};

        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getCPU() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        System.out.printf("\n%12s %20s %20s %20s %20s\n", "CpuArchitecture", "CpuNumber", "PhysicalMemory",
                "SwapMemory", "OSName", "OSVersion");
        System.out.printf("%12s %20s %20s %20s %20s\n", osBean.getArch(), osBean.getAvailableProcessors(),
                osBean.getTotalPhysicalMemorySize(), osBean.getTotalSwapSpaceSize(), osBean.getName(),
                osBean.getVersion());
        return new String[]{osBean.getArch(), String.valueOf(osBean.getAvailableProcessors()),
                String.valueOf(osBean.getFreePhysicalMemorySize()), String.valueOf(osBean.getTotalSwapSpaceSize()),
                osBean.getName(), osBean.getVersion()};
    }

    private List<String[]> getDisk() {

        List<String[]> diskInfo = new ArrayList<>();
        //diskInfo.add(new String[]{"Total Size", "Used", "Availble", "Utilization", "FileStoreName", "MountPath"});
        FileSystems.getDefault().getFileStores().forEach(
                fileStore -> {
                    try {
                        String path = fileStore.toString().replace(" (" + fileStore.name() + ")", "");
                        long total = fileStore.getTotalSpace() / 1024;
                        long used = (fileStore.getTotalSpace() - fileStore.getUnallocatedSpace()) / 1024;
                        long avail = fileStore.getUsableSpace() / 1024;
                        float utilization = (float) used / total * 100;
                        //diskInfo.add(new String[] {String.valueOf(total), String.valueOf(used), String.valueOf(avail), String.valueOf(utilization), fileStore.name(), path});
                        if ((int) Math.round(utilization) >= 80) {
                            System.out.format("%12d %12d %12d %10.02f %30s %-20s %n", total, used, avail, utilization, fileStore.name(), path);
                            diskInfo.add(new String[]{String.valueOf(total), String.valueOf(used), String.valueOf(avail), String.valueOf(utilization), fileStore.name(), path});
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return diskInfo;
    }

    private Map<String, String[]> getMBean(String mbeanName, String[] properties) {
        final AtomicBoolean header = new AtomicBoolean(false);
        final Map<String, String[]> mbeanMap = new HashMap<>();

        queryMbean(mbeanName).forEach(
                objectInstance -> {
                    try {
                        AttributeList attributeList = mBeanServerConnection.getAttributes(objectInstance.getObjectName(), properties);

                        AtomicInteger idx = new AtomicInteger();
                        String[] keys = new String[attributeList.size()];
                        String[] values = new String[attributeList.size()];
                        attributeList.asList().forEach(
                                attribute -> {
                                    keys[idx.get()] = String.valueOf(attribute.getName());
                                    values[idx.get()] = String.valueOf(attribute.getValue());
                                    idx.incrementAndGet();
                                }
                        );

                        if (!header.get()) {
                            mbeanMap.put("key", keys);
                            header.set(true);
                        }

                        mbeanMap.put("value", values);

                    } catch (InstanceNotFoundException |
                             ReflectionException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        return mbeanMap;
    }

    public void execute(String csvFilePath) {
        try {
            FileWriter fileWriter = new FileWriter(csvFilePath);
            CSVWriter csvWriter = new CSVWriter(fileWriter);

            Map<Integer, LocalVirtualMachine> vmlist = LocalVirtualMachine.getAllVirtualMachines();
            vmlist.forEach(
                    (integer, localVirtualMachine) -> {
                        if (localVirtualMachine.displayName().contains("org.apache.catalina.startup.Bootstrap")) {
                            getmBeanServerConnection(String.valueOf(localVirtualMachine.vmid()));

                            List<String[]> records = new ArrayList<>();

                            // Memory info
                            records.add(new String[] {
                                    "Heap Used", "Heap Commit", "Heap Max", "Heap Utilization"
                            });
                            records.add(getMemory());
                            records.add(new String[] {});

                            // CPU info
                            records.add(new String[] {
                                    "CPU Arch", "CPU Number", "Physical Memory", "Swap Memory", "OS Name", "OS Version"
                            });
                            records.add(getCPU());
                            records.add(new String[] {});

                            // Disk info
                            records.add(new String[] {
                                    "Total Size", "Used", "Available", "Utilization", "Filestore Name", "Mount Path"
                            });
                            records.addAll(getDisk());
                            records.add(new String[] {});

                            // Server info
                            Map<String, String[]> serverInfoMap = getMBean("Catalina:type=Server", new String[] {
                                    "serverInfo", "serverNumber"
                            });
                            records.add(new String[] {
                                    "Server Info", "Server Number"
                            });
                            records.add(serverInfoMap.get("value"));
                            records.add(new String[] {});

                            // Runtime info
                            Map<String, String[]> runtimeInfoMap = getMBean("java.lang:type=Runtime", new String[] {
                                    "VmName", "VmVersion", "VmVendor", "Uptime"
                            });
                            records.add(new String[] {
                                    "VM Name", "VM Version", "VM Vendor", "Uptime"
                            });
                            records.add(runtimeInfoMap.get("value"));
                            records.add(new String[] {});

                            // Connector (port) info
                            Map<String, String[]> connectorInfoMap = getMBean("Catalina:type=Connector,*", new String[] {
                                    "address", "port", "scheme", "secure", "stateName"
                            });
                            records.add(new String[] {
                                    "Address", "Port", "Scheme", "Secured", "State Name"
                            });
                            records.add(connectorInfoMap.get("value"));

                            csvWriter.writeAll(records);
                        }
                    }
            );
            csvWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}


