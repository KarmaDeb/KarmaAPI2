package es.karmadev.api.file.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * Named input stream
 */
public class NamedStream extends InputStream {

    private final String name;
    private final InputStream parent;

    /**
     * Initialize the named stream
     *
     * @param name the stream name
     * @param parent the stream parent
     */
    NamedStream(final String name, final InputStream parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Get the stream name
     *
     * @return the stream name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parent input stream
     *
     * @return the parent stream
     */
    public Optional<InputStream> getParent() {
        return Optional.ofNullable(parent);
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an {@code int} in the range {@code 0} to
     * {@code 255}. If no byte is available because the end of the stream
     * has been reached, the value {@code -1} is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * <p> A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or {@code -1} if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the parent stream is not valid
     */
    @Override
    public int read() throws IOException, NullPointerException {
        if (parent == null) throw new NullPointerException("Cannot read from null input stream!");
        return parent.read();
    }

    /**
     * Reads some number of bytes from the input stream and stores them into
     * the buffer array {@code b}. The number of bytes actually read is
     * returned as an integer.  This method blocks until input data is
     * available, end of file is detected, or an exception is thrown.
     *
     * <p> If the length of {@code b} is zero, then no bytes are read and
     * {@code 0} is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at the
     * end of the file, the value {@code -1} is returned; otherwise, at
     * least one byte is read and stored into {@code b}.
     *
     * <p> The first byte read is stored into element {@code b[0]}, the
     * next one into {@code b[1]}, and so on. The number of bytes read is,
     * at most, equal to the length of {@code b}. Let <i>k</i> be the
     * number of bytes actually read; these bytes will be stored in elements
     * {@code b[0]} through {@code b[}<i>k</i>{@code -1]},
     * leaving elements {@code b[}<i>k</i>{@code ]} through
     * {@code b[b.length-1]} unaffected.
     *
     * <p> The {@code read(b)} method for class {@code InputStream}
     * has the same effect as: <pre>{@code  read(b, 0, b.length) }</pre>
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or
     * {@code -1} if there is no more data because the end of
     * the stream has been reached.
     * @throws IOException          If the first byte cannot be read for any reason
     *                              other than the end of the file, if the input stream has been
     *                              closed, or if some other I/O error occurs.
     * @throws NullPointerException if {@code b} is {@code null}.
     * @see InputStream#read(byte[], int, int)
     */
    @Override
    public int read(final byte @NotNull [] b) throws IOException, NullPointerException {
        if (parent == null) throw new NullPointerException("Cannot read from null input stream!");
        return parent.read(b);
    }

    /**
     * Reads up to {@code len} bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * {@code len} bytes, but a smaller number may be read.
     * The number of bytes actually read is returned as an integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If {@code len} is zero, then no bytes are read and
     * {@code 0} is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value {@code -1} is returned; otherwise, at least one
     * byte is read and stored into {@code b}.
     *
     * <p> The first byte read is stored into element {@code b[off]}, the
     * next one into {@code b[off+1]}, and so on. The number of bytes read
     * is, at most, equal to {@code len}. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * {@code b[off]} through {@code b[off+}<i>k</i>{@code -1]},
     * leaving elements {@code b[off+}<i>k</i>{@code ]} through
     * {@code b[off+len-1]} unaffected.
     *
     * <p> In every case, elements {@code b[0]} through
     * {@code b[off-1]} and elements {@code b[off+len]} through
     * {@code b[b.length-1]} are unaffected.
     *
     * <p> The {@code read(b, off, len)} method
     * for class {@code InputStream} simply calls the method
     * {@code read()} repeatedly. If the first such call results in an
     * {@code IOException}, that exception is returned from the call to
     * the {@code read(b,} {@code off,} {@code len)} method.  If
     * any subsequent call to {@code read()} results in a
     * {@code IOException}, the exception is caught and treated as if it
     * were end of file; the bytes read up to that point are stored into
     * {@code b} and the number of bytes read before the exception
     * occurred is returned. The default implementation of this method blocks
     * until the requested amount of input data {@code len} has been read,
     * end of file is detected, or an exception is thrown. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in array {@code b}
     *            at which the data is written.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or
     * {@code -1} if there is no more data because the end of
     * the stream has been reached.
     * @throws IOException               If the first byte cannot be read for any reason
     *                                   other than end of file, or if the input stream has been closed,
     *                                   or if some other I/O error occurs.
     * @throws NullPointerException      If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException If {@code off} is negative,
     *                                   {@code len} is negative, or {@code len} is greater than
     *                                   {@code b.length - off}
     * @see InputStream#read()
     */
    @Override
    public int read(final byte @NotNull [] b, final int off, final int len) throws IOException, NullPointerException {
        if (parent == null) throw new NullPointerException("Cannot read from null input stream!");
        return parent.read(b, off, len);
    }

    /**
     * Skips over and discards {@code n} bytes of data from this input
     * stream. The {@code skip} method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly {@code 0}.
     * This may result from any of a number of conditions; reaching end of file
     * before {@code n} bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned. If {@code n} is
     * negative, the {@code skip} method for class {@code InputStream} always
     * returns 0, and no bytes are skipped. Subclasses may handle the negative
     * value differently.
     *
     * <p> The {@code skip} method implementation of this class creates a
     * byte array and then repeatedly reads into it until {@code n} bytes
     * have been read or the end of the stream has been reached. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     * For instance, the implementation may depend on the ability to seek.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped which might be zero.
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the parent stream is null
     */
    @Override
    public long skip(final long n) throws IOException, NullPointerException {
        if (parent == null) throw new NullPointerException("Cannot read from null input stream!");
        return parent.skip(n);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking, which may be 0, or 0 when
     * end of stream is detected.  The read might be on the same thread or
     * another thread.  A single read or skip of this many bytes will not block,
     * but may read or skip fewer bytes.
     *
     * <p> Note that while some implementations of {@code InputStream} will
     * return the total number of bytes in the stream, many will not.  It is
     * never correct to use the return value of this method to allocate
     * a buffer intended to hold all data in this stream.
     *
     * <p> A subclass's implementation of this method may choose to throw an
     * {@link IOException} if this input stream has been closed by invoking the
     * {@link #close()} method.
     *
     * <p> The {@code available} method of {@code InputStream} always returns
     * {@code 0}.
     *
     * <p> This method should be overridden by subclasses.
     *
     * @return an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking or
     * {@code 0} when it reaches the end of the input stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        return (parent == null ? 0 : parent.available());
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * <p> The {@code close} method of {@code InputStream} does
     * nothing.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        if (parent != null) parent.close();
    }

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the {@code reset} method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * <p> The {@code readlimit} arguments tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     *
     * <p> The general contract of {@code mark} is that, if the method
     * {@code markSupported} returns {@code true}, the stream somehow
     * remembers all the bytes read after the call to {@code mark} and
     * stands ready to supply those same bytes again if and whenever the method
     * {@code reset} is called.  However, the stream is not required to
     * remember any data at all if more than {@code readlimit} bytes are
     * read from the stream before {@code reset} is called.
     *
     * <p> Marking a closed stream should not have any effect on the stream.
     *
     * <p> The {@code mark} method of {@code InputStream} does
     * nothing.
     *
     * @param readlimit the maximum limit of bytes that can be read before
     *                  the mark position becomes invalid.
     * @see InputStream#reset()
     */
    @Override
    public synchronized void mark(final int readlimit) {
        if (parent != null) parent.mark(readlimit);
    }

    /**
     * Repositions this stream to the position at the time the
     * {@code mark} method was last called on this input stream.
     *
     * <p> The general contract of {@code reset} is:
     *
     * <ul>
     * <li> If the method {@code markSupported} returns
     * {@code true}, then:
     *
     *     <ul><li> If the method {@code mark} has not been called since
     *     the stream was created, or the number of bytes read from the stream
     *     since {@code mark} was last called is larger than the argument
     *     to {@code mark} at that last call, then an
     *     {@code IOException} might be thrown.
     *
     *     <li> If such an {@code IOException} is not thrown, then the
     *     stream is reset to a state such that all the bytes read since the
     *     most recent call to {@code mark} (or since the start of the
     *     file, if {@code mark} has not been called) will be resupplied
     *     to subsequent callers of the {@code read} method, followed by
     *     any bytes that otherwise would have been the next input data as of
     *     the time of the call to {@code reset}. </ul>
     *
     * <li> If the method {@code markSupported} returns
     * {@code false}, then:
     *
     *     <ul><li> The call to {@code reset} may throw an
     *     {@code IOException}.
     *
     *     <li> If an {@code IOException} is not thrown, then the stream
     *     is reset to a fixed state that depends on the particular type of the
     *     input stream and how it was created. The bytes that will be supplied
     *     to subsequent callers of the {@code read} method depend on the
     *     particular type of the input stream. </ul></ul>
     *
     * <p>The method {@code reset} for class {@code InputStream}
     * does nothing except throw an {@code IOException}.
     *
     * @throws IOException if this stream has not been marked or if the
     *                     mark has been invalidated.
     * @see InputStream#mark(int)
     * @see IOException
     */
    @Override
    public synchronized void reset() throws IOException {
        if (parent != null) parent.reset();
    }

    /**
     * Tests if this input stream supports the {@code mark} and
     * {@code reset} methods. Whether or not {@code mark} and
     * {@code reset} are supported is an invariant property of a
     * particular input stream instance. The {@code markSupported} method
     * of {@code InputStream} returns {@code false}.
     *
     * @return {@code true} if this stream instance supports the mark
     * and reset methods; {@code false} otherwise.
     * @see InputStream#mark(int)
     * @see InputStream#reset()
     */
    @Override
    public boolean markSupported() {
        return parent != null && parent.markSupported();
    }

    /**
     * Create a new named stream from the
     * specified stream
     *
     * @param name the stream name
     * @param parent the parent stream
     * @return the named stream
     */
    public static NamedStream newStream(final String name, final InputStream parent) {
        return new NamedStream(name, parent);
    }
}
