package ca.dtadmi.todolist.firebase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FirebaseConfigHelper {

    private final String type;
    private final String projectId;
    private final String privateKeyId;
    private final String privateKey;
    private final String clientEmail;
    private final String clientId;
    private final String authUri;
    private final String tokenUri;
    private final String authProviderX509CertUrl;
    private final String clientX509CertUrl;
    private final String universeDomain;

    @Autowired
    public FirebaseConfigHelper(
        @Value("${secret.type}")
        String type,
        @Value("${secret.project_id}")
        String projectId,
        @Value("${secret.private_key_id}")
        String privateKeyId,
        @Value("${secret.private_key}")
        String privateKey,
        @Value("${secret.client_email}")
        String clientEmail,
        @Value("${secret.client_id}")
        String clientId,
        @Value("${secret.auth_uri}")
        String authUri,
        @Value("${secret.token_uri}")
        String tokenUri,
        @Value("${secret.auth_provider_x509_cert_url}")
        String authProviderX509CertUrl,
        @Value("${secret.client_x509_cert_url}")
        String clientX509CertUrl,
        @Value("${secret.universe_domain}")
        String universeDomain
    ) {
        this.type = type;
        this.projectId = projectId;
        this.privateKeyId = privateKeyId;
        this.privateKey = privateKey;
        this.clientEmail = clientEmail;
        this.clientId = clientId;
        this.authUri = authUri;
        this.tokenUri = tokenUri;
        this.authProviderX509CertUrl = authProviderX509CertUrl;
        this.clientX509CertUrl = clientX509CertUrl;
        this.universeDomain = universeDomain;
    }

    public String serviceAccountBuilder() {
        return  "{\n" +
                "  \"type\": \"" +
                type + "\",\n" + "  \"project_id\": \"" +
                projectId + "\",\n" + "  \"private_key_id\": \"" +
                privateKeyId + "\",\n" +
                "  \"private_key\": \"" +
                privateKey + "\",\n" +
                "  \"client_email\": \"" +
                clientEmail + "\",\n" +
                "  \"client_id\": \"" +
                clientId + "\",\n" +
                "  \"auth_uri\": \"" +
                authUri + "\",\n" +
                "  \"token_uri\": \"" +
                tokenUri + "\",\n" +
                "  \"auth_provider_x509_cert_url\": \"" +
                authProviderX509CertUrl + "\",\n" +
                "  \"client_x509_cert_url\": \"" +
                clientX509CertUrl + "\",\n" +
                "  \"universe_domain\": \"" +
                universeDomain + "\"\n" +
                "}";
    }
}
