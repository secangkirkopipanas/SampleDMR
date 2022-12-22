package org.demo.health.monitoring;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class GetConnPool {
    private ModelControllerClient client;
    public GetConnPool(ModelControllerClient client){
        this.client = client;
    }

    public ModelNode getDataSourceCount(){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-children-names");
        op.get("child-type").set("data-source");

        ModelNode address = op.get("address");

        address.add("subsystem","datasources");
        //address.add("data-source", "KeycloakDS");
        //address.add("type","memory");

        //op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode isStat(String dsname){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-attribute");
        op.get("name").set("statistics-enabled");

        ModelNode address = op.get("address");

        address.add("subsystem","datasources");
        address.add("data-source", dsname);
        //address.add("type","memory");

        op.get("resolve-expressions").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getStatistics(String dsname){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        //op.get("child-type").set("data-source");

        ModelNode address = op.get("address");

        address.add("subsystem","datasources");
        address.add("data-source", dsname);
        address.add("statistics", "pool");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode testConnection(String dsname){
        ModelNode op = new ModelNode();
        op.get("operation").set("test-connection-in-pool");
        //op.get("child-type").set("data-source");

        ModelNode address = op.get("address");

        address.add("subsystem","datasources");
        address.add("data-source", dsname);
        //address.add("statistics", "pool");

        //op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public void execute(){
        try {
            List<ModelNode> dsList = client.execute(getDataSourceCount()).get("result").asList();
            for (int i =0; i<dsList.size();i++){
                String dsname = dsList.get(i).toString().replaceAll("\"","");
                System.out.println("{Test Connection Status: ["+dsname+": "+ client.execute(testConnection(dsname)).get("result").asBoolean()+"]}");
                if(client.execute(isStat(dsname)).get("result").asBoolean()){
                    //System.out.println("Connection Pool Statistics of "+ dsname);
                    System.out.println("{Statics "+dsname+ ": "+client.execute(getStatistics(dsname)).get("result").toJSONString(Boolean.TRUE)+" }");
                }else{
                    System.out.println("{Statistics is not enabled: "+dsname+"}");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
