package org.jboss.health.jmx;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import javax.management.*;
import javax.management.openmbean.TabularDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.sun.tools.attach.VirtualMachine.attach;

public class JmxMBeanServerConnection {
    private static MBeanServerConnection mBeanServerConnection = null;
    private static JmxMBeanServerConnection jmxMBeanServerConnection = null;

    private JmxMBeanServerConnection() {
    }

    public static JmxMBeanServerConnection getInstance(String pid) {
        if (jmxMBeanServerConnection == null) {
            jmxMBeanServerConnection = new JmxMBeanServerConnection(pid);
        }
        return jmxMBeanServerConnection;
    }

    private JmxMBeanServerConnection(String pid) {
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

    public Object queryObject(String mbean, String attribute) {
        try {
            ObjectName objectName = new ObjectName(mbean);
            Set<ObjectInstance> beans = mBeanServerConnection.queryMBeans(objectName, null);
            for (ObjectInstance bean : beans) {
                return getMbean(bean.getObjectName(), attribute);
            }
        } catch (MalformedObjectNameException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getMbean(ObjectName jmxobject, String attribute) {
        try {
            Object value = mBeanServerConnection.getAttribute(jmxobject, attribute);
            //System.out.printf("\n%s : %s", attribute, value);
            return value;
        } catch (IOException | MBeanException |
                 AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        TabularDataSupport tds = (TabularDataSupport) getInstance("23330").queryObject("java.lang:type=Runtime", "SystemProperties");
        Set<Map.Entry<Object,Object>> es = tds.entrySet();
        //System.out.println(es);
    }
}
