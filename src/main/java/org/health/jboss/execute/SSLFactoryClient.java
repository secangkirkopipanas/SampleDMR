package org.health.jboss.execute;

import org.glassfish.json.JsonUtil;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.sql.SQLOutput;
import java.util.Arrays;

/**
 * @author sidd
 **/

public class SSLFactoryClient {
    private static SSLFactoryClient sslFactoryClient = null;
    private static String hostname = "redhat.com";
    private static int port = 443;

    private SSLFactoryClient() {
    }

    private SSLFactoryClient(String ip_addr, String port_str) {
        this.hostname = ip_addr;
        this.port = Integer.valueOf(port_str);
    }

    public static SSLFactoryClient getInstance(String ip_addr, String port_str) {
        if (sslFactoryClient == null) {
            sslFactoryClient = new SSLFactoryClient(ip_addr, port_str);
        }
        return sslFactoryClient;
    }

    public void printSSLDetails() {

        TrustManager[] dummyTrustManager = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,dummyTrustManager,null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(hostname, port);

            X509Certificate[] certs = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            for(X509Certificate certificate: certs){
                System.out.printf("Subject: %s\nIssuer: %s\nSignature Algorithm: %s\nValid till: %s\n",certificate.getSubjectDN(),certificate.getIssuerDN(),certificate.getSigAlgName(),certificate.getNotAfter());
                System.out.printf("Subject Alternative Name: %s\n",String.valueOf(certificate.getSubjectAlternativeNames()));
                System.out.printf("\nSerial Number: %s\n",certificate.getSerialNumber().toString());
            }
            System.out.println("Enabled Protocols: ");
            Arrays.stream(sslSocket.getEnabledProtocols()).forEach(
                    n -> System.out.printf("\t%s\n",n)
            );
            System.out.println("\nEnabled Ciphers: ");
            Arrays.stream(sslSocket.getEnabledCipherSuites()).forEach(
                    n -> System.out.printf("\t%s\n",n)
            );

        } catch (IOException | NoSuchAlgorithmException | KeyManagementException | CertificateParsingException e) {
            throw new RuntimeException(e);
        }

    }
}