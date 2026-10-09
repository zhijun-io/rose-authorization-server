package io.zhijun.rose.authorization.oauth2.token;

import io.zhijun.rose.authorization.authentication.CustomUser;
import io.zhijun.rose.authorization.authentication.UserAttributesClaimAccessor;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Map;

public class IdTokenCustomClaimsCustomizer extends TokenCustomizer {
    public IdTokenCustomClaimsCustomizer() {
    }

    public boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && context.getAuthorizedScopes().contains(OidcScopes.OPENID);
    }

    public void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getCustomClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}
