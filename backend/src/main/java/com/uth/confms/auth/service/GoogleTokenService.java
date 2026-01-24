package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.GoogleUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class GoogleTokenService {

    @Value("${google.client-id:}")
    private String clientId;

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenService() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()).build();
    }

    public GoogleUserInfo verifyIdToken(String idTokenString) throws Exception {

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new RuntimeException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        // Check audience (only if clientId is configured)
        if (clientId != null && !clientId.isEmpty() && !payload.getAudience().equals(clientId)) {
            throw new RuntimeException("Invalid audience");
        }

        return new GoogleUserInfo(
                payload.getSubject(),
                payload.getEmail(),
                payload.get("name").toString(),
                payload.getEmailVerified());
    }
}
