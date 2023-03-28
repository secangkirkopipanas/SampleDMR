package org.jboss.health.execute;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;

public class ConfigureTLS {
    private ModelControllerClient client;

    public ConfigureTLS(ModelControllerClient client) {
        this.client = client;
    }

    //Method to remove existing https-listener in undertow
    public ModelNode removeHttpsListener() {

        ModelNode op = new ModelNode();
        op.get("operation").set("remove");

        ModelNode address = op.get("address");

        address.add("subsystem", "undertow");
        address.add("server", "default-server");
        address.add("https-listener", "https");

        op.get("operations").set(Boolean.TRUE);

        return op;
    }

    //Method to configure https-listener in undertow
    public ModelNode createHttpsListener(String sslctx) {

        ModelNode op = new ModelNode();
        op.get("operation").set("add");

        ModelNode address = op.get("address");

        address.add("subsystem", "undertow");
        address.add("server", "default-server");
        address.add("https-listener", "https");

        op.get("socket-binding").set("https");
        op.get("ssl-context").set(sslctx);
        op.get("enable-http2").set(Boolean.TRUE);
        op.get("enabled").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);

        return op;
    }

    public void execute(){
        try {
            System.out.println(client.execute(removeHttpsListener()).toString());
            System.out.println(client.execute(createHttpsListener("ssl-ctx")).toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
