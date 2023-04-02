package es.karmadev.api.file.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Karma InputStream utilities
 */
public class StreamUtils {

    /**
     * Parse an input stream to string
     *
     * @param stream the input stream
     * @return the stream as string
     * @throws IOException as part of {@link AutoCloseable#close()}
     */
    public static String streamToString(final InputStream stream) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) builder.append(line).append("\n");

            return builder.substring(0, Math.max(0, builder.length() - 1));
        }
    }

    /**
     * Create a new input stream
     *
     * @param data the stream data
     * @return the new input stream
     * @throws IOException as part of {@link OutputStream#write(byte[])} and {@link OutputStream#flush()}
     */
    public static InputStream create(final byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(data);
        out.flush();

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Clone the input stream into a new stream
     *
     * @param stream the clone stream
     * @return the created input stream
     * @throws IOException as part of {@link InputStream#read(byte[])} and {@link OutputStream#flush()}
     */
    public static InputStream clone(final InputStream stream) throws IOException {
        return clone(stream, true);
    }

    /**
     * Clone the input stream into a new stream
     *
     * @param stream the clone stream
     * @param autoClose close automatically the stream
     *                  once copied
     * @return the created input stream
     * @throws IOException as part of {@link InputStream#read(byte[])} and {@link OutputStream#flush()}
     */
    public static InputStream clone(final InputStream stream, final boolean autoClose) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        baos.flush();
        stream.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Write to an input stream
     *
     * @param stream the stream
     * @param data the data to write
     * @return the created input stream
     * @throws IOException as part of {@link InputStream#read(byte[])} and {@link OutputStream#flush()}
     */
    public static InputStream write(final InputStream stream, final byte[] data) throws IOException {
        InputStream extra = new ByteArrayInputStream(data);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) != -1) combined.write(buffer, 0, read);
        while ((read = extra.read(buffer)) != -1) combined.write(buffer, 0, read);
        combined.flush();

        return new ByteArrayInputStream(combined.toByteArray());
    }

    /**
     * Check if an input stream is open
     *
     * @param stream the stream
     * @return if the stream is open
     */
    public static boolean isOpen(final InputStream stream) {
        try {
            return stream != null && stream.available() > 0;
        } catch (IOException ex) {
            return false;
        }
    }
}
