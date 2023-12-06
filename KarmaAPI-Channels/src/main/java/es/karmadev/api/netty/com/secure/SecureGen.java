package es.karmadev.api.netty.com.secure;

import es.karmadev.api.core.ExceptionCollector;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

/**
 * Secure keys generation
 */
public class SecureGen {

    public final static String PAIR_ALGORITHM = "RSA";
    public final static String SECRET_ALGORITHM = "AES";

    /**
     * Generate key pairs
     *
     * @return the key pairs
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(PAIR_ALGORITHM);
            generator.initialize(4096);

            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            ExceptionCollector.catchException(SecureGen.class, ex);
        }

        return null;
    }

    /**
     * Generate a secret key
     *
     * @return the secret key
     */
    public static SecretKey generateSecret() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(SECRET_ALGORITHM);
            generator.init(256);

            return generator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            ExceptionCollector.catchException(SecureGen.class, ex);
        }

        return null;
    }

    /**
     * Protect a key
     *
     * @param key the key to protect
     * @param encoder the key encoder
     * @return the encrypted key
     */
    public static byte[] protectKey(final SecretKey key, final PublicKey encoder) {
        try {
            Cipher cipher = Cipher.getInstance(PAIR_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, encoder);

            return cipher.doFinal(key.getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                    | IllegalBlockSizeException | BadPaddingException ex) {
            ExceptionCollector.catchException(SecureGen.class, ex);
        }

        return null;
    }

    /**
     * Decode a key
     *
     * @param encoded the encoded key
     * @param decoder the key decoder
     * @return the decrypted key
     */
    public static SecretKey decodeKey(final byte[] encoded, final PrivateKey decoder) {
        try {
            Cipher cipher = Cipher.getInstance(PAIR_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, decoder);

            byte[] decoded = cipher.doFinal(encoded);
            return new SecretKeySpec(decoded, 0, decoded.length, SECRET_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | IllegalBlockSizeException | BadPaddingException ex) {
            ExceptionCollector.catchException(SecureGen.class, ex);
        }

        return null;
    }
}
