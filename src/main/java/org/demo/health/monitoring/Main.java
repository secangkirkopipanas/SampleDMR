package org.demo.health.monitoring;

import org.jboss.as.controller.client.ModelControllerClient;

/**
 * @author sidde
 */
public class Main {
    private static String host = "localhost";
    private static int port = 9990;
    private static String user = "admin";
    private static String password = "secret";

    public String parseStringArgument(String option, String[] args, int i) {
        if (i >= args.length) {
            System.out.println("Wrong Usage");
        }
        return args[i];
    }

    public void parse(String[] args) {
        boolean more = true;
        int i;
        for (i = 0; i < args.length && more; i++) {
            switch (args[i]) {
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
        new Main().parse(args);
        ExecuteDMR dmr = new ExecuteDMR(host,port,user,password);
        ModelControllerClient client = dmr.getClientInstance();
        new GetHeap(client).execute();
        new GetConnPool(client).execute();
        new GetThread(client).execute();
    }
}
