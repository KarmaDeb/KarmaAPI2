package es.karmadev.api.file.serializer.serialized;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.file.serializer.exception.NonFileException;
import es.karmadev.api.file.util.PathUtilities;
import lombok.Getter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Serialized file
 */
public class SerializedFile implements Serializable {

    @Getter
    private final long length;
    @Getter
    private final byte[] data;
    @Getter
    private final String name;
    @Getter
    private final String path;
    @Getter
    private final String sha;

    /**
     * Initialize the serialized file
     *
     * @param path the file path
     * @param file the file
     * @throws NonFileException if the file is not a file
     * @throws NoSuchAlgorithmException if the JVM is unable to generate the file SHA256
     * @throws IOException if the JVM fails to calculate the file size
     */
    public SerializedFile(final String path, final Path file) throws NonFileException, NoSuchAlgorithmException, IOException {
        if (Files.isDirectory(file)) throw new NonFileException(file);

        length = Files.size(file);
        data = PathUtilities.readBytes(file);
        this.path = path;
        name = file.getFileName().toString();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] dataHash = digest.digest(data);
        sha = bytesToHex(dataHash);
    }

    /**
     * Restore the file
     *
     * @param replace replace the file if the sha value
     *                is the same
     * @return if the file could be restored
     */
    public boolean restore(final Path destination, final boolean replace) {
        if (Files.isDirectory(destination)) {
            try {
                Path dest = destination.resolve(name);
                if (!path.equals("/") && path.contains("/")) {
                    dest = destination;

                    String[] pathData = path.split("/");
                    for (String dir : pathData) dest = dest.resolve(dir);
                    dest = dest.resolve(name);
                }

                boolean restore = true;
                if (Files.exists(dest)) {
                    byte[] data = PathUtilities.readBytes(dest);

                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] dataHash = digest.digest(data);
                    String sha = bytesToHex(dataHash);

                    restore = !sha.equals(this.sha) || replace;
                }

                if (restore) {
                    PathUtilities.write(dest, data);
                    return true;
                }
            } catch (NoSuchAlgorithmException ex) {
                ExceptionCollector.catchException(SerializedFile.class, ex);
            }
        }

        return false;
    }

    /**
     * Parse the bytes into a hex string
     *
     * @param bytes the bytes to parse
     * @return the bytes as a hex string
     */
    private String bytesToHex(byte[] bytes) {
        char[] hexCodes = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexCodes[v >>> 4];
            hexChars[i * 2 + 1] = hexCodes[v & 0x0F];
        }
        return new String(hexChars);
    }
}
