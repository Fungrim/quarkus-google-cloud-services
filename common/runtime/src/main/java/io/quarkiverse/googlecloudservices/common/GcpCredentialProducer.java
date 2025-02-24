package io.quarkiverse.googlecloudservices.common;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import io.quarkus.security.credential.Credential;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;

@Singleton
public class GcpCredentialProducer {

    private static final String CLOUD_OAUTH_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    @Inject
    Instance<SecurityIdentity> securityIdentity;

    @Inject
    GcpConfigHolder gcpConfigHolder;

    @PostConstruct
    public void verifySecurityIdentity() {
        if (securityIdentity.isResolvable() && securityIdentity.isAmbiguous()) {
            throw new IllegalStateException("Multiple " + SecurityIdentity.class + " beans registered");
        }
    }

    @Produces
    @Singleton
    @Default
    public GoogleCredentials googleCredential() throws IOException {
        GcpBootstrapConfiguration gcpConfiguration = gcpConfigHolder.getBootstrapConfig();
        if (gcpConfiguration.serviceAccountLocation.isPresent()) {
            try (FileInputStream is = new FileInputStream(gcpConfiguration.serviceAccountLocation.get())) {
                return GoogleCredentials.fromStream(is).createScoped(CLOUD_OAUTH_SCOPE);
            }
        } else if (gcpConfiguration.serviceAccountEncodedKey.isPresent()) {
            byte[] decode = Base64.getDecoder().decode(gcpConfiguration.serviceAccountEncodedKey.get());
            try (ByteArrayInputStream is = new ByteArrayInputStream(decode)) {
                return GoogleCredentials.fromStream(is).createScoped(CLOUD_OAUTH_SCOPE);
            }
        } else if (gcpConfiguration.accessTokenEnabled && securityIdentity.isResolvable()
                && !isAnonymous(securityIdentity.get())) {
            for (Credential cred : securityIdentity.get().getCredentials()) {
                if (cred instanceof TokenCredential && "bearer".equals(((TokenCredential) cred).getType())) {
                    return GoogleCredentials
                            .create(new AccessToken(((TokenCredential) cred).getToken(), null))
                            .createScoped(CLOUD_OAUTH_SCOPE);
                }
            }
        }

        return GoogleCredentials.getApplicationDefault().createScoped(CLOUD_OAUTH_SCOPE);
    }

    private boolean isAnonymous(SecurityIdentity securityIdentity) {
        try {
            return securityIdentity.isAnonymous();
        } catch (ContextNotActiveException e) {
            // this is less than ideal but currently when the credential il returned out of a request context
            // (for eg at configuration time for a config source) this exceptin can arise.
            return true;
        }
    }
}
