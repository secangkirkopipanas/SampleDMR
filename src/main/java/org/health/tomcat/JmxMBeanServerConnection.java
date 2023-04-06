package org.health.tomcat;

import com.opencsv.CSVWriter;
import com.sun.management.OperatingSystemMXBean;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.health.jboss.execute.SSLFactoryClient;
import sun.tools.jconsole.LocalVirtualMachine;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.Inet4Address;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private void queryObject(String mbean, String attribute) {
        try {
            ObjectName objectName = new ObjectName(mbean);
            Set<ObjectInstance> beans = mBeanServerConnection.queryMBeans(objectName, null);
            for (ObjectInstance bean : beans) {
                getMbean(bean.getObjectName(), attribute);
            }
        } catch (MalformedObjectNameException | IOException e) {
            e.printStackTrace();
        }
    }


    private Set<ObjectInstance> queryMbean(String mbean) {
        try {
            ObjectName objectName = new ObjectName(mbean);
            Set<ObjectInstance> beans = mBeanServerConnection.queryMBeans(objectName, null);
            return beans;
        } catch (MalformedObjectNameException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getMbean(ObjectName jmxobject, String attribute) {
        try {
            if (attribute.equals("port")) {
                Object value = mBeanServerConnection.getAttribute(jmxobject, attribute);
                System.out.printf("\n%s : %s", attribute, value);
                Boolean secure = (Boolean) mBeanServerConnection.getAttribute(jmxobject, "secure");
                System.out.printf("\nSecure: %b\n", secure);
                if (secure) {
                    String cipher = (String) mBeanServerConnection.getAttribute(jmxobject, "ciphers");
                    System.out.printf("\nCiphers: %s\n", cipher);
                    Inet4Address addr = (Inet4Address) mBeanServerConnection.getAttribute(jmxobject, "address");
                    if (!addr.toString().isEmpty()) {
                        System.out.printf("\nAddress: %s\n", addr.getHostAddress());
                        SSLFactoryClient.getInstance(addr.getHostAddress(), value.toString()).printSSLDetails();
                    }
                }
            } else {
                Object value = mBeanServerConnection.getAttribute(jmxobject, attribute);
                System.out.printf("\n%s : %s\n", attribute, value);
            }
        } catch (IOException | MBeanException | AttributeNotFoundException | InstanceNotFoundException |
                 ReflectionException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getMemory() {
        try {
            MemoryMXBean memoryMXBeanProxy = JMX.newMXBeanProxy(
                    mBeanServerConnection, new ObjectName("java.lang:type=Memory"), MemoryMXBean.class);
            MemoryUsage heapUsage = memoryMXBeanProxy.getHeapMemoryUsage();
            int heapused = (int) heapUsage.getUsed() / 1024;
            int heapmax = (int) heapUsage.getMax() / 1024;
            int heapcommit = (int) heapUsage.getCommitted() / 1024;
            float heaputil = (float) heapused / heapmax * 100;
            System.out.println(String.format("Heap Utilization: %2.02f%s", heaputil, "%"));


            System.out.printf(
                    "\tUsed: %dKB\n" +
                            "\tCommitted: %dKB\n" +
                            "\tMax: %dKB\n", heapused, heapcommit, heapused);

            return new String[]{String.valueOf(heapused), String.valueOf(heapcommit), String.valueOf(heapmax), String.format("%2.02f%s", heaputil, "%")};

        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getCPU() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        System.out.printf("\n%12s %20s %20s %20s %20s\n", "CpuArchitecture", "CpuNumber", "PhysicalMemory", "SwapMemory", "OSVersion");
        System.out.printf("%12s %20s %20s %20s %20s\n", osBean.getArch(), osBean.getAvailableProcessors(), osBean.getTotalPhysicalMemorySize(), osBean.getTotalSwapSpaceSize(), osBean.getVersion());
        return new String[]{osBean.getArch(), String.valueOf(osBean.getAvailableProcessors()), String.valueOf(osBean.getFreePhysicalMemorySize()), String.valueOf(osBean.getTotalSwapSpaceSize()), osBean.getVersion()};
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

    public void execute(String script) {
        try {
            StringWriter writer = new StringWriter();
            FileWriter fileWriter = new FileWriter("report.csv".toString());
            //using custom delimiter and quote character
            CSVWriter csvWriter = new CSVWriter(fileWriter);


            Map<Integer, LocalVirtualMachine> vmlist = LocalVirtualMachine.getAllVirtualMachines();
            vmlist.forEach(
                    (integer, localVirtualMachine) -> {
                        if (localVirtualMachine.displayName().contains("org.apache.catalina.startup.Bootstrap")) {
                            getmBeanServerConnection(String.valueOf(localVirtualMachine.vmid()));
                            List<String[]> temp = new ArrayList<>();
                            temp.add(new String[]{"Heap Used", "Heap Commit", "Heap Max", "Heap Utilization"});
                            temp.add(getMemory());
                            temp.add(new String[]{"CpuArchitecture", "CpuNumber", "PhysicalMemory", "SwapMemory", "OSVersion"});
                            temp.add(getCPU());
                            temp.add(new String[]{"Total Size", "Used", "Availble", "Utilization", "FileStoreName", "MountPath"});
                            temp.addAll(getDisk());


                            //queryObject("Catalina:type=Server", "serverInfo");
                            //queryObject("java.lang:type=Runtime", "VmVersion");
                            //queryObject("Catalina:type=Connector,*", "port");
                            //queryObject("Catalina:type=SSLHostConfig,*", "enabledciphers");
                            //queryObject("java.lang:type=Threading", "ThreadCount");
                            //FileOperation.getInstance().executeScript(script+" "+String.valueOf(localVirtualMachine.vmid()));


                            queryMbean("Catalina:type=Server").forEach(
                                    objectInstance -> {
                                        try {
                                            AttributeList value = mBeanServerConnection.getAttributes(objectInstance.getObjectName(), new String[]{"serverInfo", "serverNumber"});
                                            temp.add(getStringArrary(value.asList(),"key"));
                                            temp.add(getStringArrary(value.asList(),"value"));
                                        } catch (InstanceNotFoundException |
                                                ReflectionException | IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );

                            queryMbean("java.lang:type=Runtime").forEach(
                                    objectInstance -> {
                                        try {
                                            AttributeList value = mBeanServerConnection.getAttributes(objectInstance.getObjectName(), new String[]{"VmName", "VmVersion", "VmVendor", "Uptime"});
                                            temp.add(getStringArrary(value.asList(),"key"));
                                            temp.add(getStringArrary(value.asList(),"value"));
                                        } catch (InstanceNotFoundException |
                                                 ReflectionException | IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );

                            final boolean[] header = { false };
                            queryMbean("Catalina:type=Connector,*").forEach(
                                    objectInstance -> {
                                        try {
                                            AttributeList value = mBeanServerConnection.getAttributes(objectInstance.getObjectName(), new String[]{"address","port","scheme","secure","stateName"});
                                            if (!header[0]) {
                                                temp.add(getStringArrary(value.asList(), "key"));
                                                header[0] = true;
                                            }
                                            temp.add(getStringArrary(value.asList(),"value"));
                                        } catch (InstanceNotFoundException |
                                                 ReflectionException | IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );

                            csvWriter.writeAll(temp);
                        }
                    }
            );
            csvWriter.close();
            System.out.println(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String[] getStringArrary(List<Attribute> attributeList, String type){
        AtomicInteger idx = new AtomicInteger();
        String[] value = new String[attributeList.size()];
        attributeList.stream().forEach(
                attribute -> {
                    if(type.equalsIgnoreCase("Value")) {
                        value[idx.get()] = String.valueOf(attribute.getValue());
                    } else if (type.equalsIgnoreCase("Key")) {
                        value[idx.get()] = String.valueOf(attribute.getName());
                    }

                    idx.incrementAndGet();
                }
        );
        return value;
    }

}


