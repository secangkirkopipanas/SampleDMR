package org.demo.health.monitoring;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;

public class GetThread {
    private ModelControllerClient client;
    public GetThread(ModelControllerClient client){
        this.client = client;
    }

    public ModelNode getWorkerThreadInfo(){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");

        address.add("subsystem","io");
        address.add("worker","default");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public void execute(){
        try {
            System.out.println(client.execute(getWorkerThreadInfo()).get("result").toJSONString(Boolean.TRUE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
