package io.zhijun.rose.authorization.oauth2.token;

import io.zhijun.rose.authorization.authentication.CustomUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Optional;

public abstract class TokenCustomizer {
    public TokenCustomizer() {
    }

    abstract boolean shouldCustomize(JwtEncodingContext context);

    public final void customize(JwtEncodingContext context) {
        if (this.shouldCustomize(context)) {
            this.customizeInternal(context);
        }
    }

    abstract void customizeInternal(JwtEncodingContext context);

    final Optional<CustomUser> getPrincipal(JwtEncodingContext context) {
        return Optional.ofNullable(context.getPrincipal())
                .map(Authentication.class::cast)
                .map(Authentication::getPrincipal).filter(CustomUser.class::isInstance)
                .map(CustomUser.class::cast);
    }

    final boolean isAccessToken(JwtEncodingContext context) {
        return OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType());
    }

    final boolean isIdToken(JwtEncodingContext context) {
        return OidcTokenType.ID_TOKEN.equals(context.getTokenType());
    }

    final boolean hasScope(JwtEncodingContext context, String scope) {
        return context.getAuthorizedScopes().contains(scope);
    }

    public static class OidcTokenType {
        public static final OAuth2TokenType ID_TOKEN = new OAuth2TokenType("id_token");

        public OidcTokenType() {
        }
    }
}
