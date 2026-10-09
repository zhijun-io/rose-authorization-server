package io.zhijun.rose.authorization.authentication;

import org.springframework.security.oauth2.core.ClaimAccessor;

import java.util.*;
import java.util.stream.Collectors;

public class UserAttributesClaimAccessor implements ClaimAccessor {
    private final Map<String, Object> claims;
    private static final Set<String> OPENID_PROFILE_CLAIMS = Set.of("name", "given_name", "family_name",
            "middle_name", "nickname", "preferred_username", "profile",
            "picture", "website", "gender", "birthdate", "zoneinfo", "locale", "updated_at");
    private static final Set<String> OPENID_EMAIL_CLAIMS = Set.of("email", "email_verified");
    private static final Set<String> OPENID_PHONE_CLAIMS = Set.of("phone_number", "phone_number_verified");
    private static final Set<String> OPENID_ADDRESS_CLAIMS = Set.of("address");
    private static final Set<String> OPENID_SUB_CLAIMS = Set.of("sub");
    public static final Set<String> RESERVED_CLAIMS = Set.of("acr", "amr", "at_hash", "auth_time", "azp",
            "c_hash", "nonce", "aud", "exp", "iat", "iss", "jti", "nbf", "sub");
    public static final Set<String> STANDARD_CLAIMS;

    public UserAttributesClaimAccessor() {
        this.claims = Collections.emptyMap();
    }

    public UserAttributesClaimAccessor(Map<String, Object> claims) {
        this.claims = Collections.unmodifiableMap(claims);
    }

    public UserAttributesClaimAccessor withClaim(String name, Object value) {
        HashMap<String, Object> newClaims = new HashMap(this.getClaims());
        newClaims.put(name, value);
        return new UserAttributesClaimAccessor(newClaims);
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    public Map<String, Object> getOidcProfileClaims() {
        return this.filterClaimsByName(OPENID_PROFILE_CLAIMS);
    }

    public Map<String, Object> getOidcEmailClaims() {
        return this.filterClaimsByName(OPENID_EMAIL_CLAIMS);
    }

    public Map<String, Object> getOidcPhoneClaims() {
        return this.filterClaimsByName(OPENID_PHONE_CLAIMS);
    }

    public Map<String, Object> getOidcAddressClaims() {
        return this.filterClaimsByName(OPENID_ADDRESS_CLAIMS);
    }

    public Map<String, Object> getOidcSubClaim() {
        return this.filterClaimsByName(OPENID_SUB_CLAIMS);
    }

    public Map<String, Object> getCustomClaims() {
        return this.getClaims().entrySet()
                .stream()
                .filter((e) -> !STANDARD_CLAIMS.contains(e.getKey()))
                .filter((e) -> e.getValue()!=null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Object> filterClaimsByName(Set<String> filterBy) {
        return this.getClaims().entrySet().stream()
                .filter(e -> filterBy.contains(e.getKey()))
                .filter((e) -> e.getValue()!=null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static {
        HashSet<String> result = new HashSet();
        result.addAll(OPENID_PROFILE_CLAIMS);
        result.addAll(OPENID_ADDRESS_CLAIMS);
        result.addAll(OPENID_EMAIL_CLAIMS);
        result.addAll(OPENID_PHONE_CLAIMS);
        result.addAll(OPENID_SUB_CLAIMS);
        result.addAll(RESERVED_CLAIMS);
        STANDARD_CLAIMS = Collections.unmodifiableSet(result);
    }
}
