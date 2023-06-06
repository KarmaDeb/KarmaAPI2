package es.karmadev.api.core;

import es.karmadev.api.security.LockedProperties;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * KarmaAPI information
 */
@SuppressWarnings("unused")
public class KarmaAPI {

    public final static Properties properties = new LockedProperties();

    static {
        try(InputStream stream = KarmaAPI.class.getResourceAsStream("/api.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (NullPointerException | IllegalArgumentException | IOException ignored) {}
    }

    public final static String VERSION = properties.getProperty("version", "1.0.0");
    public final static String BUILD = properties.getProperty("build", "1");
    public final static String FULL_VERSION = properties.getProperty("full", "2.0.0-1");
    public final static String COMPILER = properties.getProperty("java", "8");
    public final static String COMPILE_DATE = properties.getProperty("date", "01-01-1999 00:00:00");
    public final static Supplier<String> USER_AGENT = () ->
            String.format("KarmaAPI/%s Build/%s Java/%s Identifier/%s", VERSION, BUILD, COMPILER, properties.getProperty("identifier"));

    public static boolean isTestMode() {
        try {
            String password = System.getenv("TEST_PASSWORD");
            if (password == null) return false;

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(password.getBytes());

            byte[] rs = digest.digest();
            StringBuilder hexBuilder = new StringBuilder();

            for (byte b : rs) {
                String hex = String.format("%02x", b);
                hexBuilder.append(hex);
            }

            return hexBuilder.toString().equals("43cbb23abe19f41edfd064dc50321ec15c3f385fa3584abfa187024237b48f783f34c1a00a694f74559f13634cd6f2d034b0d8ca03dd4a057a71063e923d8b38");
        } catch (NoSuchAlgorithmException ex) {
            //Should be available in all OS
        }

        return false;
    }
}
