package io.zhijun.rose.authorization;

import io.zhijun.rose.authorization.config.ServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServerProperties.class})
public class AuthorizationServerApplication {
    public static void main(String[] args) {
        if (Cli.checkHelpFlagAndPrintUsage(args)) {
            systemExit(0);
        }

        if (Cli.checkSampleConfigFlagAndPrint(args)) {
            systemExit(0);
        }

        if (Cli.checkUnknownFlagsAndPrintError(args)) {
            systemExit(1);
        }

        try {
            Cli.setupSpringConfigLocation(args);
        } catch (Exception e) {
            System.out.println("\n\ud83d\udea8 " + e.getMessage() + "\n");
            throw new RuntimeException(e);
        }

        setupAot();
        SpringApplication.run(AuthorizationServerApplication.class, args);
    }

    public static void systemExit(int status) {
        System.exit(status);
    }

    private static void setupAot() {
        if (System.getProperty("spring.aot.enabled")==null) {
            try {
                Class.forName("io.zhijun.rose.authorization.AuthorizationServerApplication__ApplicationContextInitializer",
                        false, AuthorizationServerApplication.class.getClassLoader());
                System.setProperty("spring.aot.enabled", "true");
            } catch (ClassNotFoundException var1) {
            }

        }
    }
}
