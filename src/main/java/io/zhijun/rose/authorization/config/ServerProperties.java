package io.zhijun.rose.authorization.config;

import io.zhijun.rose.authorization.authentication.CustomUser;
import io.zhijun.rose.authorization.oauth2.client.ClientProperties;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "rose.authorization-server")
public class ServerProperties {
    @Nullable
    private final List<CustomUser> users;
    @Nullable
    private final List<ClientProperties> clients;
    @Nullable
    private final Jwk jwk;

    @ConstructorBinding
    public ServerProperties(@Nullable List<CustomUser> users, @Nullable List<ClientProperties> clients, @Nullable Jwk jwk) {
        this.users = users;
        this.clients = clients;
        this.jwk = jwk;
    }

    public List<CustomUser> getUsers() {
        return this.users!=null ? this.users:Arrays.asList(Defaults.USER);
    }

    public List<ClientProperties> getClients() {
        return this.clients!=null ? this.clients:Collections.emptyList();
    }

    public Jwk getJwk() {
        return this.jwk!=null ? this.jwk:new Jwk(true, null, null);
    }

    public record Jwk(boolean random, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
    }
}
