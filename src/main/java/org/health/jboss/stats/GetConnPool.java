package org.health.jboss.stats;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.health.report.CsvGenerator;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GetConnPool {
    private ModelControllerClient client;

    public GetConnPool(ModelControllerClient client) {
        this.client = client;
    }

    public ModelNode getDataSourceCount(String dstype) {
        ModelNode op = new ModelNode();
        op.get("operation").set("read-children-names");
        op.get("child-type").set(dstype);

        ModelNode address = op.get("address");

        address.add("subsystem", "datasources");
        //address.add("data-source", "KeycloakDS");
        //address.add("type","memory");

        //op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode isStat(String dsname) {
        ModelNode op = new ModelNode();
        op.get("operation").set("read-attribute");
        op.get("name").set("statistics-enabled");

        ModelNode address = op.get("address");

        address.add("subsystem", "datasources");
        address.add("data-source", dsname);
        //address.add("type","memory");

        op.get("resolve-expressions").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getStatistics(String dsname, String dstype) {
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        //op.get("child-type").set("data-source");

        ModelNode address = op.get("address");

        address.add("subsystem", "datasources");
        address.add(dstype, dsname);
        address.add("statistics", "pool");

        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode getJndiName(String dsname, String dstype) {
        ModelNode op = new ModelNode();
        op.get("operation").set("read-attribute");

        ModelNode address = op.get("address");

        address.add("subsystem", "datasources");
        address.add(dstype, dsname);
        //address.add("statistics", "pool");

        op.get("name").set("jndi-name");
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode testConnection(String dsname, String dstype) {
        ModelNode op = new ModelNode();
        op.get("operation").set("test-connection-in-pool");
        //op.get("child-type").set("data-source");

        ModelNode address = op.get("address");

        address.add("subsystem", "datasources");
        address.add(dstype, dsname);
        //address.add("statistics", "pool");

        //op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public void execute() {
        List<String> dstypes = Arrays.asList("data-source", "xa-data-source");
        for(String dstype:dstypes){
            try {
                client.execute(getDataSourceCount(dstype)).get("result").asList().stream().forEach(
                        modelNode -> {
                            String dsname = modelNode.toString().replaceAll("\"","");
                            try {
                                String jndiName = client.execute(getJndiName(dsname, dstype)).get("result").toString();
                                Boolean testConResult = client.execute(testConnection(dsname, dstype)).get("result").asBoolean(false);
                                Boolean statEnabled = client.execute(isStat(dsname)).get("result").asBoolean(false);
                                String str = String.format("{\"JndiName\": %s, \"ConnectionStatus\": %s}",jndiName,testConResult);
                                System.out.println(str);
                                CsvGenerator.getInstance().generateReport(str,"ConnectionStatus");
                                if(testConResult && statEnabled){
                                    String stat_str = client.execute(getStatistics(dsname, dstype)).get("result").toJSONString(Boolean.TRUE);
                                    CsvGenerator.getInstance().generateReport(stat_str,"Statistics");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
