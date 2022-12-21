package org.demo.health.monitoring;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import javax.json.JsonNumber;

/**
 *
 * @author sidde
 */
public class GetHeap {
    private static String host = "localhost";
    private static int port = 9990;
    private static String user = "admin";
    private static String password = "secret";

    public static String parseStringArgument(String option, String[] args, int i) {
        if (i >= args.length) {
            System.out.println("Wrong Usage");
        }
        return args[i];
    }
    public void parse(String[] args){
        boolean more = true;
        int i;
        for (i = 0; i < args.length && more; i++) {
            switch(args[i]) {
                case "-H":
                    this.host = parseStringArgument("-H", args, ++i);
                    break;
                case "-P":
                    this.port = Integer.parseInt(parseStringArgument("-P", args, ++i));
                    break;
                case "-U":
                    this.user = parseStringArgument("-U", args, ++i);
                    break;
                case "-W":
                    this.password = parseStringArgument("-W", args, ++i);
                    break;
                default:
                    more = false;
                    i--;
            }
        }
    }

    public static void main(String[] args) {
        new GetHeap().parse(args);
        try {
            CommandContext ctx = CommandContextFactory.getInstance().newCommandContext(user, password.toCharArray());
            ctx.connectController(host, port);

            ModelControllerClient client = ctx.getModelControllerClient();
            
            ModelNode batchRequest = getHeapUtilization();
            ModelNode result = client.execute(batchRequest);

            //System.out.println(result.toString());
            System.out.println(result.toJSONString(true));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result.toJSONString(true));
            System.out.println(jsonNode.get("result").get("heap-memory-usage"));
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
