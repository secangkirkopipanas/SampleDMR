package org.demo.health.monitoring;

import java.io.IOException;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author sidde
 */
public class GetHeap {

    public static void main(String[] args) {
        try {
            String host = "localhost";
            int port = 9990;
            String user = "admin";
            String password = "secret";

            CommandContext ctx = CommandContextFactory.getInstance().newCommandContext(user, password.toCharArray());
            ctx.connectController(host, port);

            ModelControllerClient client = ctx.getModelControllerClient();
            
            ModelNode batchRequest = getHeapUtilization();
            ModelNode result = client.execute(batchRequest);

            System.out.println(result.toString());
            ctx.disconnectController();
            System.exit(0);
            
        } catch (CommandLineException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ModelNode getHeapUtilization(){
        System.out.println("Getting Heap Utilization...");
        
        ModelNode op = new ModelNode();
        op.get("operation").set("read-resource");
        ModelNode address = op.get("address");
      
        address.add("core-service","platform-mbean");
        address.add("type","memory");
        
        op.get("include-runtime").set(Boolean.TRUE);
        op.get("operations").set(Boolean.TRUE);
        
        return op;
    }

}
