package io.zhijun.rose.authorization.oauth2;

import io.zhijun.rose.authorization.oauth2.client.CustomRegisteredClient;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationValidator;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.function.Consumer;

public class AuthorizationCodeRequestValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {
    public AuthorizationCodeRequestValidator() {
    }

    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext context) {
        RegisteredClient registeredClient = context.getRegisteredClient();
        if (registeredClient instanceof CustomRegisteredClient client) {
            if (client.isValidateScope()) {
                OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR.accept(context);
            }

            if (client.isValidateRedirectUri()) {
                OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_REDIRECT_URI_VALIDATOR.accept(context);
            }

        } else {
            throw new RuntimeException("Invalid client type: " + context.getRegisteredClient().getClass());
        }
    }
}