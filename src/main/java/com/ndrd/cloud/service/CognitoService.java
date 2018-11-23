package com.ndrd.cloud.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The CognitoHelper class abstracts the functionality of connecting to
 * the Cognito user pool and Federated Identities.
 */
@Service
public class CognitoService implements AuthorizerService {

    static final Logger log = LoggerFactory.getLogger(CognitoService.class);

    //TODO    Move to application properties file
    String POOL_ID = System.getenv("POOL_ID");
    String CLIENTAPP_ID = System.getenv("CLIENTAPP_ID");
    String CLIENTAPP_SECRET = System.getenv("CLIENTAPP_SECRET");


    /**
     * Authenticate the user.
     *
     * @param username User id to be authenticated.
     * @param password the user's password.
     * @return if the verification is successful.
     */
    public String authenticateUser(String username, String password) {

        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);
        authParams.put("SECRET_HASH", CLIENTAPP_SECRET);

        AWSCognitoIdentityProvider cognitoIdp = AWSCognitoIdentityProviderClientBuilder.defaultClient();
        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withAuthParameters(authParams)
                .withClientId(CLIENTAPP_ID)
                .withUserPoolId(POOL_ID);
        AdminInitiateAuthResult authResponse = cognitoIdp.adminInitiateAuth(authRequest);

        String challenge = authResponse.getChallengeName();

        // Temp password check
        if (ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(challenge)) {
            throw new NotAuthorizedException("Attempted to sign in with temporary password for " + username);
        }
        return authResponse.getAuthenticationResult().toString();
    }

    public String authenticateToken(String accessToken) {
        try {
            AWSCognitoIdentityProvider cognitoIdp = AWSCognitoIdentityProviderClientBuilder.defaultClient();
            GetUserRequest authRequest = new GetUserRequest().withAccessToken(accessToken);
            GetUserResult authResponse = cognitoIdp.getUser(authRequest);
            String userName = authResponse.getUsername();
        } catch (RuntimeException ex) {
            log.error("Exception Occured : " + ex.getMessage());
        }

        return "";
    }

    public String refreshToken(String accessToken) {
        try {
            AWSCognitoIdentityProvider cognitoIdp = AWSCognitoIdentityProviderClientBuilder.defaultClient();
            GetUserRequest authRequest = new GetUserRequest().withAccessToken(accessToken);
            GetUserResult authResponse = cognitoIdp.getUser(authRequest);
            String userName = authResponse.getUsername();

        } catch (RuntimeException ex) {
            log.error("Exception Occurred : " + ex.getMessage());
        }
        return "";
    }
}