package io.zhijun.rose.authorization.oauth2;

import io.zhijun.rose.authorization.oauth2.client.CustomRegisteredClient;
import io.zhijun.rose.authorization.oauth2.client.CustomRegisteredClientRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.util.StringUtils;

class CustomOidcLogoutAuthenticationProvider implements AuthenticationProvider {
    private final org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationProvider delegate;
    private final OAuth2AuthorizationService authorizationService;
    private final CustomRegisteredClientRepository registeredClientRepository;

    CustomOidcLogoutAuthenticationProvider(org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationProvider delegate, OAuth2AuthorizationService authorizationService, CustomRegisteredClientRepository registeredClientRepository) {
        this.delegate = delegate;
        this.authorizationService = authorizationService;
        this.registeredClientRepository = registeredClientRepository;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OidcLogoutAuthenticationToken initialAuth = (OidcLogoutAuthenticationToken)authentication;
        if (StringUtils.isEmpty(initialAuth.getPostLogoutRedirectUri())) {
            return this.delegate.authenticate(initialAuth);
        } else {
            OAuth2Authorization authorization = this.authorizationService.findByToken(initialAuth.getIdTokenHint(), new OAuth2TokenType("id_token"));
            if (authorization == null) {
                return this.delegate.authenticate(initialAuth);
            } else {
                CustomRegisteredClient registeredClient = this.registeredClientRepository.findById(authorization.getRegisteredClientId());
                if (registeredClient == null) {
                    return this.delegate.authenticate(initialAuth);
                } else {
                    registeredClient.addPostLogoutUri(initialAuth.getPostLogoutRedirectUri());
                    return this.delegate.authenticate(initialAuth);
                }
            }
        }
    }

    public boolean supports(Class<?> authentication) {
        return this.delegate.supports(authentication);
    }
}
