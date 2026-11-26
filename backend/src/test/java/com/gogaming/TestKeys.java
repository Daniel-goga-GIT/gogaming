package com.gogaming;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/** Par de claves RSA para firmar JWT en los tests (se genera una vez por ejecución). */
public final class TestKeys {

    private static final KeyPair KEY_PAIR;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KEY_PAIR = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private TestKeys() {
    }

    public static KeyPair keyPair() {
        return KEY_PAIR;
    }
}
