package io.zhijun.rose.authorization.oauth2.token;

import io.zhijun.rose.authorization.authentication.CustomUser;
import io.zhijun.rose.authorization.authentication.UserAttributesClaimAccessor;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Map;

import static org.springframework.security.oauth2.core.oidc.OidcScopes.PHONE;

public class IdTokenPhoneCustomizer extends TokenCustomizer {
    public IdTokenPhoneCustomizer() {
    }

    boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && this.hasScope(context, PHONE);
    }

    void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getOidcPhoneClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}
