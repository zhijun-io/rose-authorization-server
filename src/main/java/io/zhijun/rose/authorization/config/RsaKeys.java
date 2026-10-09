package io.zhijun.rose.authorization.config;

import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

final class RsaKeys {
    private static final Logger logger = LoggerFactory.getLogger(RsaKeys.class);

    private RsaKeys() {
    }

    static RSAKey getRsaKey(boolean random) {
        return getRsaKey(random, null, null);
    }

    static RSAKey getRsaKey(boolean random, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        if (random) {
            KeyPair keyPair = generateRsaKey();
            return getRsaKey((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
        }

        if (publicKey!=null && privateKey!=null) {
            return getRsaKey(publicKey, privateKey);
        } else {
            KeyPair keyPair = loadRsaKey();
            return getRsaKey((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
        }
    }

    static RSAKey getRsaKey(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        return rsaKey;
    }

    static KeyPair loadRsaKey() {
        logger.info("Loading hardcoded RSA key for token signatures");

        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            byte[] privateKeyBytes = readPrivateKeyBytes();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = factory.generatePrivate(privateKeySpec);
            byte[] publicKeyBytes = readPublicKeyBytes();
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = factory.generatePublic(publicKeySpec);
            return new KeyPair(publicKey, privateKey);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not load RSA key from file", ex);
        }
    }

    private static byte[] readPrivateKeyBytes() {
        String privateKeyText = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC9sNWdlbfg2FIF\n3CAxttJr+p0k29acpDQaN2FK+ZNt2cnXJ1Exjozv/7r0keefim5L7g2wZ1mFxEeC\ngBQwZ5baP0VApKk89gjVLr9sVVQNE5JUEuGt0QaKL9vT7YgdIIPD/o+kNkt5kAdw\nktN+lyJdvDFgmsAhQgJwG75yQk/EfaNxLa4iHx8rkoOb7AOyu9M0gxPEf5yPoTw4\nlVwU0839koF9XHTjmw9kSBmMn4gX4lJqOGDUB/5X5NU6XHoEzgxuMx02eI95Hq1i\nHcKjHozTX0PhkOpjhkoLAFa/iIdD+ypAscPWPf6oLCvVJUKZ/Z4tIWysvO1ln5C2\nuw603EtxAgMBAAECggEAAnXqZXWJWbYwpTEQbVip5NruwilEYMqL/vmQLmbro8mH\nEd6Jkfcfiqbyc8T3QFVtPhinzo2epoqv40haSrDzTuAye6bPJ7Jo1awaMVNviE5k\nT+khSo4LM8xwYXDDut3+u5KAJkEKjFiDEjGjYvFesaDU8qP7QUswCSiA+U41Ju81\nDyZq/Ba15+nOIJxTi2s0YNbbAPzOFtYgFQ4Vq0h3aQKTj23DNKliF/VKXYPAAChz\nHGRGMTre9MO3a5FbGauX0EfuyhRMoVC71unK0oJSTdYnz1b7suAO3dCEGZiNKjls\nCzcY6k7hNUYc6ay0dDbpFc7Pldppd0x5/VlGkalqgQKBgQDpfEHmjUrxtm3HbSE5\n0oZtMeyeEKXj66INGyISW4797fi5JC3ixC7pO1LIrM9k4cLvuKLfIke6siO4ckiY\n2yUI8UY9OgatbL8JN0FZeay6rFLEYBnTIIgFvCIXA57uUDQa9czfJO+JqD5rJWz/\ngtKRFSemc00/siPq3RgnfhEXsQKBgQDP+3o/9PtTjRhgXOEZ0/AMcp0hxYY1kV/8\nIbqWvY6ILDZZpKGZtiZl0mrsbhnVxdpYY9np4oPxsuB8y3Uh3FLurCqfnY++tL9a\nzCciMTFvVWXQn+d6E1/pVElKIIU0SoPdOcMqNRNuI+4l7kg9ZKzcnvOrwCfzWYl9\nBiliK4sfwQKBgQC6VW4KXsxmpp5pv0/642Zgkq0xYDvj71L+fp7sY6F0SYxrxNHR\n3ZtKiwuAF1nUOs/lnEZvKN5xwmT7eEkzpACkaFkT19EoalbzNeOzUoVk3M2Y5tD1\nS0sCuKfEEiGuMtfChFOh6co448ocnFumdnMxUd642d/Wa4Z6k0QJkHR3oQKBgFh8\nqu+JIGDU1/kvqwndFPWG0fmrW0VxO4A/LW6y6XBgNyN+ms6WI9IQazN6SH2eNx6C\nJRWQLQjpTP9rZMNqRNKM53mPhLfjmMJdt4yhl2HB2JahYc+bbKQOGzxxh5rO1TmL\nONC9Ui37FXiH365XJgCblBqPn6+2eXt64qKE2iVBAoGAWiC85aiG3qoHGEnxp7Pd\nMWiHHzqwg9jMxxnY1jwKCk4Y+Ftju+rxeNnHKGfu5i/rIwvTSy9uf13LLiNU9CbF\nS9slvjXiMbV5jJFAdAmvzlRt6DH//Yt5s3OPWliRPJb/pT/gg2wwVFgAqplgD/M5\nvIECiVzgW6EyeOrN3mSCRXg=\n".strip().replace(System.lineSeparator(), "");
        return Base64.getDecoder().decode(privateKeyText);
    }

    private static byte[] readPublicKeyBytes() {
        String publicKeyText = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvbDVnZW34NhSBdwgMbbS\na/qdJNvWnKQ0GjdhSvmTbdnJ1ydRMY6M7/+69JHnn4puS+4NsGdZhcRHgoAUMGeW\n2j9FQKSpPPYI1S6/bFVUDROSVBLhrdEGii/b0+2IHSCDw/6PpDZLeZAHcJLTfpci\nXbwxYJrAIUICcBu+ckJPxH2jcS2uIh8fK5KDm+wDsrvTNIMTxH+cj6E8OJVcFNPN\n/ZKBfVx045sPZEgZjJ+IF+JSajhg1Af+V+TVOlx6BM4MbjMdNniPeR6tYh3Cox6M\n019D4ZDqY4ZKCwBWv4iHQ/sqQLHD1j3+qCwr1SVCmf2eLSFsrLztZZ+QtrsOtNxL\ncQIDAQAB\n".strip().replace(System.lineSeparator(), "");
        return Base64.getDecoder().decode(publicKeyText);
    }

    static KeyPair generateRsaKey() {
        logger.info("Generating random RSA key for token signatures");

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
