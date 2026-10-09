package io.zhijun.rose.authorization.authentication;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Validated
public class CustomUser implements UserDetails {
    private final @NotBlank String username;
    private final @NotBlank String password;
    private final UserAttributesClaimAccessor attributes;

    @ConstructorBinding
    public CustomUser(String username, String password, @Nullable Map<String, Object> attributes) {
        this.username = username;
        this.password = password;
        this.attributes = attributes!=null ? new UserAttributesClaimAccessor(preProcessAttributes(attributes)):new UserAttributesClaimAccessor();
    }

    public CustomUser(String username, String password) {
        this(username, password, Collections.emptyMap());
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.NO_AUTHORITIES;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    public UserAttributesClaimAccessor getTokenClaims() {
        return this.attributes;
    }

    public Object getClaim(String claim) {
        return this.getTokenClaims().getClaim(claim);
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean equals(Object obj) {
        if (obj==this) {
            return true;
        } else if (!(obj instanceof CustomUser)) {
            return false;
        } else {
            CustomUser that = (CustomUser) obj;
            return Objects.equals(this.username, that.username) && Objects.equals(this.password, that.password) && Objects.equals(this.attributes, that.attributes);
        }
    }

    public int hashCode() {
        return Objects.hash(this.username, this.password, this.attributes);
    }

    public String toString() {
        return "User[username=" + this.username + ", password=****, attributes=" + this.attributes + "]";
    }

    private static Map<String, Object> preProcessAttributes(Map<String, Object> claims) {
        if (claims==null) {
            return Map.of();
        }

        HashMap<String, Object> newClaims = new HashMap(claims);
        claims.forEach((key, value) -> {
            if (value instanceof Map) {
                Map mapEntry = (Map) value;
                Set<Integer> keysAsInt = (Set<Integer>) mapEntry.keySet().stream()
                        .filter(t -> String.class.isInstance(t))
                        .map(t -> intOrNull((String) t))
                        .filter(Objects::nonNull).collect(Collectors.toSet());
                if (isMonotonousSequenceOfSize(keysAsInt, mapEntry.size())) {
                    List<Object> orderedValues = (List<Object>) IntStream.range(0, mapEntry.size()).mapToObj(Integer::toString).map(mapEntry::get).toList();
                    newClaims.put(key, orderedValues);
                }
            }
        });
        return newClaims;
    }

    private static boolean isMonotonousSequenceOfSize(Set<Integer> intKeys, int size) {
        List<Integer> sortedKeys = intKeys.stream().sorted().toList();
        return intKeys.size()==size && sortedKeys.get(0)==0 && sortedKeys.get(sortedKeys.size() - 1)==sortedKeys.size() - 1;
    }

    @Nullable
    public static Integer intOrNull(String key) {
        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException var2) {
            return null;
        }
    }
}
