package io.zhijun.rose.authorization.oauth2.client;

import io.zhijun.rose.authorization.config.Defaults;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class CustomRegisteredClientRepository implements RegisteredClientRepository {
    private final Map<String, CustomRegisteredClient> idRegistrationMap;
    private final Map<String, CustomRegisteredClient> clientIdRegistrationMap;
    private final boolean isDefault;

    public CustomRegisteredClientRepository(CustomRegisteredClient registeredClient) {
        this(List.of(registeredClient));
    }

    public CustomRegisteredClientRepository(List<CustomRegisteredClient> registeredClients) {
        Assert.notEmpty(registeredClients, "registeredClients cannot be empty");
        ConcurrentHashMap<String, CustomRegisteredClient> idRegistrationMapResult = new ConcurrentHashMap();
        ConcurrentHashMap<String, CustomRegisteredClient> clientIdRegistrationMapResult = new ConcurrentHashMap();

        registeredClients.forEach((registration) -> {
            Assert.notNull(registration, "registration cannot be null");
            idRegistrationMapResult.put(registration.getId(), registration);
            clientIdRegistrationMapResult.put(registration.getClientId(), registration);
        });

        this.idRegistrationMap = idRegistrationMapResult;
        this.clientIdRegistrationMap = clientIdRegistrationMapResult;
        this.isDefault = registeredClients.get(0)==Defaults.CLIENT;
    }

    public void save(RegisteredClient registeredClient) {
        throw new RuntimeException("Not implemented");
    }

    @Nullable
    public CustomRegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return this.idRegistrationMap.get(id);
    }

    @Nullable
    public CustomRegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        return this.clientIdRegistrationMap.get(clientId);
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public List<CustomRegisteredClient> getRegisteredClients() {
        return this.clientIdRegistrationMap.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).toList();
    }
}
