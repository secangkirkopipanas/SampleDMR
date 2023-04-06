package org.health.jboss.jmx;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

import static com.sun.tools.attach.VirtualMachine.attach;

public class JmxMbeanConnection {
    private static JmxMbeanConnection jmxMbeanConnect = null;
    private static MBeanServerConnection mBeanServerConnection = null;

    public static JmxMbeanConnection getInstance(String pid) {
        if (jmxMbeanConnect == null) {
            jmxMbeanConnect = new JmxMbeanConnection(pid);
        }
        return jmxMbeanConnect;
    }
    private JmxMbeanConnection(String pid){
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
    public MBeanServerConnection getmBeanServerConnection(){
        return mBeanServerConnection;
    }
}
