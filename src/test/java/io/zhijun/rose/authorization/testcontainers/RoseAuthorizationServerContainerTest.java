package io.zhijun.rose.authorization.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RoseAuthorizationServerContainerTest {

	@TempDir
	static Path tempDir;

	@Test
	void defaultsToHttpHealthProbe() {
		try (RoseAuthorizationServerContainer container = new RoseAuthorizationServerContainer()) {
			assertThat(container.getReadyProbe()).isEqualTo(RoseAuthorizationServerContainer.ReadyProbe.HTTP_HEALTH);
			assertThat(container.isHttps()).isFalse();
		}
	}

	@Test
	void tlsSwitchesToInsecureHttpsProbe() throws IOException {
		try (RoseAuthorizationServerContainer container = new RoseAuthorizationServerContainer()
				.withTls(writeCert("server.crt"), writeCert("server.key"))) {
			assertThat(container.isHttps()).isTrue();
			assertThat(container.getReadyProbe()).isEqualTo(RoseAuthorizationServerContainer.ReadyProbe.HTTPS_HEALTH);
		}
	}

	@Test
	void mutualTlsFallsBackToStartupLogProbe() throws IOException {
		try (RoseAuthorizationServerContainer container = new RoseAuthorizationServerContainer().withMutualTls(
				writeCert("server.crt"), writeCert("server.key"), writeCert("client-ca.crt"))) {
			assertThat(container.getReadyProbe())
					.isEqualTo(RoseAuthorizationServerContainer.ReadyProbe.STARTUP_LOG);
		}
	}

	@Test
	void buildsOauth2ClientPropertiesForGivenIssuer() {
		Map<String, String> properties = new RoseAuthorizationServerContainer()
				.oauth2ClientProperties("custom-provider", "https://localhost:9000");

		assertThat(properties).containsEntry(
				"spring.security.oauth2.client.provider.custom-provider.issuer-uri", "https://localhost:9000");
		assertThat(properties).containsEntry(
				"spring.security.oauth2.client.registration.custom-provider.client-id",
				RoseAuthorizationServerContainer.DEFAULT_CLIENT_ID);
		assertThat(properties).containsEntry(
				"spring.security.oauth2.client.registration.custom-provider.client-secret",
				RoseAuthorizationServerContainer.DEFAULT_CLIENT_SECRET);
	}

	private static Path writeCert(String name) throws IOException {
		Path file = tempDir.resolve(name);
		Files.writeString(file, "placeholder\n");
		return file;
	}
}
