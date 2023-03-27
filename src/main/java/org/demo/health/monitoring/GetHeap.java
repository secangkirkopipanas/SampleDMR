package org.demo.health.monitoring;

import com.sun.management.OperatingSystemMXBean;
import org.demo.health.monitoring.keystore.CsvGenerator;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class GetHeap {
    private ModelControllerClient client;

    public GetHeap(ModelControllerClient client) {
        this.client = client;
    }

    public ModelNode getHeapUtilization() {

        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");

        address.add("core-service", "platform-mbean");
        address.add("type", "memory");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getOSLoad() {
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");

        address.add("core-service", "platform-mbean");
        address.add("type", "operating-system");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getRuntimeData() {
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");

        address.add("core-service", "platform-mbean");
        address.add("type", "runtime");

        op.get("recursive").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public void execute() {
        try {
            //System.out.println("{ OS Details: " + client.execute(getOSLoad()).get("result").toJSONString(Boolean.TRUE) + " }");
            //System.out.println("{Heap Utilization: " + client.execute(getHeapUtilization()).get("result").get("heap-memory-usage").toJSONString(Boolean.TRUE) + " }");
            //System.out.println("{Non Heap Utilization: " + client.execute(getHeapUtilization()).get("result").get("non-heap-memory-usage").toJSONString(Boolean.TRUE) + " }");
            //System.out.println(client.execute(getRuntimeData()).get("result").get("system-properties").get("jboss.home.dir").toString());
            CsvGenerator.getInstance().generateReport(client.execute(getOSLoad()).get("result").toJSONString(true),"OsInfo");
            CsvGenerator.getInstance().generateReport(client.execute(getHeapUtilization()).get("result").get("heap-memory-usage").toJSONString(true),"HeapUtilization");
            CsvGenerator.getInstance().generateReport(client.execute(getHeapUtilization()).get("result").get("non-heap-memory-usage").toJSONString(true),"NonHeapUtilization");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
