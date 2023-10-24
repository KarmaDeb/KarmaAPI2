package es.karmadev.api.web;

import es.karmadev.api.web.request.RequestData;
import lombok.Getter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

/**
 *  URL connection wrapper
 */
@Getter
@SuppressWarnings("unused")
public class URLConnectionWrapper extends URLConnection implements AutoCloseable {

    /**
     * -- GETTER --
     *  Get the unsecure connection
     */
    private final HttpURLConnection unsecureConnection;
    /**
     * -- GETTER --
     *  Get the secure connection
     */
    private final HttpsURLConnection secureConnection;

    /**
     * Creates a new wrapped connection from the
     * url connection
     *
     * @param connection the url connection
     */
    protected URLConnectionWrapper(final URLConnection connection) {
        super(connection.getURL());
        if (connection instanceof HttpsURLConnection) {
            secureConnection = (HttpsURLConnection) connection;
            unsecureConnection = null;
        } else {
            secureConnection = null;
            unsecureConnection = (HttpURLConnection) connection;
        }
    }

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     * @throws IOException if the connection fails to open
     */
    protected URLConnectionWrapper(final URL url) throws IOException {
        super(url);
        URLConnection realConnection = url.openConnection();
        if (url.toString().startsWith("https")) {
            secureConnection = (HttpsURLConnection) realConnection;
            unsecureConnection = null;
        } else {
            secureConnection = null;
            unsecureConnection = (HttpURLConnection) realConnection;
        }
    }

    /**
     * Returns the server's principal which was established as part of
     * defining the session.
     * <p>
     * Note: Subclasses should override this method. If not overridden, it
     * will default to returning the X500Principal of the server's end-entity
     * certificate for certificate-based ciphersuites, or throw an
     * SSLPeerUnverifiedException for non-certificate based ciphersuites,
     * such as Kerberos.
     *
     * @return the server's principal. Returns an X500Principal of the
     * end-entity certiticate for X509-based cipher suites, and
     * KerberosPrincipal for Kerberos cipher suites.
     * @throws SSLPeerUnverifiedException if the peer was not verified
     * @throws IllegalStateException      if this method is called before
     *                                    the connection has been established.
     * @see #getServerCertificates()
     * @see #getLocalPrincipal()
     * @since 1.5
     */
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (secureConnection != null) return secureConnection.getPeerPrincipal();
        return null;
    }

    /**
     * Returns the principal that was sent to the server during handshaking.
     * <p>
     * Note: Subclasses should override this method. If not overridden, it
     * will default to returning the X500Principal of the end-entity certificate
     * that was sent to the server for certificate-based ciphersuites or,
     * return null for non-certificate based ciphersuites, such as Kerberos.
     *
     * @return the principal sent to the server. Returns an X500Principal
     * of the end-entity certificate for X509-based cipher suites, and
     * KerberosPrincipal for Kerberos cipher suites. If no principal was
     * sent, then null is returned.
     * @throws IllegalStateException if this method is called before
     *                               the connection has been established.
     * @see #getLocalCertificates()
     * @see #getPeerPrincipal()
     * @since 1.5
     */
    public Principal getLocalPrincipal() {
        if (secureConnection != null) return secureConnection.getLocalPrincipal();
        return null;
    }

    /**
     * Sets the <code>HostnameVerifier</code> for this instance.
     * <p>
     * New instances of this class inherit the default static hostname
     * verifier set by .  Calls to this method replace
     * this object's <code>HostnameVerifier</code>.
     *
     * @param v the host name verifier
     * @throws IllegalArgumentException if the <code>HostnameVerifier</code>
     *                                  parameter is null.
     * @see #getHostnameVerifier()
     */
    public void setHostnameVerifier(final HostnameVerifier v) {
        if (secureConnection != null) secureConnection.setHostnameVerifier(v);
    }

    /**
     * Gets the <code>HostnameVerifier</code> in place on this instance.
     *
     * @return the host name verifier
     * @see #setHostnameVerifier(HostnameVerifier)
     */
    public HostnameVerifier getHostnameVerifier() {
        if (secureConnection != null) return secureConnection.getHostnameVerifier();
        return null;
    }

    /**
     * Sets the <code>SSLSocketFactory</code> to be used when this instance
     * creates sockets for secure https URL connections.
     * <p>
     * New instances of this class inherit the default static
     * <code>SSLSocketFactory</code> set by
     * .  Calls to this method replace
     * this object's <code>SSLSocketFactory</code>.
     *
     * @param sf the SSL socket factory
     * @throws IllegalArgumentException if the <code>SSLSocketFactory</code>
     *                                  parameter is null.
     * @throws SecurityException        if a security manager exists and its
     *                                  <code>checkSetFactory</code> method does not allow
     *                                  a socket factory to be specified.
     * @see #getSSLSocketFactory()
     */
    public void setSSLSocketFactory(final SSLSocketFactory sf) {
        if (secureConnection != null) secureConnection.setSSLSocketFactory(sf);
    }

    /**
     * Gets the SSL socket factory to be used when creating sockets
     * for secure https URL connections.
     *
     * @return the <code>SSLSocketFactory</code>
     * @see #setSSLSocketFactory(SSLSocketFactory)
     */
    public SSLSocketFactory getSSLSocketFactory() {
        if (secureConnection != null) return secureConnection.getSSLSocketFactory();
        return null;
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is known in
     * advance.
     * <p>
     * An exception will be thrown if the application
     * attempts to write more data than the indicated
     * content-length, or if the application closes the OutputStream
     * before writing the indicated amount.
     * <p>
     * When output streaming is enabled, authentication
     * and redirection cannot be handled automatically.
     * A HttpRetryException will be thrown when reading
     * the response if authentication or redirection are required.
     * This exception can be queried for the details of the error.
     * <p>
     * This method must be called before the URLConnection is connected.
     * <p>
     * <B>NOTE:</B> {@link #setFixedLengthStreamingMode(long)} is recommended
     * instead of this method as it allows larger content lengths to be set.
     *
     * @param contentLength The number of bytes which will be written
     *                      to the OutputStream.
     * @throws IllegalStateException    if URLConnection is already connected
     *                                  or if a different streaming mode is already enabled.
     * @throws IllegalArgumentException if a content length less than
     *                                  zero is specified.
     * @see #setChunkedStreamingMode(int)
     * @since 1.5
     */
    public void setFixedLengthStreamingMode(final int contentLength) {
        if (unsecureConnection != null) unsecureConnection.setFixedLengthStreamingMode(contentLength);
        if (secureConnection != null) secureConnection.setFixedLengthStreamingMode(contentLength);
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is known in
     * advance.
     *
     * <P> An exception will be thrown if the application attempts to write
     * more data than the indicated content-length, or if the application
     * closes the OutputStream before writing the indicated amount.
     *
     * <P> When output streaming is enabled, authentication and redirection
     * cannot be handled automatically. A {@linkplain HttpRetryException} will
     * be thrown when reading the response if authentication or redirection
     * are required. This exception can be queried for the details of the
     * error.
     *
     * <P> This method must be called before the URLConnection is connected.
     *
     * <P> The content length set by invoking this method takes precedence
     * over any value set by {@link #setFixedLengthStreamingMode(int)}.
     *
     * @param contentLength The number of bytes which will be written to the OutputStream.
     * @throws IllegalStateException    if URLConnection is already connected or if a different
     *                                  streaming mode is already enabled.
     * @throws IllegalArgumentException if a content length less than zero is specified.
     * @since 1.7
     */
    public void setFixedLengthStreamingMode(final long contentLength) {
        if (unsecureConnection != null) unsecureConnection.setFixedLengthStreamingMode(contentLength);
        if (secureConnection != null) secureConnection.setFixedLengthStreamingMode(contentLength);
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is <b>not</b>
     * known in advance. In this mode, chunked transfer encoding
     * is used to send the request body. Note, not all HTTP servers
     * support this mode.
     * <p>
     * When output streaming is enabled, authentication
     * and redirection cannot be handled automatically.
     * A HttpRetryException will be thrown when reading
     * the response if authentication or redirection are required.
     * This exception can be queried for the details of the error.
     * <p>
     * This method must be called before the URLConnection is connected.
     *
     * @param chunklen The number of bytes to write in each chunk.
     *                 If chunklen is less than or equal to zero, a default
     *                 value will be used.
     * @throws IllegalStateException if URLConnection is already connected
     *                               or if a different streaming mode is already enabled.
     * @see #setFixedLengthStreamingMode(int)
     * @since 1.5
     */
    public void setChunkedStreamingMode(final int chunklen) {
        if (unsecureConnection != null) unsecureConnection.setChunkedStreamingMode(chunklen);
        if (secureConnection != null) secureConnection.setChunkedStreamingMode(chunklen);
    }

    /**
     * Sets whether HTTP redirects (requests with response code 3xx) should
     * be automatically followed by this {@code HttpURLConnection}
     * instance.
     * <p>
     * The default value comes from followRedirects, which defaults to
     * true.
     *
     * @param followRedirects a {@code boolean} indicating
     *                        whether or not to follow HTTP redirects.
     * @see #getInstanceFollowRedirects
     * @since 1.3
     */
    public void setInstanceFollowRedirects(final boolean followRedirects) {
        if (unsecureConnection != null) unsecureConnection.setInstanceFollowRedirects(followRedirects);
        if (secureConnection != null) secureConnection.setInstanceFollowRedirects(followRedirects);
    }

    /**
     * Returns the value of this {@code HttpURLConnection}'s
     * {@code instanceFollowRedirects} field.
     *
     * @return the value of this {@code HttpURLConnection}'s
     * {@code instanceFollowRedirects} field.
     * @see #setInstanceFollowRedirects(boolean)
     * @since 1.3
     */
    public boolean getInstanceFollowRedirects() {
        if (unsecureConnection != null) return unsecureConnection.getInstanceFollowRedirects();
        return secureConnection != null && secureConnection.getInstanceFollowRedirects();
    }

    /**
     * Set the method for the URL request, one of:
     * <UL>
     * <LI>GET
     * <LI>POST
     * <LI>HEAD
     * <LI>OPTIONS
     * <LI>PUT
     * <LI>DELETE
     * <LI>TRACE
     * </UL> are legal, subject to protocol restrictions.  The default
     * method is GET.
     *
     * @param method the HTTP method
     * @throws ProtocolException if the method cannot be reset or if
     *                           the requested method isn't valid for HTTP.
     * @throws SecurityException if a security manager is set and the
     *                           method is "TRACE", but the "allowHttpTrace"
     *                           NetPermission is not granted.
     * @see #getRequestMethod()
     */
    public void setRequestMethod(final String method) throws ProtocolException {
        if (unsecureConnection != null) unsecureConnection.setRequestMethod(method);
        if (secureConnection != null) secureConnection.setRequestMethod(method);
    }

    /**
     * Get the request method.
     *
     * @return the HTTP request method
     * @see #setRequestMethod(String)
     */
    public String getRequestMethod() {
        if (unsecureConnection != null) return unsecureConnection.getRequestMethod();
        if (secureConnection != null) return secureConnection.getRequestMethod();
        return "GET";
    }

    /**
     * Gets the status code from an HTTP response message.
     * For example, in the case of the following status lines:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * It will return 200 and 401 respectively.
     * Returns -1 if no code can be discerned
     * from the response (i.e., the response is not valid HTTP).
     *
     * @return the HTTP Status-Code, or -1
     * @throws IOException if an error occurred connecting to the server.
     */
    public int getResponseCode() throws IOException {
        if (unsecureConnection != null) return unsecureConnection.getResponseCode();
        if (secureConnection != null) return secureConnection.getResponseCode();
        return 500;
    }

    /**
     * Gets the HTTP response message, if any, returned along with the
     * response code from a server.  From responses like:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 404 Not Found
     * </PRE>
     * Extracts the Strings "OK" and "Not Found" respectively.
     * Returns null if none could be discerned from the responses
     * (the result was not valid HTTP).
     *
     * @return the HTTP response message, or {@code null}
     * @throws IOException if an error occurred connecting to the server.
     */
    public String getResponseMessage() throws IOException {
        if (unsecureConnection != null) return unsecureConnection.getResponseMessage();
        if (secureConnection != null) return secureConnection.getResponseMessage();
        return null;
    }

    /**
     * Returns the error stream if the connection failed
     * but the server sent useful data nonetheless. The
     * typical example is when an HTTP server responds
     * with a 404, which will cause a FileNotFoundException
     * to be thrown in connect, but the server sent an HTML
     * help page with suggestions as to what to do.
     *
     * <p>This method will not cause a connection to be initiated.  If
     * the connection was not connected, or if the server did not have
     * an error while connecting or if the server had an error but
     * no error data was sent, this method will return null. This is
     * the default.
     *
     * @return an error stream if any, null if there have been no
     * errors, the connection is not connected or the server sent no
     * useful data.
     */
    public InputStream getErrorStream() {
        if (unsecureConnection != null) return unsecureConnection.getErrorStream();
        if (secureConnection != null) return secureConnection.getErrorStream();
        return null;
    }

    /**
     * Returns the cipher suite in use on this connection.
     *
     * @return the cipher suite
     * @throws IllegalStateException if this method is called before
     *                               the connection has been established.
     */
    public String getCipherSuite() {
        if (secureConnection != null) return secureConnection.getCipherSuite();
        return null;
    }

    /**
     * Returns the certificate(s) that were sent to the server during
     * handshaking.
     * <p>
     * Note: This method is useful only when using certificate-based
     * cipher suites.
     * <p>
     * When multiple certificates are available for use in a
     * handshake, the implementation chooses what it considers the
     * "best" certificate chain available, and transmits that to
     * the other side.  This method allows the caller to know
     * which certificate chain was actually sent.
     *
     * @return an ordered array of certificates,
     * with the client's own certificate first followed by any
     * certificate authorities.  If no certificates were sent,
     * then null is returned.
     * @throws IllegalStateException if this method is called before
     *                               the connection has been established.
     */
    public Certificate[] getLocalCertificates() {
        if (secureConnection != null) return secureConnection.getLocalCertificates();
        return new Certificate[0];
    }

    /**
     * Returns the server's certificate chain which was established
     * as part of defining the session.
     * <p>
     * Note: This method can be used only when using certificate-based
     * cipher suites; using it with non-certificate-based cipher suites,
     * such as Kerberos, will throw an SSLPeerUnverifiedException.
     *
     * @return an ordered array of server certificates,
     * with the peer's own certificate first followed by
     * any certificate authorities.
     * @throws SSLPeerUnverifiedException if the peer is not verified.
     * @throws IllegalStateException      if this method is called before
     *                                    the connection has been established.
     */
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        if (secureConnection != null) return secureConnection.getServerCertificates();
        return new Certificate[0];
    }

    /**
     * Opens a communications link to the resource referenced by this
     * URL, if such a connection has not already been established.
     * <p>
     * If the {@code connect} method is called when the connection
     * has already been opened (indicated by the {@code connected}
     * field having the value {@code true}), the call is ignored.
     * <p>
     * URLConnection objects go through two phases: first they are
     * created, then they are connected.  After being created, and
     * before being connected, various options can be specified
     * (e.g., doInput and UseCaches).  After connecting, it is an
     * error to try to set them.  Operations that depend on being
     * connected, like getContentLength, will implicitly perform the
     * connection, if necessary.
     *
     * @throws SocketTimeoutException if the timeout expires before
     *                                the connection can be established
     * @throws IOException            if an I/O error occurs while opening the
     *                                connection.
     * @see URLConnection#connected
     * @see #getConnectTimeout()
     * @see #setConnectTimeout(int)
     */
    @Override
    public void connect() throws IOException {
        if (unsecureConnection != null) unsecureConnection.connect();
        if (secureConnection != null) secureConnection.connect();
    }

    /**
     * Indicates that other requests to the server
     * are unlikely in the near future. Calling disconnect()
     * should not imply that this HttpURLConnection
     * instance can be reused for other requests.
     */
    public void disconnect() {
        if (unsecureConnection != null) unsecureConnection.disconnect();
        if (secureConnection != null) secureConnection.disconnect();
    }

    /**
     * Indicates if the connection is going through a proxy.
     *
     * @return a boolean indicating if the connection is
     * using a proxy.
     */
    public boolean usingProxy() {
        if (unsecureConnection != null) return unsecureConnection.usingProxy();
        return secureConnection != null && secureConnection.usingProxy();
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used
     * when opening a communications link to the resource referenced
     * by this URLConnection.  If the timeout expires before the
     * connection can be established, a
     * java.net.SocketTimeoutException is raised. A timeout of zero is
     * interpreted as an infinite timeout.
     *
     * <p> Some non-standard implementation of this method may ignore
     * the specified timeout. To see the connect timeout set, please
     * call getConnectTimeout().
     *
     * @param timeout an {@code int} that specifies the connect
     *                timeout value in milliseconds
     * @throws IllegalArgumentException if the timeout parameter is negative
     * @see #getConnectTimeout()
     * @see #connect()
     * @since 1.5
     */
    @Override
    public void setConnectTimeout(final int timeout) {
        if (unsecureConnection != null) unsecureConnection.setConnectTimeout(timeout);
        if (secureConnection != null) secureConnection.setConnectTimeout(timeout);
        super.setConnectTimeout(timeout);
    }

    /**
     * Returns setting for connect timeout.
     * <p>
     * 0 return implies that the option is disabled
     * (i.e., timeout of infinity).
     *
     * @return an {@code int} that indicates the connect timeout
     * value in milliseconds
     * @see #setConnectTimeout(int)
     * @see #connect()
     * @since 1.5
     */
    @Override
    public int getConnectTimeout() {
        if (unsecureConnection != null) return unsecureConnection.getConnectTimeout();
        if (secureConnection != null) return secureConnection.getConnectTimeout();
        return super.getConnectTimeout();
    }

    /**
     * Sets the read timeout to a specified timeout, in
     * milliseconds. A non-zero value specifies the timeout when
     * reading from Input stream when a connection is established to a
     * resource. If the timeout expires before there is data available
     * for read, a java.net.SocketTimeoutException is raised. A
     * timeout of zero is interpreted as an infinite timeout.
     *
     * <p> Some non-standard implementation of this method ignores the
     * specified timeout. To see the read timeout set, please call
     * getReadTimeout().
     *
     * @param timeout an {@code int} that specifies the timeout
     *                value to be used in milliseconds
     * @throws IllegalArgumentException if the timeout parameter is negative
     * @see #getReadTimeout()
     * @see InputStream#read()
     * @since 1.5
     */
    @Override
    public void setReadTimeout(final int timeout) {
        if (unsecureConnection != null) unsecureConnection.setReadTimeout(timeout);
        if (secureConnection != null) secureConnection.setConnectTimeout(timeout);
        super.setReadTimeout(timeout);
    }

    /**
     * Returns setting for read timeout. 0 return implies that the
     * option is disabled (i.e., timeout of infinity).
     *
     * @return an {@code int} that indicates the read timeout
     * value in milliseconds
     * @see #setReadTimeout(int)
     * @see InputStream#read()
     * @since 1.5
     */
    @Override
    public int getReadTimeout() {
        if (unsecureConnection != null) return unsecureConnection.getReadTimeout();
        if (secureConnection != null) return secureConnection.getReadTimeout();
        return super.getReadTimeout();
    }

    /**
     * Returns the value of this {@code URLConnection}'s {@code URL}
     * field.
     *
     * @return the value of this {@code URLConnection}'s {@code URL}
     * field.
     * @see URLConnection#url
     */
    @Override
    public URL getURL() {
        if (unsecureConnection != null) return unsecureConnection.getURL();
        if (secureConnection != null) return secureConnection.getURL();
        return super.getURL();
    }

    /**
     * Returns the value of the {@code content-length} header field.
     * <p>
     * <B>Note</B>: {@link #getContentLengthLong() getContentLengthLong()}
     * should be preferred over this method, since it returns a {@code long}
     * instead and is therefore more portable.</P>
     *
     * @return the content length of the resource that this connection's URL
     * references, {@code -1} if the content length is not known,
     * or if the content length is greater than Integer.MAX_VALUE.
     */
    @Override
    public int getContentLength() {
        if (unsecureConnection != null) return unsecureConnection.getContentLength();
        if (secureConnection != null) return secureConnection.getContentLength();
        return super.getContentLength();
    }

    /**
     * Returns the value of the {@code content-length} header field as a
     * long.
     *
     * @return the content length of the resource that this connection's URL
     * references, or {@code -1} if the content length is
     * not known.
     * @since 7.0
     */
    @Override
    public long getContentLengthLong() {
        if (unsecureConnection != null) return unsecureConnection.getContentLengthLong();
        if (secureConnection != null) return secureConnection.getContentLengthLong();
        return super.getContentLengthLong();
    }

    /**
     * Returns the value of the {@code content-type} header field.
     *
     * @return the content type of the resource that the URL references,
     * or {@code null} if not known.
     * @see URLConnection#getHeaderField(String)
     */
    @Override
    public String getContentType() {
        if (unsecureConnection != null) return unsecureConnection.getContentType();
        if (secureConnection != null) return secureConnection.getContentType();
        return super.getContentType();
    }

    /**
     * Returns the value of the {@code content-encoding} header field.
     *
     * @return the content encoding of the resource that the URL references,
     * or {@code null} if not known.
     * @see URLConnection#getHeaderField(String)
     */
    @Override
    public String getContentEncoding() {
        if (unsecureConnection != null) return unsecureConnection.getContentEncoding();
        if (secureConnection != null) return secureConnection.getContentEncoding();
        return super.getContentEncoding();
    }

    /**
     * Returns the value of the {@code expires} header field.
     *
     * @return the expiration date of the resource that this URL references,
     * or 0 if not known. The value is the number of milliseconds since
     * January 1, 1970 GMT.
     * @see URLConnection#getHeaderField(String)
     */
    @Override
    public long getExpiration() {
        if (unsecureConnection != null) return unsecureConnection.getExpiration();
        if (secureConnection != null) return secureConnection.getExpiration();
        return super.getExpiration();
    }

    /**
     * Returns the value of the {@code date} header field.
     *
     * @return the sending date of the resource that the URL references,
     * or {@code 0} if not known. The value returned is the
     * number of milliseconds since January 1, 1970 GMT.
     * @see URLConnection#getHeaderField(String)
     */
    @Override
    public long getDate() {
        if (unsecureConnection != null) return unsecureConnection.getDate();
        if (secureConnection != null) return secureConnection.getDate();
        return super.getDate();
    }

    /**
     * Returns the value of the {@code last-modified} header field.
     * The result is the number of milliseconds since January 1, 1970 GMT.
     *
     * @return the date the resource referenced by this
     * {@code URLConnection} was last modified, or 0 if not known.
     * @see URLConnection#getHeaderField(String)
     */
    @Override
    public long getLastModified() {
        if (unsecureConnection != null) return unsecureConnection.getLastModified();
        if (secureConnection != null) return secureConnection.getLastModified();
        return super.getLastModified();
    }

    /**
     * Returns the value of the named header field.
     * <p>
     * If called on a connection that sets the same header multiple times
     * with possibly different values, only the last value is returned.
     *
     * @param name the name of a header field.
     * @return the value of the named header field, or {@code null}
     * if there is no such field in the header.
     */
    @Override
    public String getHeaderField(final String name) {
        if (unsecureConnection != null) return unsecureConnection.getHeaderField(name);
        if (secureConnection != null) return secureConnection.getHeaderField(name);
        return super.getHeaderField(name);
    }

    /**
     * Returns an unmodifiable Map of the header fields.
     * The Map keys are Strings that represent the
     * response-header field names. Each Map value is an
     * unmodifiable List of Strings that represents
     * the corresponding field values.
     *
     * @return a Map of header fields
     * @since 1.4
     */
    @Override
    public Map<String, List<String>> getHeaderFields() {
        if (unsecureConnection != null) return unsecureConnection.getHeaderFields();
        if (secureConnection != null) return secureConnection.getHeaderFields();
        return super.getHeaderFields();
    }

    /**
     * Returns the value of the named field parsed as a number.
     * <p>
     * This form of {@code getHeaderField} exists because some
     * connection types (e.g., {@code http-ng}) have pre-parsed
     * headers. Classes for that connection type can override this method
     * and short-circuit the parsing.
     *
     * @param name    the name of the header field.
     * @param Default the default value.
     * @return the value of the named field, parsed as an integer. The
     * {@code Default} value is returned if the field is
     * missing or malformed.
     */
    @Override
    public int getHeaderFieldInt(final String name, final int Default) {
        if (unsecureConnection != null) return unsecureConnection.getHeaderFieldInt(name, Default);
        if (secureConnection != null) return secureConnection.getHeaderFieldInt(name, Default);
        return super.getHeaderFieldInt(name, Default);
    }

    /**
     * Returns the value of the named field parsed as a number.
     * <p>
     * This form of {@code getHeaderField} exists because some
     * connection types (e.g., {@code http-ng}) have pre-parsed
     * headers. Classes for that connection type can override this method
     * and short-circuit the parsing.
     *
     * @param name    the name of the header field.
     * @param Default the default value.
     * @return the value of the named field, parsed as a long. The
     * {@code Default} value is returned if the field is
     * missing or malformed.
     * @since 7.0
     */
    @Override
    public long getHeaderFieldLong(final String name, final long Default) {
        if (unsecureConnection != null) return unsecureConnection.getHeaderFieldLong(name, Default);
        if (secureConnection != null) return secureConnection.getHeaderFieldLong(name, Default);
        return super.getHeaderFieldLong(name, Default);
    }

    /**
     * Returns the value of the named field parsed as date.
     * The result is the number of milliseconds since January 1, 1970 GMT
     * represented by the named field.
     * <p>
     * This form of {@code getHeaderField} exists because some
     * connection types (e.g., {@code http-ng}) have pre-parsed
     * headers. Classes for that connection type can override this method
     * and short-circuit the parsing.
     *
     * @param name    the name of the header field.
     * @param Default a default value.
     * @return the value of the field, parsed as a date. The value of the
     * {@code Default} argument is returned if the field is
     * missing or malformed.
     */
    @Override
    public long getHeaderFieldDate(final String name, final long Default) {
        if (unsecureConnection != null) return unsecureConnection.getHeaderFieldDate(name, Default);
        if (secureConnection != null) return secureConnection.getHeaderFieldDate(name, Default);
        return super.getHeaderFieldDate(name, Default);
    }

    /**
     * Returns the key for the {@code n}<sup>th</sup> header field.
     * It returns {@code null} if there are fewer than {@code n+1} fields.
     *
     * @param n an index, where {@code n>=0}
     * @return the key for the {@code n}<sup>th</sup> header field,
     * or {@code null} if there are fewer than {@code n+1}
     * fields.
     */
    @Override
    public String getHeaderFieldKey(final int n) {
        if (unsecureConnection != null) return unsecureConnection.getHeaderFieldKey(n);
        if (secureConnection != null) return secureConnection.getHeaderFieldKey(n);
        return super.getHeaderFieldKey(n);
    }

    /**
     * Returns the value for the {@code n}<sup>th</sup> header field.
     * It returns {@code null} if there are fewer than
     * {@code n+1}fields.
     * <p>
     * This method can be used in conjunction with the
     * {@link #getHeaderFieldKey(int) getHeaderFieldKey} method to iterate through all
     * the headers in the message.
     *
     * @param n an index, where {@code n>=0}
     * @return the value of the {@code n}<sup>th</sup> header field
     * or {@code null} if there are fewer than {@code n+1} fields
     * @see URLConnection#getHeaderFieldKey(int)
     */
    @Override
    public String getHeaderField(final int n) {
        if (unsecureConnection != null) return unsecureConnection.getHeaderField(n);
        if (secureConnection != null) return secureConnection.getHeaderField(n);
        return super.getHeaderField(n);
    }

    /**
     * Retrieves the contents of this URL connection.
     * <p>
     * This method first determines the content type of the object by
     * calling the {@code getContentType} method. If this is
     * the first time that the application has seen that specific content
     * type, a content handler for that content type is created:
     * <ol>
     * <li>If the application has set up a content handler factory instance
     *     using the {@code setContentHandlerFactory} method, the
     *     {@code createContentHandler} method of that instance is called
     *     with the content type as an argument; the result is a content
     *     handler for that content type.
     * <li>If no content handler factory has yet been set up, or if the
     *     factory's {@code createContentHandler} method returns
     *     {@code null}, then the application loads the class named:
     *     <blockquote><pre>
     *         sun.net.www.content.&lt;<i>contentType</i>&gt;
     *     </pre></blockquote>
     *     where &lt;<i>contentType</i>&gt; is formed by taking the
     *     content-type string, replacing all slash characters with a
     *     {@code period} ('.'), and all other non-alphanumeric characters
     *     with the underscore character '{@code _}'. The alphanumeric
     *     characters are specifically the 26 uppercase ASCII letters
     *     '{@code A}' through '{@code Z}', the 26 lowercase ASCII
     *     letters '{@code a}' through '{@code z}', and the 10 ASCII
     *     digits '{@code 0}' through '{@code 9}'. If the specified
     *     class does not exist, or is not a subclass of
     *     {@code ContentHandler}, then an
     *     {@code UnknownServiceException} is thrown.
     * </ol>
     *
     * @return the object fetched. The {@code instanceof} operator
     * should be used to determine the specific kind of object
     * returned.
     * @throws IOException             if an I/O error occurs while
     *                                 getting the content.
     * @throws UnknownServiceException if the protocol does not support
     *                                 the content type.
     * @see ContentHandlerFactory#createContentHandler(String)
     * @see URLConnection#getContentType()
     * @see URLConnection#setContentHandlerFactory(ContentHandlerFactory)
     */
    @Override
    public Object getContent() throws IOException {
        if (unsecureConnection != null) return unsecureConnection.getContent();
        if (secureConnection != null) return secureConnection.getContent();
        return super.getContent();
    }

    /**
     * Retrieves the contents of this URL connection.
     *
     * @param classes the {@code Class} array
     *                indicating the requested types
     * @return the object fetched that is the first match of the type
     * specified in the classes array. null if none of
     * the requested types are supported.
     * The {@code instanceof} operator should be used to
     * determine the specific kind of object returned.
     * @throws IOException             if an I/O error occurs while
     *                                 getting the content.
     * @throws UnknownServiceException if the protocol does not support
     *                                 the content type.
     * @see URLConnection#getContent()
     * @see ContentHandlerFactory#createContentHandler(String)
     * @see URLConnection#getContent(Class[])
     * @see URLConnection#setContentHandlerFactory(ContentHandlerFactory)
     * @since 1.3
     */
    @Override
    public Object getContent(final Class[] classes) throws IOException {
        if (unsecureConnection != null) return unsecureConnection.getContent(classes);
        if (secureConnection != null) return secureConnection.getContent(classes);
        return super.getContent(classes);
    }

    /**
     * Returns a permission object representing the permission
     * necessary to make the connection represented by this
     * object. This method returns null if no permission is
     * required to make the connection. By default, this method
     * returns {@code java.security.AllPermission}. Subclasses
     * should override this method and return the permission
     * that best represents the permission required to make a
     * a connection to the URL. For example, a {@code URLConnection}
     * representing a {@code file:} URL would return a
     * {@code java.io.FilePermission} object.
     *
     * <p>The permission returned may dependent upon the state of the
     * connection. For example, the permission before connecting may be
     * different from that after connecting. For example, an HTTP
     * sever, say foo.com, may redirect the connection to a different
     * host, say bar.com. Before connecting the permission returned by
     * the connection will represent the permission needed to connect
     * to foo.com, while the permission returned after connecting will
     * be to bar.com.
     *
     * <p>Permissions are generally used for two purposes: to protect
     * caches of objects obtained through URLConnections, and to check
     * the right of a recipient to learn about a particular URL. In
     * the first case, the permission should be obtained
     * <em>after</em> the object has been obtained. For example, in an
     * HTTP connection, this will represent the permission to connect
     * to the host from which the data was ultimately fetched. In the
     * second case, the permission should be obtained and tested
     * <em>before</em> connecting.
     *
     * @return the permission object representing the permission
     * necessary to make the connection represented by this
     * URLConnection.
     * @throws IOException if the computation of the permission
     *                     requires network or file I/O and an exception occurs while
     *                     computing it.
     */
    @Override
    public Permission getPermission() throws IOException {
        if (unsecureConnection != null) return unsecureConnection.getPermission();
        if (secureConnection != null) return secureConnection.getPermission();
        return super.getPermission();
    }

    /**
     * Returns an input stream that reads from this open connection.
     * <p>
     * A SocketTimeoutException can be thrown when reading from the
     * returned input stream if the read timeout expires before data
     * is available for read.
     *
     * @return an input stream that reads from this open connection.
     * @throws IOException             if an I/O error occurs while
     *                                 creating the input stream.
     * @throws UnknownServiceException if the protocol does not support
     *                                 input.
     * @see #setReadTimeout(int)
     * @see #getReadTimeout()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (unsecureConnection != null) return unsecureConnection.getInputStream();
        if (secureConnection != null) return secureConnection.getInputStream();
        return super.getInputStream();
    }

    /**
     * Returns an output stream that writes to this connection.
     *
     * @return an output stream that writes to this connection.
     * @throws IOException             if an I/O error occurs while
     *                                 creating the output stream.
     * @throws UnknownServiceException if the protocol does not support
     *                                 output.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (unsecureConnection != null) return unsecureConnection.getOutputStream();
        if (secureConnection != null) return secureConnection.getOutputStream();
        return super.getOutputStream();
    }

    /**
     * Returns a {@code String} representation of this URL connection.
     *
     * @return a string representation of this {@code URLConnection}.
     */
    @Override
    public String toString() {
        if (unsecureConnection != null) return unsecureConnection.toString();
        if (secureConnection != null) return secureConnection.toString();
        return super.toString();
    }

    /**
     * Sets the value of the {@code doInput} field for this
     * {@code URLConnection} to the specified value.
     * <p>
     * A URL connection can be used for input and/or output.  Set the DoInput
     * flag to true if you intend to use the URL connection for input,
     * false if not.  The default is true.
     *
     * @param doinput the new value.
     * @throws IllegalStateException if already connected
     * @see URLConnection#doInput
     * @see #getDoInput()
     */
    @Override
    public void setDoInput(final boolean doinput) {
        if (unsecureConnection != null) unsecureConnection.setDoInput(doInput);
        if (secureConnection != null) secureConnection.setDoInput(doInput);
        super.setDoInput(doInput);
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code doInput} flag.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code doInput} flag.
     * @see #setDoInput(boolean)
     */
    @Override
    public boolean getDoInput() {
        if (unsecureConnection != null) return unsecureConnection.getDoInput();
        if (secureConnection != null) return secureConnection.getDoInput();
        return super.getDoInput();
    }

    /**
     * Sets the value of the {@code doOutput} field for this
     * {@code URLConnection} to the specified value.
     * <p>
     * A URL connection can be used for input and/or output.  Set the DoOutput
     * flag to true if you intend to use the URL connection for output,
     * false if not.  The default is false.
     *
     * @param dooutput the new value.
     * @throws IllegalStateException if already connected
     * @see #getDoOutput()
     */
    @Override
    public void setDoOutput(final boolean dooutput) {
        if (unsecureConnection != null) unsecureConnection.setDoOutput(dooutput);
        if (secureConnection != null) secureConnection.setDoOutput(dooutput);
        super.setDoOutput(dooutput);
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code doOutput} flag.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code doOutput} flag.
     * @see #setDoOutput(boolean)
     */
    @Override
    public boolean getDoOutput() {
        if (unsecureConnection != null) return unsecureConnection.getDoOutput();
        if (secureConnection != null) return secureConnection.getDoOutput();
        return super.getDoOutput();
    }

    /**
     * Set the value of the {@code allowUserInteraction} field of
     * this {@code URLConnection}.
     *
     * @param allowuserinteraction the new value.
     * @throws IllegalStateException if already connected
     * @see #getAllowUserInteraction()
     */
    @Override
    public void setAllowUserInteraction(final boolean allowuserinteraction) {
        if (unsecureConnection != null) unsecureConnection.setAllowUserInteraction(allowuserinteraction);
        if (secureConnection != null) secureConnection.setAllowUserInteraction(allowuserinteraction);
        super.setAllowUserInteraction(allowuserinteraction);
    }

    /**
     * Returns the value of the {@code allowUserInteraction} field for
     * this object.
     *
     * @return the value of the {@code allowUserInteraction} field for
     * this object.
     * @see #setAllowUserInteraction(boolean)
     */
    @Override
    public boolean getAllowUserInteraction() {
        if (unsecureConnection != null) return unsecureConnection.getAllowUserInteraction();
        if (secureConnection != null) return secureConnection.getAllowUserInteraction();
        return super.getAllowUserInteraction();
    }

    /**
     * Sets the value of the {@code useCaches} field of this
     * {@code URLConnection} to the specified value.
     * <p>
     * Some protocols do caching of documents.  Occasionally, it is important
     * to be able to "tunnel through" and ignore the caches (e.g., the
     * "reload" button in a browser).  If the UseCaches flag on a connection
     * is true, the connection is allowed to use whatever caches it can.
     * If false, caches are to be ignored.
     * The default value comes from DefaultUseCaches, which defaults to
     * true.
     *
     * @param usecaches a {@code boolean} indicating whether
     *                  or not to allow caching
     * @throws IllegalStateException if already connected
     * @see #getUseCaches()
     */
    @Override
    public void setUseCaches(final boolean usecaches) {
        if (unsecureConnection != null) unsecureConnection.setUseCaches(useCaches);
        if (secureConnection != null) secureConnection.setUseCaches(useCaches);
        super.setUseCaches(useCaches);
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code useCaches} field.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code useCaches} field.
     * @see #setUseCaches(boolean)
     */
    @Override
    public boolean getUseCaches() {
        if (unsecureConnection != null) return unsecureConnection.getUseCaches();
        if (secureConnection != null) return secureConnection.getUseCaches();
        return super.getUseCaches();
    }

    /**
     * Sets the value of the {@code ifModifiedSince} field of
     * this {@code URLConnection} to the specified value.
     *
     * @param ifmodifiedsince the new value.
     * @throws IllegalStateException if already connected
     * @see #getIfModifiedSince()
     */
    @Override
    public void setIfModifiedSince(final long ifmodifiedsince) {
        if (unsecureConnection != null) unsecureConnection.setIfModifiedSince(ifmodifiedsince);
        if (secureConnection != null) secureConnection.setIfModifiedSince(ifmodifiedsince);
        super.setIfModifiedSince(ifmodifiedsince);
    }

    /**
     * Returns the value of this object's {@code ifModifiedSince} field.
     *
     * @return the value of this object's {@code ifModifiedSince} field.
     * @see #setIfModifiedSince(long)
     */
    @Override
    public long getIfModifiedSince() {
        if (unsecureConnection != null) return unsecureConnection.getIfModifiedSince();
        if (secureConnection != null) return secureConnection.getIfModifiedSince();
        return super.getIfModifiedSince();
    }

    /**
     * Returns the default value of a {@code URLConnection}'s
     * {@code useCaches} flag.
     * <p>
     * Ths default is "sticky", being a part of the static state of all
     * URLConnections.  This flag applies to the next, and all following
     * URLConnections that are created.
     *
     * @return the default value of a {@code URLConnection}'s
     * {@code useCaches} flag.
     * @see #setDefaultUseCaches(boolean)
     */
    @Override
    public boolean getDefaultUseCaches() {
        if (unsecureConnection != null) return unsecureConnection.getDefaultUseCaches();
        if (secureConnection != null) return secureConnection.getDefaultUseCaches();
        return super.getDefaultUseCaches();
    }

    /**
     * Sets the default value of the {@code useCaches} field to the
     * specified value.
     *
     * @param defaultusecaches the new value.
     * @see #getDefaultUseCaches()
     */
    @Override
    public void setDefaultUseCaches(final boolean defaultusecaches) {
        if (unsecureConnection != null) unsecureConnection.setDefaultUseCaches(defaultusecaches);
        if (secureConnection != null) secureConnection.setDefaultUseCaches(defaultusecaches);
        super.setDefaultUseCaches(defaultusecaches);
    }

    /**
     * Sets the general request property. If a property with the key already
     * exists, overwrite its value with the new value.
     *
     * <p> NOTE: HTTP requires all request properties which can
     * legally have multiple instances with the same key
     * to use a comma-separated list syntax which enables multiple
     * properties to be appended into a single property.
     *
     * @param key   the keyword by which the request is known
     *              (e.g., "{@code Accept}").
     * @param value the value associated with it.
     * @throws IllegalStateException if already connected
     * @throws NullPointerException  if key is <CODE>null</CODE>
     * @see #getRequestProperty(String)
     */
    @Override
    public void setRequestProperty(final String key, final String value) {
        if (unsecureConnection != null) unsecureConnection.setRequestProperty(key, value);
        if (secureConnection != null) secureConnection.setRequestProperty(key, value);
        super.setRequestProperty(key, value);
    }

    /**
     * Set the request user agent
     *
     * @param agent the request user agent
     */
    public void setUserAgent(final String agent) {
        setRequestProperty("User-Agent", agent);
    }

    /**
     * Set the request content type
     *
     * @param type the request content type
     */
    public void setContentType(final RequestData.ContentType type) {
        String raw;
        switch (type) {
            case JSON:
            case PRETTY_JSON:
                raw = "application/json";
                break;
            case FORM:
                raw = "multipart/form-data";
                break;
            case ENCODED:
            default:
                raw = "application/x-www-form-urlencoded";
                break;
        }

        setRequestProperty("Content-Type", raw);
    }

    /**
     * Adds a general request property specified by a
     * key-value pair.  This method will not overwrite
     * existing values associated with the same key.
     *
     * @param key   the keyword by which the request is known
     *              (e.g., "{@code Accept}").
     * @param value the value associated with it.
     * @throws IllegalStateException if already connected
     * @throws NullPointerException  if key is null
     * @see #getRequestProperties()
     * @since 1.4
     */
    @Override
    public void addRequestProperty(final String key, final String value) {
        if (unsecureConnection != null) unsecureConnection.addRequestProperty(key, value);
        if (secureConnection != null) secureConnection.addRequestProperty(key, value);
        super.addRequestProperty(key, value);
    }

    /**
     * Returns the value of the named general request property for this
     * connection.
     *
     * @param key the keyword by which the request is known (e.g., "Accept").
     * @return the value of the named general request property for this
     * connection. If key is null, then null is returned.
     * @throws IllegalStateException if already connected
     * @see #setRequestProperty(String, String)
     */
    @Override
    public String getRequestProperty(final String key) {
        if (unsecureConnection != null) return unsecureConnection.getRequestProperty(key);
        if (secureConnection != null) return secureConnection.getRequestProperty(key);
        return super.getRequestProperty(key);
    }

    /**
     * Returns an unmodifiable Map of general request
     * properties for this connection. The Map keys
     * are Strings that represent the request-header
     * field names. Each Map value is a unmodifiable List
     * of Strings that represents the corresponding
     * field values.
     *
     * @return a Map of the general request properties for this connection.
     * @throws IllegalStateException if already connected
     * @since 1.4
     */
    @Override
    public Map<String, List<String>> getRequestProperties() {
        if (unsecureConnection != null) return unsecureConnection.getRequestProperties();
        if (secureConnection != null) return secureConnection.getRequestProperties();
        return super.getRequestProperties();
    }

    /**
     * Get if the connection is secure
     *
     * @return if the connection is secure
     */
    public boolean isSecure() {
        return secureConnection != null;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     */
    @Override
    public void close() {
        disconnect();
    }

    /**
     * Create a wrapped connection from the URI
     *
     * @param uri the URI
     * @return the connection wrapper
     * @throws MalformedURLException if the uri is not a valid URL
     * @throws IOException if the connection fails to open
     */
    public static URLConnectionWrapper fromURI(final URI uri) throws MalformedURLException, IOException {
        return fromURL(uri.toURL());
    }

    /**
     * Create a wrapped connection from the URL
     *
     * @param url the connection
     * @return the connection wrapper
     * @throws IOException if the connection fails to open
     */
    public static URLConnectionWrapper fromURL(final URL url) throws IOException {
        return new URLConnectionWrapper(url);
    }

    /**
     * Create a wrapped connection from the URL
     * connection
     *
     * @param connection the connection
     * @return the connection wrapper
     */
    public static URLConnectionWrapper fromConnection(final URLConnection connection) {
        return new URLConnectionWrapper(connection);
    }
}
