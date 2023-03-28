package org.jboss.health.execute;


import org.wildfly.security.auth.server.IdentityCredentials;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStoreException;
import org.wildfly.security.credential.store.WildFlyElytronCredentialStoreProvider;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.ClearPassword;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

public class RetrieveCredential {

    private static RetrieveCredential retrieveCredential = null;
    private static final Provider PASSWORD_PROVIDER = new WildFlyElytronPasswordProvider();
    private static final Provider CREDENTIAL_STORE_PROVIDER = new WildFlyElytronCredentialStoreProvider();
    static {
        Security.addProvider(PASSWORD_PROVIDER);
    }

    private RetrieveCredential(){
    }

    public static RetrieveCredential getInstance(){
        if (retrieveCredential == null){
            retrieveCredential = new RetrieveCredential();
        }
        return retrieveCredential;
    }
    public String getPassword(String keystore, String storepass, String alias) {
        try {
            CredentialStore store = CredentialStore.getInstance("KeyStoreCredentialStore", CREDENTIAL_STORE_PROVIDER);
            Map<String, String> attributes = new HashMap<>();
            attributes.put("keyStoreType", "JCEKS");
            //attributes.put("location", "/Users/sidde/Downloads/Software/rh-sso-7.6/standalone/configuration/cs-store.keystore");
            attributes.put("location", keystore);

            CredentialStore.CredentialSourceProtectionParameter protection = new CredentialStore.CredentialSourceProtectionParameter(
                    IdentityCredentials.NONE.withCredential(new PasswordCredential(
                            ClearPassword.createRaw(ClearPassword.ALGORITHM_CLEAR, storepass.toCharArray()))));

            store.initialize(attributes, protection);
            Password password = store.retrieve(alias, PasswordCredential.class).getPassword();

            if (password instanceof ClearPassword) {
                String clearPassword = new String(((ClearPassword) password).getPassword());
                //System.err.println("CLEAR PASSWORD: " + clearPassword);
                return clearPassword;
            }

        } catch (CredentialStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
