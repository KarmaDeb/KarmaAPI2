package es.karmadev.api.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * KarmaAPI information
 */
@SuppressWarnings("unused")
public class KarmaAPI {

    public final static Properties properties = new Properties();
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
    public final static String USER_AGENT = String.format("KarmaAPI/%s Build/%s Java/%s", VERSION, BUILD, COMPILER);
}
