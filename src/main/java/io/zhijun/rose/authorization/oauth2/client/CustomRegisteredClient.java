package io.zhijun.rose.authorization.oauth2.client;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class CustomRegisteredClient extends RegisteredClient {
    private final RegisteredClient delegate;
    private final boolean validateRedirectUri;
    private final boolean validateScope;
    private final Set<String> postLogoutRedirectUris = new HashSet();

    private CustomRegisteredClient(RegisteredClient delegate, boolean validateRedirectUri, boolean validateScope) {
        this.delegate = delegate;
        this.validateRedirectUri = validateRedirectUri;
        this.validateScope = validateScope;
    }

    public static Builder withId(String id) {
        return new Builder(id);
    }

    public boolean isValidateRedirectUri() {
        return this.validateRedirectUri;
    }

    public boolean isValidateScope() {
        return this.validateScope;
    }

    public void addPostLogoutUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUris.add(postLogoutRedirectUri);
    }

    public String getId() {
        return this.delegate.getId();
    }

    public String getClientId() {
        return this.delegate.getClientId();
    }

    public Instant getClientIdIssuedAt() {
        return this.delegate.getClientIdIssuedAt();
    }

    public String getClientSecret() {
        return this.delegate.getClientSecret();
    }

    public Instant getClientSecretExpiresAt() {
        return this.delegate.getClientSecretExpiresAt();
    }

    public String getClientName() {
        return this.delegate.getClientName();
    }

    public Set<ClientAuthenticationMethod> getClientAuthenticationMethods() {
        return this.delegate.getClientAuthenticationMethods();
    }

    public Set<AuthorizationGrantType> getAuthorizationGrantTypes() {
        return this.delegate.getAuthorizationGrantTypes();
    }

    public Set<String> getRedirectUris() {
        return this.delegate.getRedirectUris();
    }

    public Set<String> getPostLogoutRedirectUris() {
        return !this.isValidateRedirectUri() ? this.postLogoutRedirectUris:this.delegate.getPostLogoutRedirectUris();
    }

    public Set<String> getScopes() {
        return this.delegate.getScopes();
    }

    public ClientSettings getClientSettings() {
        return this.delegate.getClientSettings();
    }

    public TokenSettings getTokenSettings() {
        return this.delegate.getTokenSettings();
    }

    public String toYamlString() {
        String authorizationGrantTypes = this.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue).sorted().map((t) -> "  - " + t)
                .collect(Collectors.joining("\n"));
        String scopes = " []";
        if (!this.getScopes().isEmpty()) {
            scopes = "\n" + this.getScopes().stream().sorted().map((t) -> "  - " + t).collect(Collectors.joining("\n"));
        }

        String clientAuthenticationMethod = this.getClientAuthenticationMethods().stream()
                .map(ClientAuthenticationMethod::getValue).sorted().map((t) -> "  - " + t)
                .collect(Collectors.joining("\n"));

        return """
                client-id: %s
                client-secret: %s
                authorization-grant-types:
                %s
                client-authentication-method: 
                %s
                scope:%s
                """
                .formatted(this.getClientId(), this.getClientSecret(), authorizationGrantTypes, clientAuthenticationMethod, scopes);
    }

    public static class Builder extends RegisteredClient.Builder {
        private boolean validateRedirectUri = false;
        private boolean validateScope = false;

        protected Builder(String id) {
            super(id);
        }

        public Builder validateRedirectUri(boolean validateRedirectUri) {
            this.validateRedirectUri = validateRedirectUri;
            return this;
        }

        public Builder validateScope(boolean validateScope) {
            this.validateScope = validateScope;
            return this;
        }

        public CustomRegisteredClient build() {
            return new CustomRegisteredClient(super.build(), this.validateRedirectUri, this.validateScope);
        }

        public Builder id(String id) {
            super.id(id);
            return this;
        }

        public Builder clientId(String clientId) {
            super.clientId(clientId);
            return this;
        }

        public Builder clientIdIssuedAt(Instant clientIdIssuedAt) {
            super.clientIdIssuedAt(clientIdIssuedAt);
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            super.clientSecret(clientSecret);
            return this;
        }

        public Builder clientSecretExpiresAt(Instant clientSecretExpiresAt) {
            super.clientSecretExpiresAt(clientSecretExpiresAt);
            return this;
        }

        public Builder clientName(String clientName) {
            super.clientName(clientName);
            return this;
        }

        public Builder clientAuthenticationMethod(ClientAuthenticationMethod clientAuthenticationMethod) {
            super.clientAuthenticationMethod(clientAuthenticationMethod);
            return this;
        }

        public Builder clientAuthenticationMethods(Consumer<Set<ClientAuthenticationMethod>> clientAuthenticationMethodsConsumer) {
            super.clientAuthenticationMethods(clientAuthenticationMethodsConsumer);
            return this;
        }

        public Builder authorizationGrantType(AuthorizationGrantType authorizationGrantType) {
            super.authorizationGrantType(authorizationGrantType);
            return this;
        }

        public Builder authorizationGrantTypes(Consumer<Set<AuthorizationGrantType>> authorizationGrantTypesConsumer) {
            super.authorizationGrantTypes(authorizationGrantTypesConsumer);
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            super.redirectUri(redirectUri);
            return this;
        }

        public Builder redirectUris(Consumer<Set<String>> redirectUrisConsumer) {
            super.redirectUris(redirectUrisConsumer);
            return this;
        }

        public Builder postLogoutRedirectUri(String postLogoutRedirectUri) {
            super.postLogoutRedirectUri(postLogoutRedirectUri);
            return this;
        }

        public Builder postLogoutRedirectUris(Consumer<Set<String>> postLogoutRedirectUrisConsumer) {
            super.postLogoutRedirectUris(postLogoutRedirectUrisConsumer);
            return this;
        }

        public Builder scope(String scope) {
            super.scope(scope);
            return this;
        }

        public Builder scopes(Consumer<Set<String>> scopesConsumer) {
            super.scopes(scopesConsumer);
            return this;
        }

        public Builder clientSettings(ClientSettings clientSettings) {
            super.clientSettings(clientSettings);
            return this;
        }

        public Builder tokenSettings(TokenSettings tokenSettings) {
            super.tokenSettings(tokenSettings);
            return this;
        }
    }
}
