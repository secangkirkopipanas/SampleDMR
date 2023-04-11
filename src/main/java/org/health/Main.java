package org.health;

import org.health.jboss.JBossManagement;
import org.health.jboss.execute.ConfigureTLS;
import org.health.jboss.execute.DeployTLS;
import org.health.jboss.execute.ExecuteDMR;
import org.health.jboss.execute.RetrieveCredential;
import org.health.jboss.osops.FileOperation;
import org.health.jboss.stats.GetConnPool;
import org.health.jboss.stats.GetHeap;
import org.health.jboss.stats.GetThread;
import org.health.tomcat.JmxMBeanServerConnection;
import org.jboss.as.controller.client.ModelControllerClient;

import java.io.*;
import java.util.Properties;

/**
 * @author sidde
 */
public class Main {
    private static Boolean captureJboss = false;
    private static Boolean captureTomcat = false;
    private static Boolean credentialStore = false;
    private static String store = null;
    private static String alias = null;
    private static String storepass = null;
    private static Boolean deployTLS = false;
    private static String host = null;
    private static int port = 0;
    private static String user = null;
    private static String password = null;
    private static Boolean generateDump = false;
    private static Boolean generateCSR = false;
    private static Boolean captureCPU = false;

    private static String PID = null;

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
                case "-captureJboss":
                    this.captureJboss = true;
                    break;
                case "-captureTomcat":
                    this.captureTomcat = true;
                    break;
                case "-deployTLS":
                    this.deployTLS = true;
                    break;
                case "-credentialStore":
                    this.credentialStore = true;
                    break;
                case "-store":
                    this.store = parseStringArgument("-store", args, ++i);
                    break;
                case "-storepass":
                    this.storepass = parseStringArgument("-storepass", args, ++i);
                    break;
                case "-alias":
                    this.alias = parseStringArgument("-alias", args, ++i);
                    break;
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
                case "-generateDump":
                    this.generateDump = true;
                    break;
                case "-generateCSR":
                    this.generateCSR = true;
                    break;
                case "-captureCPU":
                    this.captureCPU = true;
                    break;
                case "-pid":
                    this.PID = parseStringArgument("-pid", args, ++i);
                default:
                    more = false;
                    i--;
            }
        }
    }

    public static void main(String[] args) {
        Main obj = new Main();
        obj.parse(args);
        if (captureJboss) {
            if (credentialStore) {
                if (store != null && storepass != null && alias != null) {
                    password = RetrieveCredential.getInstance().getPassword(store, storepass, alias);
                    //System.err.println(password);
                }
            }
            if (host != null && port != 0 && user != null && password != null) {
                ExecuteDMR dmr = new ExecuteDMR(host, port, user, password);
                ModelControllerClient client = dmr.getClientInstance();
                new GetHeap(client).execute();
                new GetConnPool(client).execute();
                new GetThread(client).execute();

                JBossManagement.getInstance(client).captureJBossDetails();

            } else {
                System.out.println("Please pass the following parameter to capture Jboss related parameters\n" +
                        "\t -H: hostname \n" +
                        "\t -P: Controller Port \n" +
                        "\t -U: username \n" +
                        "\t -W: password \n");
            }
        }
        if (deployTLS) {
            if (host != null && port != 0 && user != null && password != null) {
                System.out.printf("%s, %d, %s, %s", host, port, user, password);
                ExecuteDMR dmr = new ExecuteDMR(host, port, user, password);
                ModelControllerClient client = dmr.getClientInstance();
                new DeployTLS(client).execute("application.keystore", "password");
                new ConfigureTLS(client).execute();
            } else {
                System.out.println("Please pass the following parameter to capture Jboss related parameters\n" +
                        "\t -H: hostname \n" +
                        "\t -P: Controller Port \n" +
                        "\t -U: username \n" +
                        "\t -W: password \n");
            }
        }
        if (generateDump) {
            if (PID != null) {
                String tempfile = FileOperation.getInstance().createTempFile("generate-dump");
                FileOperation.getInstance().executeScript(tempfile + " " + PID);
                obj.deleteFile(tempfile);
            } else {
                System.out.println("Please pass the PID\n" +
                        "\t-pid: \n");
            }
        }
        if (generateCSR) {
            File conf = new File("preconfig");
            if (conf.exists()) {
                try {
                    InputStream inputStream = new FileInputStream(conf);
                    Properties prop = new Properties();
                    prop.load(inputStream);
                    String key_arg = prop.getProperty("keytool.all");
                    String genkey_arg = prop.getProperty("keytool.genkeypair");
                    String csr_arg = prop.getProperty("keytool.certreq");
                    String list_arg = prop.getProperty("keytool.list");
                    //System.out.println("keytool -genkeypair "+key_arg+" "+genkey_arg);
                    File tempfile = new File("generate-csr.sh");
                    FileWriter fileWriter = new FileWriter(tempfile);
                    fileWriter.write("keytool -genkeypair " + key_arg + " " + genkey_arg + "\n");
                    fileWriter.write("keytool -list " + key_arg + " " + list_arg + "\n");
                    fileWriter.write("keytool -certreq " + key_arg + " " + csr_arg + "\n");
                    fileWriter.close();
                    FileOperation.getInstance().executeScript(tempfile.getPath());
                    conf.delete();
                    tempfile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Create a pre-configured options file with name preconfig\n" +
                        "-----------------------------------------------------------\n" +
                        "keytool.all = -keystore ${user.home}/jboss.keystore -storepass secret\n" +
                        "keytool.list = -v\n" +
                        "keytool.genkeypair = -alias mykey -keyalg rsa -keysize 2048 -sigalg SHA256withRSA -validity 90 -dname \"CN=localhost\" -keypass secret\n" +
                        "keytool.certreq = -alias mykey -file mykey.csr\n" +
                        "\npreconfig file will be deleted post execution");
            }

        }
        if (captureCPU) {
            String tempfile = FileOperation.getInstance().createTempFile("capture-cpu-memory");
            FileOperation.getInstance().executeScript(tempfile);
            obj.deleteFile(tempfile);
        }
        if (captureTomcat) {
            String tempfile = FileOperation.getInstance().createTempFile("capture-tomcat");
            JmxMBeanServerConnection.getInstance().execute("report.csv");
            FileOperation.getInstance().executeScript(tempfile);
            obj.deleteFile(tempfile);
        }
    }

    private void deleteFile(String filename) {
        File file = new File(filename);
        file.delete();
    }
}

