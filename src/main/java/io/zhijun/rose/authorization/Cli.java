package io.zhijun.rose.authorization;

import io.zhijun.rose.authorization.config.ConfigurationPrinter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public final class Cli {
    private static final String HELP_FLAG = "--help";
    private static final String HELP_FLAG_SHORT = "-h";
    private static final String CONFIG_FLAG = "--config";
    private static final String PRINT_SAMPLE_CONFIG_FLAG = "--print-sample-config";

    private Cli() {
    }

    public static boolean checkHelpFlagAndPrintUsage(String[] args) {
        boolean hasHelpFlag = Arrays.stream(args)
                .anyMatch((arg) -> HELP_FLAG.equalsIgnoreCase(arg) || HELP_FLAG_SHORT.equalsIgnoreCase(arg));
        if (hasHelpFlag) {
            ConfigurationPrinter.printHelp();
        }

        return hasHelpFlag;
    }

    public static boolean checkSampleConfigFlagAndPrint(String[] args) {
        boolean hasSampleConfigFlag = Arrays.stream(args).anyMatch(PRINT_SAMPLE_CONFIG_FLAG::equalsIgnoreCase);
        if (hasSampleConfigFlag) {
            ConfigurationPrinter.printSampleConfiguration();
        }

        return hasSampleConfigFlag;
    }

    public static void setupSpringConfigLocation(String[] args) {
        if (args!=null) {
            if (Arrays.stream(args).filter(argx -> argx.startsWith(CONFIG_FLAG)).count() > 1L) {
                throw new MultipleFlagsException();
            }
            boolean hasConfig = false;
            String configLocation = null;

            for (String arg : args) {
                if (hasConfig) {
                    configLocation = arg;
                    break;
                }

                if (arg.equals(CONFIG_FLAG)) {
                    hasConfig = true;
                } else if (arg.startsWith("--config=")) {
                    configLocation = arg.substring("--config=".length());
                    break;
                }
            }

            if (configLocation!=null) {
                if (!Paths.get(configLocation).toFile().exists()) {
                    throw new MissingConfigFileException(configLocation);
                } else {
                    System.setProperty("spring.config.location", "classpath:/application.yml," + configLocation);
                }
            }
        }
    }

    public static boolean checkUnknownFlagsAndPrintError(String[] flags) {
        Set<String> knownFlags = Set.of(HELP_FLAG, HELP_FLAG_SHORT, CONFIG_FLAG, PRINT_SAMPLE_CONFIG_FLAG);

        for (String flag : flags) {
            if (!flag.startsWith("-")) {
                continue;
            }
            String name = flag.split("=", 2)[0].toLowerCase(Locale.getDefault());
            if (knownFlags.contains(name) || isPassthroughBootProperty(name)) {
                continue;
            }
            ConfigurationPrinter.printUnknownFlag(flag);
            return true;
        }

        return false;
    }

    /**
     * Allow Spring Boot / server property flags (e.g. {@code --server.ssl.*}) so TLS overlays
     * and Compose can pass configuration without going through {@code --config} alone.
     */
    static boolean isPassthroughBootProperty(String flagName) {
        return flagName.startsWith("--spring.")
                || flagName.startsWith("--server.")
                || flagName.startsWith("--logging.")
                || flagName.startsWith("--management.");
    }

    static class MultipleFlagsException extends SsoException {
        MultipleFlagsException() {
            super("Multiple %s flags found. Only a single %s flag is allowed.".formatted(CONFIG_FLAG, CONFIG_FLAG));
        }
    }

    static class MissingConfigFileException extends SsoException {
        MissingConfigFileException(String configLocation) {
            super("Config file [%s] does not exist.".formatted(configLocation));
        }
    }

    static class SsoException extends RuntimeException {
        SsoException(String message) {
            super(message);
        }
    }
}
