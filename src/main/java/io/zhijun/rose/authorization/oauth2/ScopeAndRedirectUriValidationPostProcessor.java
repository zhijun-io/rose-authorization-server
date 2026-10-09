package io.zhijun.rose.authorization.oauth2;

import io.zhijun.rose.authorization.oauth2.client.CustomRegisteredClientRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationProvider;

public class ScopeAndRedirectUriValidationPostProcessor implements ObjectPostProcessor<AuthenticationProvider> {
    private final CustomRegisteredClientRepository repository;
    private final OAuth2AuthorizationService authorizationService;

    public ScopeAndRedirectUriValidationPostProcessor(CustomRegisteredClientRepository repository, OAuth2AuthorizationService authorizationService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
    }

    public AuthenticationProvider postProcess(AuthenticationProvider authenticationProvider) {
        if (authenticationProvider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider provider) {
            provider.setAuthenticationValidator(new AuthorizationCodeRequestValidator());
        } else if (authenticationProvider instanceof OAuth2ClientCredentialsAuthenticationProvider provider) {
            provider.setAuthenticationValidator(new ClientCredentialsRequestValidator());
        } else if (authenticationProvider instanceof OidcLogoutAuthenticationProvider provider) {
            return new CustomOidcLogoutAuthenticationProvider(provider, this.authorizationService, this.repository);
        }

        return authenticationProvider;
    }
}
