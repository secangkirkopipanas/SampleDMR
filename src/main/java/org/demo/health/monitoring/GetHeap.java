package org.demo.health.monitoring;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.File;
import java.io.IOException;

public class GetHeap {
    private  ModelControllerClient client;
    public GetHeap(ModelControllerClient client){
        this.client = client;
    }
    public ModelNode getHeapUtilization(){

        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");

        address.add("core-service","platform-mbean");
        address.add("type","memory");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getOSLoad(){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");

        address.add("core-service","platform-mbean");
        address.add("type","operating-system");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getRuntimeData(){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");

        address.add("core-service","platform-mbean");
        address.add("type","runtime");

        op.get("recursive").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }
    public void getFileUtilization(String path){
        File file = new File(path);
        System.out.println(String.format("Total space: %.2f GB", (double)file.getTotalSpace() /1073741824));
        System.out.println(String.format("Free space: %.2f GB", (double)file.getFreeSpace() /1073741824));
        System.out.println(String.format("Usable space: %.2f GB",(double)file.getUsableSpace() /1073741824));
    }

    public void execute(){
        try {
            System.out.println("{ OS Details: "+client.execute(getOSLoad()).get("result").toJSONString(Boolean.TRUE)+" }");
            System.out.println("{Heap Utilization: "+client.execute(getHeapUtilization()).get("result").get("heap-memory-usage").toJSONString(Boolean.TRUE) +" }");
            System.out.println("{Non Heap Utilization: "+ client.execute(getHeapUtilization()).get("result").get("non-heap-memory-usage").toJSONString(Boolean.TRUE)+" }");
            String path = client.execute(getRuntimeData()).get("result").get("system-properties").get("jboss.home.dir").toString();
            getFileUtilization(path);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
