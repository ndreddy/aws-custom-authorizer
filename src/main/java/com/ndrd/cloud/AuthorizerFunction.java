package com.ndrd.cloud;

import com.ndrd.cloud.io.AuthPolicy;
import com.ndrd.cloud.io.TokenAuthorizerContext;
import com.ndrd.cloud.service.AuthorizerService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component("authorizerFunction")
public class AuthorizerFunction implements Function<TokenAuthorizerContext, AuthPolicy> {
    static final Logger log = LoggerFactory.getLogger(AuthorizerFunction.class);
    public static final String ID_TOKEN = "IdToken";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    private final AuthorizerService authorizerService;

    public AuthorizerFunction(final AuthorizerService authorizerService) {
        this.authorizerService = authorizerService;
    }

    @Override
    public AuthPolicy apply(final TokenAuthorizerContext tokenAuthorizerContext) {
        return handleRequest(tokenAuthorizerContext);
    }


    /**
     * this function must generate a policy that is associated with the recognized principal user identifier.
     * depending on your use case, you might store policies in a DB, or generate them on the fly
     * <p>
     * keep in mind, the policy is cached for 5 minutes by default (TTL is configurable in the authorizer)
     * and will apply to subsequent calls to any method/resource in the RestApi
     * made with the same token
     * <p>
     * the example policy below denies access to all resources in the RestApi
     * if the client token is not recognized or invalid
     * you can send a 401 Unauthorized response to the client by failing like so:
     * throw new RuntimeException("Unauthorized");
     * <p>
     * if the token is valid, a policy should be generated which will allow or deny access to the client
     * <p>
     * if access is denied, the client will receive a 403 Access Denied response
     * if access is allowed, API Gateway will proceed with the back-end integration configured on the method that was called
     *
     * @param input request input
     * @return Authorization policy
     */
    private AuthPolicy handleRequest(TokenAuthorizerContext input) {
        String token = input.getAuthorizationToken();

        /* decode the incoming token of form base64 encoded user:pwd */
        String[] user = decodeToken(token);
        String principalId = user[0];

        try {
            // Authenticates the User
            String response = authorizerService.authenticateUser(user[0], user[1]);

            // Constructs Policy object
            AuthPolicy policy = getAuthPolicy(principalId, input, true);

            // Sets the permissions
            policy.setContext(getContextMap(response));
            return policy;
        } catch (RuntimeException rx) {
            log.debug("Access denied due to exception : " + rx.getMessage());
            return getAuthPolicy(principalId, input, false);
        }
    }

    /**
     * Popultes context map from Json object.
     *
     * @param response Json string of context map
     * @return Context map
     */
    private Map<String, String> getContextMap(String response) {
        JSONObject jsonRes = new JSONObject(response);
        Map<String, String> context = new HashMap<>();

        if(jsonRes.has(ID_TOKEN)) {
            context.put(ID_TOKEN, jsonRes.getString(ID_TOKEN));
        }

        if (jsonRes.has(ACCESS_TOKEN)) {
            context.put(ACCESS_TOKEN, jsonRes.getString(ACCESS_TOKEN));
        }

        if (jsonRes.has(REFRESH_TOKEN)) {
            context.put(REFRESH_TOKEN, jsonRes.getString(ACCESS_TOKEN));
        }

        return context;
    }


    /**
     * Constructs Auth policy
     *
     * @param principalId principle
     * @param input request
     * @param allow true to allow else deny
     * @return Auth Policy
     */
    private AuthPolicy getAuthPolicy(String principalId, TokenAuthorizerContext input, boolean allow) {
        String methodArn = input.getMethodArn();
        String[] arnPartials = methodArn.split(":");
        String region = arnPartials[3];
        String awsAccountId = arnPartials[4];
        String[] apiGatewayArnPartials = arnPartials[5].split("/");
        String restApiId = apiGatewayArnPartials[0];
        String stage = apiGatewayArnPartials[1];

        if (allow) {
            return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getAllowAllPolicy(region, awsAccountId, restApiId, stage));

        } else {
            return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
        }
    }

    /**
     * Decodes base 64 encoded Authorization header string.
     *
     * @param token
     * @return
     */
    private String[] decodeToken(String token) {
        if (token != null && token.startsWith("Basic ")) {
            byte[] decoded = Base64.getDecoder().decode(token.replace("Basic ", ""));
            String decodedUserStr = new String(decoded, StandardCharsets.UTF_8);
            return decodedUserStr.split(":");
        } else {
            throw new RuntimeException("Unauthorized");
        }
    }
}
