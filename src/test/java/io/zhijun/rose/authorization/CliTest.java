package io.zhijun.rose.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CliTest {

	@Test
	void allowsSpringBootPropertyFlags() {
		assertThat(Cli.isPassthroughBootProperty("--spring.ssl.bundle.pem.server.keystore.certificate"))
				.isTrue();
		assertThat(Cli.isPassthroughBootProperty("--server.ssl.client-auth")).isTrue();
		assertThat(Cli.isPassthroughBootProperty("--config")).isFalse();
		assertThat(Cli.checkUnknownFlagsAndPrintError(new String[] {
                "--config=configs/config.yml",
                "--spring.config.additional-location=optional:file:configs/tls/application-tls.yml",
				"--server.ssl.bundle=server"
		})).isFalse();
	}
}
