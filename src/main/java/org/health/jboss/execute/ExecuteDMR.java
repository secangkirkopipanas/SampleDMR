package org.health.jboss.execute;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.ModelControllerClient;

public class ExecuteDMR {
    private  String host;
    private  int port;
    private  String user;
    private  String password;

    public ExecuteDMR(String host, int port, String user, String password){
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public ModelControllerClient getClientInstance(){
        ModelControllerClient client = null;
        try {
            CommandContext ctx = CommandContextFactory.getInstance().newCommandContext(user, password.toCharArray());
            ctx.connectController(host, port);
            client = ctx.getModelControllerClient();
        } catch (CommandLineException e) {
            throw new RuntimeException(e);
        }

        return client != null ? client : null ;
    }
}
