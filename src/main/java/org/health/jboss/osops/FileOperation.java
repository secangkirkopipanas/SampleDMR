package org.health.jboss.osops;

import java.io.*;

public class FileOperation {

    private static FileOperation fileOperation = null;
    private String tempfile;

    public String createTempFile(String filename) {
        try {
            File f = File.createTempFile(filename, ".sh");
            tempfile = f.getPath();
            InputStream is = getClass().getClassLoader().getResourceAsStream(filename + ".sh");
            OutputStream os = new FileOutputStream(f);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempfile;
    }

    public void executeScript(String script) {
        String s;
        try {
            //System.out.println(script);
            Process ps = Runtime.getRuntime().exec("sh "+script);
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            while ((s = br.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileOperation getInstance() {
        if (fileOperation == null) {
            fileOperation = new FileOperation();
        }
        return fileOperation;
    }
}
