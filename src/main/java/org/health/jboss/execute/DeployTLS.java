package org.health.jboss.execute;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;

public class DeployTLS {

    private ModelControllerClient client;

    public DeployTLS(ModelControllerClient client) {
        this.client = client;
    }

    public ModelNode createKeyStore(String keystore, String storepass) {

        ModelNode op = new ModelNode();
        op.get("operation").set("add");

        ModelNode address = op.get("address");

        address.add("subsystem", "elytron");
        address.add("key-store", "key-store");

        op.get("type").set("JKS");
        op.get("path").set(keystore);
        ModelNode cred = op.get("credential-reference");
        cred.get("clear-text").set(storepass);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode createKeyManager(String keystore, String storepass) {

        ModelNode op = new ModelNode();
        op.get("operation").set("add");

        ModelNode address = op.get("address");

        address.add("subsystem", "elytron");
        address.add("key-manager", "key-manager");

        op.get("key-store").set(keystore);
        ModelNode cred = op.get("credential-reference");
        cred.get("clear-text").set(storepass);
        op.get("operations").set(Boolean.TRUE);
        return op;
    }

    public ModelNode createSSLContext(String keymanager) {

        ModelNode op = new ModelNode();
        op.get("operation").set("add");

        ModelNode address = op.get("address");

        address.add("subsystem", "elytron");
        address.add("server-ssl-context", "ssl-ctx");

        op.get("key-manager").set(keymanager);
        op.get("operations").set(Boolean.TRUE);

        return op;
    }

    public void execute(String keystore, String storepass) {
        try {
            System.out.println(client.execute(createKeyStore(keystore,storepass)).toString());
            System.out.println(client.execute(createKeyManager("key-store",storepass)).toString());
            System.out.println(client.execute(createSSLContext("key-manager")).toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
