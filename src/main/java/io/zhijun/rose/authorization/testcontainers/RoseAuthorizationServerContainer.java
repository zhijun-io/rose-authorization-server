package io.zhijun.rose.authorization.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.util.*;

/**
 * Testcontainers wrapper for {@code ghcr.io/zhijun-io/rose-authorization-server}.
 *
 * <p>Defaults match the bundled Authorization Server: port {@code 9000}, health at
 * {@code /actuator/health}, and the default client credentials.
 */
public class RoseAuthorizationServerContainer extends GenericContainer<RoseAuthorizationServerContainer> {

    public static final String DEFAULT_IMAGE_NAME = "ghcr.io/zhijun-io/rose-authorization-server:latest";

	public static final int PORT = 9000;

	public static final String DEFAULT_CLIENT_ID = "default-client-id";

	public static final String DEFAULT_CLIENT_SECRET = "default-client-secret";

    public static final String DEFAULT_PROVIDER_ID = "rose-authorization-server";

	private static final String CONFIG_IN_CONTAINER = "/config/config.yml";

	private static final String SERVER_CERT_IN_CONTAINER = "/certs/server.crt";

	private static final String SERVER_KEY_IN_CONTAINER = "/certs/server.key";

	private static final String CLIENT_CA_IN_CONTAINER = "/certs/client-ca.crt";

	private final List<String> commandArgs = new ArrayList<>();

	private boolean https;

	private ReadyProbe readyProbe = ReadyProbe.HTTP_HEALTH;

	/**
	 * How the container decides it is ready. mTLS cannot use an unauthenticated HTTPS health probe.
	 */
	public enum ReadyProbe {
		HTTP_HEALTH,
		HTTPS_HEALTH,
		STARTUP_LOG
	}

    public RoseAuthorizationServerContainer() {
		this(DockerImageName.parse(DEFAULT_IMAGE_NAME));
	}

    public RoseAuthorizationServerContainer(String dockerImageName) {
		this(DockerImageName.parse(dockerImageName));
	}

    public RoseAuthorizationServerContainer(DockerImageName dockerImageName) {
		super(dockerImageName);
		withExposedPorts(PORT);
		waitingForHttpHealth();
	}

	/**
	 * Mount a YAML config and pass {@code --config} so users/clients override defaults.
	 */
    public RoseAuthorizationServerContainer withConfig(Path configFile) {
		Objects.requireNonNull(configFile, "configFile");
		return withConfig(MountableFile.forHostPath(configFile));
	}

    public RoseAuthorizationServerContainer withConfig(MountableFile configFile) {
		Objects.requireNonNull(configFile, "configFile");
		withCopyFileToContainer(configFile, CONFIG_IN_CONTAINER);
		replaceCommandArgPrefix("--config=", "--config=" + CONFIG_IN_CONTAINER);
		applyCommand();
		return this;
	}

	/**
	 * Mount a classpath resource as the Authorization Server {@code --config} file.
	 */
    public RoseAuthorizationServerContainer withClasspathConfig(String classpathResource) {
		Objects.requireNonNull(classpathResource, "classpathResource");
		return withConfig(MountableFile.forClasspathResource(classpathResource));
	}

	/**
	 * Enable HTTPS with a PEM certificate / private key pair (typically self-signed for tests).
	 *
	 * <p>Health checks use {@code Wait.forHttps(...).allowInsecure()} so self-signed certs work.
	 */
    public RoseAuthorizationServerContainer withTls(Path certificate, Path privateKey) {
		Objects.requireNonNull(certificate, "certificate");
		Objects.requireNonNull(privateKey, "privateKey");
		withCopyFileToContainer(MountableFile.forHostPath(certificate), SERVER_CERT_IN_CONTAINER);
		withCopyFileToContainer(MountableFile.forHostPath(privateKey), SERVER_KEY_IN_CONTAINER);
		replaceCommandArgPrefix("--spring.ssl.bundle.pem.server.keystore.certificate=",
				"--spring.ssl.bundle.pem.server.keystore.certificate=file:" + SERVER_CERT_IN_CONTAINER);
		replaceCommandArgPrefix("--spring.ssl.bundle.pem.server.keystore.private-key=",
				"--spring.ssl.bundle.pem.server.keystore.private-key=file:" + SERVER_KEY_IN_CONTAINER);
		replaceCommandArgPrefix("--server.ssl.bundle=", "--server.ssl.bundle=server");
		replaceCommandArgPrefix("--server.ssl.client-auth=", "--server.ssl.client-auth=NONE");
		this.https = true;
		waitingForHttpsHealth();
		applyCommand();
		return this;
	}

	/**
	 * Enable mTLS: server presents {@code certificate}/{@code privateKey}, and requires client
	 * certificates signed by {@code clientCaCertificate}.
	 *
	 * <p>Readiness uses a startup log probe (not HTTPS health): unauthenticated health checks fail
	 * when {@code client-auth=NEED}.
	 */
    public RoseAuthorizationServerContainer withMutualTls(Path certificate, Path privateKey,
			Path clientCaCertificate) {
		Objects.requireNonNull(clientCaCertificate, "clientCaCertificate");
		withTls(certificate, privateKey);
		withCopyFileToContainer(MountableFile.forHostPath(clientCaCertificate), CLIENT_CA_IN_CONTAINER);
		replaceCommandArgPrefix("--spring.ssl.bundle.pem.server.truststore.certificate=",
				"--spring.ssl.bundle.pem.server.truststore.certificate=file:" + CLIENT_CA_IN_CONTAINER);
		replaceCommandArgPrefix("--server.ssl.client-auth=", "--server.ssl.client-auth=NEED");
		waitingForStartupLog();
		applyCommand();
		return this;
	}

	public ReadyProbe getReadyProbe() {
		return this.readyProbe;
	}

	public boolean isHttps() {
		return this.https;
	}

	public int getHttpPort() {
		return getMappedPort(PORT);
	}

	public String getIssuerUri() {
		String scheme = this.https ? "https" : "http";
		return scheme + "://" + getHost() + ":" + getMappedPort(PORT);
	}

	public String getOpenIdConfigurationUri() {
		return getIssuerUri() + "/.well-known/openid-configuration";
	}

	/**
	 * Boot OAuth2 client property keys/values for the default provider registration id.
	 *
	 * <p>Requires the container to be started (uses the mapped issuer URI). Prefer
	 * {@link #oauth2ClientProperties(String, String)} when composing maps before start.
	 */
	public Map<String, String> oauth2ClientProperties() {
		return oauth2ClientProperties(DEFAULT_PROVIDER_ID, getIssuerUri());
	}

	public Map<String, String> oauth2ClientProperties(String providerId) {
		return oauth2ClientProperties(providerId, getIssuerUri());
	}

	public Map<String, String> oauth2ClientProperties(String providerId, String issuerUri) {
		Objects.requireNonNull(providerId, "providerId");
		Objects.requireNonNull(issuerUri, "issuerUri");
		Map<String, String> properties = new LinkedHashMap<>();
		properties.put("spring.security.oauth2.client.provider." + providerId + ".issuer-uri", issuerUri);
		properties.put("spring.security.oauth2.client.registration." + providerId + ".provider", providerId);
		properties.put("spring.security.oauth2.client.registration." + providerId + ".client-id",
				DEFAULT_CLIENT_ID);
		properties.put("spring.security.oauth2.client.registration." + providerId + ".client-secret",
				DEFAULT_CLIENT_SECRET);
		return properties;
	}

	private void replaceCommandArgPrefix(String prefix, String replacement) {
		commandArgs.removeIf(arg -> arg.startsWith(prefix));
		commandArgs.add(replacement);
	}

	private void applyCommand() {
		if (!commandArgs.isEmpty()) {
			withCommand(commandArgs.toArray(String[]::new));
		}
	}

	private void waitingForHttpHealth() {
		this.readyProbe = ReadyProbe.HTTP_HEALTH;
		waitingFor(Wait.forHttp("/actuator/health").forPort(PORT));
	}

	private void waitingForHttpsHealth() {
		this.readyProbe = ReadyProbe.HTTPS_HEALTH;
		waitingFor(Wait.forHttps("/actuator/health").forPort(PORT).allowInsecure());
	}

	private void waitingForStartupLog() {
		this.readyProbe = ReadyProbe.STARTUP_LOG;
		waitingFor(Wait.forLogMessage(".*Started AuthorizationServerApplication.*", 1));
	}
}
