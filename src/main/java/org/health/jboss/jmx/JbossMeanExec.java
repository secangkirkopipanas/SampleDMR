package org.health.jboss.jmx;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Set;

public class JbossMeanExec {

    public void queryObject(String mbean, String attribute, String pid) {
        MBeanServerConnection mBeanServerConnection = JmxMbeanConnection.getInstance(pid).getmBeanServerConnection();
        try {
            ObjectName objectName = new ObjectName(mbean);
            Set<ObjectInstance> beans = mBeanServerConnection.queryMBeans(objectName, null);
            for (ObjectInstance bean : beans) {
                //getMbean(bean.getObjectName(), attribute);
            }
        } catch (MalformedObjectNameException | IOException e) {
            e.printStackTrace();
        }
    }
}
