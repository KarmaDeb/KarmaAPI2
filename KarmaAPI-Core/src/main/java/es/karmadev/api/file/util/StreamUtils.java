package es.karmadev.api.file.util;

import es.karmadev.api.core.ExceptionCollector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Karma InputStream utilities
 */
@SuppressWarnings("unused")
public class StreamUtils {

    /**
     * Parse an input stream to string
     *
     * @param stream the input stream
     * @return the stream as string
     */
    public static String streamToString(final InputStream stream) {
        return streamToString(stream, false);
    }

    /**
     * Parse an input stream to string
     *
     * @param stream the input stream
     * @param autoClose close automatically the stream
     *                  once read
     * @return the stream as string
     */
    public static String streamToString(final InputStream stream, final boolean autoClose) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }

            baos.flush();
        } catch (IOException ex) {
            ExceptionCollector.catchException(StreamUtils.class, ex);
        } finally {
            if (autoClose) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    ExceptionCollector.catchException(StreamUtils.class, ex);
                }
            } else {
                try {
                    stream.reset();
                } catch (IOException ex) {
                    ExceptionCollector.catchException(StreamUtils.class, ex);
                }
            }

            try {
                baos.close();
            } catch (IOException ex) {
                ExceptionCollector.catchException(StreamUtils.class, ex);
            }
        }

        return baos.toString();
    }

    /**
     * Create a new input stream
     *
     * @param data the stream data
     * @return the new input stream
     */
    public static InputStream create(final byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(data);
            baos.flush();
        } catch (IOException ex) {
            ExceptionCollector.catchException(StreamUtils.class, ex);
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                ExceptionCollector.catchException(StreamUtils.class, ex);
            }
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Clone the input stream into a new stream
     *
     * @param stream the clone stream
     * @return the created input stream
     */
    public static InputStream clone(final InputStream stream) {
        return clone(stream, false);
    }

    /**
     * Clone the input stream into a new stream
     *
     * @param stream the clone stream
     * @param autoClose close automatically the stream
     *                  once copied
     * @return the created input stream
     */
    public static InputStream clone(final InputStream stream, final boolean autoClose) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            baos.flush();
        } catch (IOException ex) {
            ExceptionCollector.catchException(StreamUtils.class, ex);
        } finally {
            if (autoClose) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    ExceptionCollector.catchException(StreamUtils.class, ex);
                }
            } else {
                try {
                    stream.reset();
                } catch (IOException ex) {
                    ExceptionCollector.catchException(StreamUtils.class, ex);
                }
            }

            try {
                baos.close();
            } catch (IOException ex) {
                ExceptionCollector.catchException(StreamUtils.class, ex);
            }
        }

        byte[] bytes = baos.toByteArray();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Write to an input stream
     *
     * @param stream the stream
     * @param data the data to write
     * @return the created input stream
     */
    public static InputStream write(final InputStream stream, final byte[] data) {
        InputStream extra = new ByteArrayInputStream(data);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) combined.write(buffer, 0, read);
            while ((read = extra.read(buffer)) != -1) combined.write(buffer, 0, read);

            combined.flush();
        } catch (IOException ex) {
            ExceptionCollector.catchException(StreamUtils.class, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                ExceptionCollector.catchException(StreamUtils.class, ex);
            }
            try {
                combined.close();
            } catch (IOException ex) {
                ExceptionCollector.catchException(StreamUtils.class, ex);
            }
        }

        return new ByteArrayInputStream(combined.toByteArray());
    }

    /**
     * Read the stream
     *
     * @param stream the stream to read
     * @return the stream data
     */
    public static byte[] read(final InputStream stream) {
        return read(stream, 4096);
    }

    /**
     * Read the stream
     *
     * @param stream the stream to read
     * @param buffer the read buffer
     * @return the stream data
     */
    public static byte[] read(final InputStream stream, final int buffer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[buffer];
            int read;
            while ((read = stream.read(b, 0, b.length)) != -1) {
                baos.write(b, 0, read);
            }

            baos.flush();
        } catch (IOException ex) {
            ExceptionCollector.catchException(StreamUtils.class, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                ExceptionCollector.catchException(StreamUtils.class, ex);
            }
            try {
                baos.close();
            } catch (IOException ex) {
                ExceptionCollector.catchException(StreamUtils.class, ex);
            }
        }

        return baos.toByteArray();
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
