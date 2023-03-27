package org.demo.health.monitoring;

import org.demo.health.monitoring.keystore.CsvGenerator;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public ModelNode getSocketInfo(){
        ModelNode op = new ModelNode();
        op.get("operation").set("query");
        ModelNode address = op.get("address");

        address.add("socket-binding-group","standard-sockets");
        address.add("socket-binding","*");
        op.get("select").add("name").add("bound-port").add("bound-address");
        op.get("where").add("bound","true");
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public void execute(){
        try {
            //System.out.println(client.execute(getWorkerThreadInfo()).get("result").toJSONString(Boolean.TRUE));
            System.out.println(client.execute(getSocketInfo()).get("result").toJSONString(true));
            CsvGenerator.getInstance().generateReport(client.execute(getWorkerThreadInfo()).get("result").toJSONString(Boolean.TRUE),"Thread");
            //CsvGenerator.getInstance().generateReport(client.execute(getSocketInfo()).get("result").toJSONString(Boolean.TRUE),"Socket");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
