package org.health.jboss;

import org.health.jboss.dto.*;
import org.health.jboss.dto.Runtime;
import org.health.jboss.dto.Thread;
import org.jboss.as.controller.client.ModelControllerClient;

import java.io.IOException;

public class JBossManagement {

    private static JBossManagement INSTANCE;

    private ModelControllerClient client;

    private JBossManagement(ModelControllerClient client) {
        this.client = client;
    }

    public static JBossManagement getInstance(ModelControllerClient client) {
        if (INSTANCE == null) {
            INSTANCE = new JBossManagement(client);
        }
        return INSTANCE;
    }

    public void captureJBossDetails() {
        try {
            OperatingSystem os = OperatingSystem.getInstance(client);
            System.out.println("OS ==> " + os.toJsonString(false));

            Memory mem = Memory.getInstance(client);
            System.out.println("Memory ==> " + mem.toJsonString(false));

            Runtime runtime = Runtime.getInstance(client);
            System.out.println("Runtime ==> " + runtime.toJsonString(false));

            DataSource ds = DataSource.getInstance(client);
            ds.getJndiDetails(client);
            System.out.println("DataSources ==> " + ds.toJsonString(false));

            Thread thread = Thread.getInstance(client);
            System.out.println("Thread ==> " + thread.toJsonString(false));

            Connector connector = Connector.getInstance(client);
            System.out.println("Connector ==> " + connector.toJsonString(false));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
