package io.zhijun.rose.authorization.oauth2.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Validated
public class ClientProperties {
    private final @NotBlank String clientId;
    private final @NotBlank String clientSecret;
    private final @NotEmpty List<ClientAuthenticationMethod> clientAuthenticationMethods;
    private final @NotEmpty List<AuthorizationGrantType> authorizationGrantTypes;
    private final List<String> scope;
    private final List<String> redirectUris;
    private final List<String> postLogoutRedirectUris;
    private final boolean requireAuthorizationConsent;
    private final boolean requireProofKey;
    private final boolean validateRedirectUri;
    private final boolean validateScope;
    private final OAuth2AuthorizationServerProperties.Token token;

    public ClientProperties(@NotBlank String clientId,
                            @NotBlank String clientSecret,
                            @NotEmpty List<ClientAuthenticationMethod> clientAuthenticationMethods,
                            @NotEmpty List<AuthorizationGrantType> authorizationGrantTypes,
                            @Nullable List<String> scope,
                            @Nullable List<String> redirectUris,
                            @Nullable List<String> postLogoutRedirectUris,
                            @Nullable Boolean requireAuthorizationConsent,
                            @Nullable Boolean requireProofKey,
                            @Nullable Boolean validateRedirectUri,
                            @Nullable Boolean validateScope,
                            OAuth2AuthorizationServerProperties.@Nullable Token token
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientAuthenticationMethods = Collections.unmodifiableList(clientAuthenticationMethods);
        this.authorizationGrantTypes = Collections.unmodifiableList(authorizationGrantTypes);
        if (this.authorizationGrantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE)) {
            Assert.notEmpty(redirectUris, "when authorization-grant-types contains \"authorization_code\", redirect-uris must not be empty");
        }

        this.redirectUris = redirectUris!=null ? Collections.unmodifiableList(redirectUris):Collections.emptyList();
        this.postLogoutRedirectUris = postLogoutRedirectUris!=null ? Collections.unmodifiableList(postLogoutRedirectUris):Collections.emptyList();
        this.scope = scope!=null ? Collections.unmodifiableList(scope):Collections.emptyList();
        this.requireAuthorizationConsent = requireAuthorizationConsent!=null ? requireAuthorizationConsent:false;
        this.requireProofKey = requireProofKey!=null ? requireProofKey:false;
        this.token = token!=null ? token:new OAuth2AuthorizationServerProperties.Token();
        this.validateScope = validateScope!=null ? validateScope:false;
        this.validateRedirectUri = validateRedirectUri!=null ? validateRedirectUri:false;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public Collection<ClientAuthenticationMethod> getClientAuthenticationMethods() {
        return this.clientAuthenticationMethods;
    }

    public Collection<AuthorizationGrantType> getAuthorizationGrantTypes() {
        return this.authorizationGrantTypes;
    }

    public Collection<String> getRedirectUris() {
        return this.redirectUris;
    }

    public Collection<String> getPostLogoutRedirectUris() {
        return this.postLogoutRedirectUris;
    }

    public List<String> getScope() {
        return this.scope;
    }

    public boolean isRequireAuthorizationConsent() {
        return this.requireAuthorizationConsent;
    }

    public boolean isRequireProofKey() {
        return requireProofKey;
    }

    public OAuth2AuthorizationServerProperties.Token getToken() {
        return token;
    }

    public boolean isValidateRedirectUri() {
        return this.validateRedirectUri;
    }

    public boolean isValidateScope() {
        return this.validateScope;
    }
}
