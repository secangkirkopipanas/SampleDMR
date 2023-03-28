package org.jboss.health.stat;

import org.jboss.health.report.CsvGenerator;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GetConnPool {
    private ModelControllerClient client;
    public GetConnPool(ModelControllerClient client){
        this.client = client;
    }

    public ModelNode getDataSourceCount(String dstype){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-children-names");
        op.get("child-type").set(dstype);

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

    public ModelNode getStatistics(String dsname, String dstype){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        //op.get("child-type").set("data-source");

        ModelNode address = op.get("address");

        address.add("subsystem","datasources");
        address.add(dstype, dsname);
        address.add("statistics", "pool");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getJndiName(String dsname, String dstype){
        ModelNode op = new ModelNode();
        op.get("operation").set("read-attribute");

        ModelNode address = op.get("address");

        address.add("subsystem","datasources");
        address.add(dstype, dsname);
        //address.add("statistics", "pool");

        op.get("name").set("jndi-name");
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode testConnection(String dsname, String dstype){
        ModelNode op = new ModelNode();
        op.get("operation").set("test-connection-in-pool");
        //op.get("child-type").set("data-source");

        ModelNode address = op.get("address");

        address.add("subsystem","datasources");
        address.add(dstype, dsname);
        //address.add("statistics", "pool");

        //op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public void execute(){
        List<String> dstypes = Arrays.asList("data-source","xa-data-source");
        for (String dstype : dstypes) {
            try {
                List<ModelNode> dsList = client.execute(getDataSourceCount(dstype)).get("result").asList();
                if(!dsList.isEmpty()) {
                    for (int i = 0; i < dsList.size(); i++) {
                        String dsname = dsList.get(i).toString().replaceAll("\"", "");
                        String jndiName = client.execute(getJndiName(dsname, dstype)).get("result").toString();
                        client.execute(testConnection(dsname, dstype)).get("result").asBoolean();

                        CsvGenerator.getInstance().generateReport("{\"Jndi Name\":"+jndiName+",\"ConnectionStatus\":"+client.execute(testConnection(dsname,dstype)).get("result").asBoolean()+",\"Statistics Enabled\":" +client.execute(isStat(dsname)).get("result").asBoolean()+"}","ConnectionStatus");

                        if (client.execute(isStat(dsname)).get("result").asBoolean()) {
                            //System.out.println("Connection Pool Statistics of "+ dsname);
                            //System.out.println("{Statics " + jndiName + ": "
                            //        + client.execute(getStatistics(dsname, dstype)).get("result").toJSONString(Boolean.TRUE) + " }");
                            CsvGenerator.getInstance().generateReport(client.execute(getStatistics(dsname, dstype)).get("result").toJSONString(Boolean.TRUE),"Connection Statistics");
                        }
                        /*else {
                            System.out.println("{Statistics is not enabled: " + jndiName + "}");
                        }*/
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
