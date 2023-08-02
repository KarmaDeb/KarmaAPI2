package es.karmadev.api.security;

import es.karmadev.api.core.KarmaAPI;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.logger.log.console.LogLevel;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unused")
public class LockedProperties extends Properties {

    private static boolean securityWarning = false;

    private final Map<String, String> properties = new ConcurrentHashMap<>();
    private final Map<String, Path> protectedProperties = new ConcurrentHashMap<>();

    /**
     * Creates an empty property list with no default values.
     */
    public LockedProperties() {
        super();
    }

    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param defaults the defaults.
     */
    public LockedProperties(final Properties defaults) {
        super(defaults);
    }

    /**
     * Calls the <tt>Hashtable</tt> method {@code put}. Provided for
     * parallelism with the <tt>getProperty</tt> method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the <tt>Hashtable</tt> call to {@code put}.
     *
     * @param key   the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @return the previous value of the specified key in this property
     * list, or {@code null} if it did not have one.
     * @see #getProperty
     * @since 1.2
     */
    @Override
    public synchronized Object setProperty(final String key, final String value) {
        if (hasAccess(key)) {
            return properties.put(key, value);
        } else {
            return properties.getOrDefault(key, value);
        }
    }

    /**
     * Reads a property list (key and element pairs) from the input
     * character stream in a simple line-oriented format.
     * <p>
     * Properties are processed in terms of lines. There are two
     * kinds of line, <i>natural lines</i> and <i>logical lines</i>.
     * A natural line is defined as a line of
     * characters that is terminated either by a set of line terminator
     * characters ({@code \n} or {@code \r} or {@code \r\n})
     * or by the end of the stream. A natural line may be either a blank line,
     * a comment line, or hold all or some of a key-element pair. A logical
     * line holds all the data of a key-element pair, which may be spread
     * out across several adjacent natural lines by escaping
     * the line terminator sequence with a backslash character
     * {@code \}.  Note that a comment line cannot be extended
     * in this manner; every natural line that is a comment must have
     * its own comment indicator, as described below. Lines are read from
     * input until the end of the stream is reached.
     *
     * <p>
     * A natural line that contains only white space characters is
     * considered blank and is ignored.  A comment line has an ASCII
     * {@code '#'} or {@code '!'} as its first non-white
     * space character; comment lines are also ignored and do not
     * encode key-element information.  In addition to line
     * terminators, this format considers the characters space
     * ({@code ' '}, {@code '\u005Cu0020'}), tab
     * ({@code '\t'}, {@code '\u005Cu0009'}), and form feed
     * ({@code '\f'}, {@code '\u005Cu000C'}) to be white
     * space.
     *
     * <p>
     * If a logical line is spread across several natural lines, the
     * backslash escaping the line terminator sequence, the line
     * terminator sequence, and any white space at the start of the
     * following line have no affect on the key or element values.
     * The remainder of the discussion of key and element parsing
     * (when loading) will assume all the characters constituting
     * the key and element appear on a single natural line after
     * line continuation characters have been removed.  Note that
     * it is <i>not</i> sufficient to only examine the character
     * preceding a line terminator sequence to decide if the line
     * terminator is escaped; there must be an odd number of
     * contiguous backslashes for the line terminator to be escaped.
     * Since the input is processed from left to right, a
     * non-zero even number of 2<i>n</i> contiguous backslashes
     * before a line terminator (or elsewhere) encodes <i>n</i>
     * backslashes after escape processing.
     *
     * <p>
     * The key contains all of the characters in the line starting
     * with the first non-white space character and up to, but not
     * including, the first unescaped {@code '='},
     * {@code ':'}, or white space character other than a line
     * terminator. All of these key termination characters may be
     * included in the key by escaping them with a preceding backslash
     * character; for example,<p>
     * <p>
     * {@code \:\=}<p>
     * <p>
     * would be the two-character key {@code ":="}.  Line
     * terminator characters can be included using {@code \r} and
     * {@code \n} escape sequences.  Any white space after the
     * key is skipped; if the first non-white space character after
     * the key is {@code '='} or {@code ':'}, then it is
     * ignored and any white space characters after it are also
     * skipped.  All remaining characters on the line become part of
     * the associated element string; if there are no remaining
     * characters, the element is the empty string
     * {@code ""}.  Once the raw character sequences
     * constituting the key and element are identified, escape
     * processing is performed as described above.
     *
     * <p>
     * As an example, each of the following three lines specifies the key
     * {@code "Truth"} and the associated element value
     * {@code "Beauty"}:
     * <pre>
     * Truth = Beauty
     *  Truth:Beauty
     * Truth                    :Beauty
     * </pre>
     * As another example, the following three lines specify a single
     * property:
     * <pre>
     * fruits                           apple, banana, pear, \
     *                                  cantaloupe, watermelon, \
     *                                  kiwi, mango
     * </pre>
     * The key is {@code "fruits"} and the associated element is:
     * <pre>"apple, banana, pear, cantaloupe, watermelon, kiwi, mango"</pre>
     * Note that a space appears before each {@code \} so that a space
     * will appear after each comma in the final result; the {@code \},
     * line terminator, and leading white space on the continuation line are
     * merely discarded and are <i>not</i> replaced by one or more other
     * characters.
     * <p>
     * As a third example, the line:
     * <pre>cheeses
     * </pre>
     * specifies that the key is {@code "cheeses"} and the associated
     * element is the empty string {@code ""}.
     * <p>
     * <a name="unicodeescapes"></a>
     * Characters in keys and elements can be represented in escape
     * sequences similar to those used for character and string literals
     * (see sections 3.3 and 3.10.6 of
     * <cite>The Java&trade; Language Specification</cite>).
     * <p>
     * The differences from the character escape sequences and Unicode
     * escapes used for characters and strings are:
     *
     * <ul>
     * <li> Octal escapes are not recognized.
     *
     * <li> The character sequence {@code \b} does <i>not</i>
     * represent a backspace character.
     *
     * <li> The method does not treat a backslash character,
     * {@code \}, before a non-valid escape character as an
     * error; the backslash is silently dropped.  For example, in a
     * Java string the sequence {@code "\z"} would cause a
     * compile time error.  In contrast, this method silently drops
     * the backslash.  Therefore, this method treats the two character
     * sequence {@code "\b"} as equivalent to the single
     * character {@code 'b'}.
     *
     * <li> Escapes are not necessary for single and double quotes;
     * however, by the rule above, single and double quote characters
     * preceded by a backslash still yield single and double quote
     * characters, respectively.
     *
     * <li> Only a single 'u' character is allowed in a Unicode escape
     * sequence.
     *
     * </ul>
     * <p>
     * The specified stream remains open after this method returns.
     *
     * @param reader the input character stream.
     * @throws IOException              if an error occurred when reading from the
     *                                  input stream.
     * @throws IllegalArgumentException if a malformed Unicode escape
     *                                  appears in the input.
     * @since 1.6
     */
    @Override
    public synchronized void load(final Reader reader) throws IOException {
        Properties vProperties = new Properties();
        vProperties.load(reader);

        properties.clear();
        protectedProperties.clear();
        vProperties.forEach((key, value) -> {
            if (key instanceof String && value != null) {
                String k = (String) key;
                setProtected(k, String.valueOf(value));
            }
        });
    }

    /**
     * Reads a property list (key and element pairs) from the input
     * byte stream. The input stream is in a simple line-oriented
     * format as specified in
     * {@link #load(Reader) load(Reader)} and is assumed to use
     * the ISO 8859-1 character encoding; that is each byte is one Latin1
     * character. Characters not in Latin1, and certain special characters,
     * are represented in keys and elements using Unicode escapes as defined in
     * section 3.3 of
     * <cite>The Java&trade; Language Specification</cite>.
     * <p>
     * The specified stream remains open after this method returns.
     *
     * @param inStream the input stream.
     * @throws IOException              if an error occurred when reading from the
     *                                  input stream.
     * @throws IllegalArgumentException if the input stream contains a
     *                                  malformed Unicode escape sequence.
     * @since 1.2
     */
    @Override
    public synchronized void load(final InputStream inStream) throws IOException {
        Properties vProperties = new Properties();
        vProperties.load(inStream);

        properties.clear();
        protectedProperties.clear();
        vProperties.forEach((key, value) -> {
            if (key instanceof String && value != null) {
                String k = (String) key;
                setProtected(k, String.valueOf(value));
            }
        });
    }

    /**
     * Calls the {@code store(OutputStream out, String comments)} method
     * and suppresses IOExceptions that were thrown.
     *
     * @param out      an output stream.
     * @param comments a description of the property list.
     * {@code storeToXML(OutputStream os, String comment)} method.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void save(final OutputStream out, final String comments) {
        //Cannot save locked properties
    }

    /**
     * Writes this property list (key and element pairs) in this
     * {@code Properties} table to the output character stream in a
     * format suitable for using the {@link #load(Reader) load(Reader)}
     * method.
     * <p>
     * Properties from the defaults table of this {@code Properties}
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * If the comments argument is not null, then an ASCII {@code #}
     * character, the comments string, and a line separator are first written
     * to the output stream. Thus, the {@code comments} can serve as an
     * identifying comment. Any one of a line feed ('\n'), a carriage
     * return ('\r'), or a carriage return followed immediately by a line feed
     * in comments is replaced by a line separator generated by the {@code Writer}
     * and if the next character in comments is not character {@code #} or
     * character {@code !} then an ASCII {@code #} is written out
     * after that line separator.
     * <p>
     * Next, a comment line is always written, consisting of an ASCII
     * {@code #} character, the current date and time (as if produced
     * by the {@code toString} method of {@code Date} for the
     * current time), and a line separator as generated by the {@code Writer}.
     * <p>
     * Then every entry in this {@code Properties} table is
     * written out, one per line. For each entry the key string is
     * written, then an ASCII {@code =}, then the associated
     * element string. For the key, all space characters are
     * written with a preceding {@code \} character.  For the
     * element, leading space characters, but not embedded or trailing
     * space characters, are written with a preceding {@code \}
     * character. The key and element characters {@code #},
     * {@code !}, {@code =}, and {@code :} are written
     * with a preceding backslash to ensure that they are properly loaded.
     * <p>
     * After the entries have been written, the output stream is flushed.
     * The output stream remains open after this method returns.
     * <p>
     *
     * @param writer   an output character stream writer.
     * @param comments a description of the property list.
     * @since 1.6
     */
    @Override
    public void store(final Writer writer, final String comments) {
        //Cannot save locked properties
    }

    /**
     * Writes this property list (key and element pairs) in this
     * {@code Properties} table to the output stream in a format suitable
     * for loading into a {@code Properties} table using the
     * {@link #load(InputStream) load(InputStream)} method.
     * <p>
     * Properties from the defaults table of this {@code Properties}
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * This method outputs the comments, properties keys and values in
     * the same format as specified in
     * {@link #store(Writer, String) store(Writer)},
     * with the following differences:
     * <ul>
     * <li>The stream is written using the ISO 8859-1 character encoding.
     *
     * <li>Characters not in Latin-1 in the comments are written as
     * {@code \u005Cu}<i>xxxx</i> for their appropriate unicode
     * hexadecimal value <i>xxxx</i>.
     *
     * <li>Characters less than {@code \u005Cu0020} and characters greater
     * than {@code \u005Cu007E} in property keys or values are written
     * as {@code \u005Cu}<i>xxxx</i> for the appropriate hexadecimal
     * value <i>xxxx</i>.
     * </ul>
     * <p>
     * After the entries have been written, the output stream is flushed.
     * The output stream remains open after this method returns.
     * <p>
     *
     * @param out      an output stream.
     * @param comments a description of the property list.
     * @since 1.2
     */
    @Override
    public void store(OutputStream out, @Nullable String comments) {
        //Cannot save locked properties
    }

    /**
     * Loads all of the properties represented by the XML document on the
     * specified input stream into this properties table.
     *
     * <p>The XML document must have the following DOCTYPE declaration:
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     * Furthermore, the document must satisfy the properties DTD described
     * above.
     *
     * <p> An implementation is required to read XML documents that use the
     * "{@code UTF-8}" or "{@code UTF-16}" encoding. An implementation may
     * support additional encodings.
     *
     * <p>The specified stream is closed after this method returns.
     *
     * @param in the input stream from which to read the XML document.
     * @throws IOException                      if reading from the specified input stream
     *                                          results in an <tt>IOException</tt>.
     * @throws UnsupportedEncodingException     if the document's encoding
     *                                          declaration can be read and it specifies an encoding that is not
     *                                          supported
     * @throws InvalidPropertiesFormatException Data on input stream does not
     *                                          constitute a valid XML document with the mandated document type.
     * @throws NullPointerException             if {@code in} is null.
     * @see #storeToXML(OutputStream, String, String)
     * @see <a href="http://www.w3.org/TR/REC-xml/#charencoding">Character
     * Encoding in Entities</a>
     * @since 1.5
     */
    @Override
    public synchronized void loadFromXML(final InputStream in) throws IOException, InvalidPropertiesFormatException {
        Properties vProperties = new Properties();
        vProperties.loadFromXML(in);

        properties.clear();
        protectedProperties.clear();
        vProperties.forEach((key, value) -> {
            if (key instanceof String && value != null) {
                String k = (String) key;
                setProtected(k, String.valueOf(value));
            }
        });
    }

    /**
     * Emits an XML document representing all of the properties contained
     * in this table.
     *
     * <p> An invocation of this method of the form <tt>props.storeToXML(os,
     * comment)</tt> behaves in exactly the same way as the invocation
     * <tt>props.storeToXML(os, comment, "UTF-8");</tt>.
     *
     * @param os      the output stream on which to emit the XML document.
     * @param comment a description of the property list, or {@code null}
     *                if no comment is desired.
     * @see #loadFromXML(InputStream)
     * @since 1.5
     */
    @Override
    public void storeToXML(final OutputStream os, final String comment) {
        //Cannot save locked properties
    }

    /**
     * Emits an XML document representing all of the properties contained
     * in this table, using the specified encoding.
     *
     * <p>The XML document will have the following DOCTYPE declaration:
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     *
     * <p>If the specified comment is {@code null} then no comment
     * will be stored in the document.
     *
     * <p> An implementation is required to support writing of XML documents
     * that use the "{@code UTF-8}" or "{@code UTF-16}" encoding. An
     * implementation may support additional encodings.
     *
     * <p>The specified stream remains open after this method returns.
     *
     * @param os       the output stream on which to emit the XML document.
     * @param comment  a description of the property list, or {@code null}
     *                 if no comment is desired.
     * @param encoding the name of a supported
     *                 <a href="../lang/package-summary.html#charenc">
     *                 character encoding</a>
     * @see #loadFromXML(InputStream)
     * @see <a href="http://www.w3.org/TR/REC-xml/#charencoding">Character
     * Encoding in Entities</a>
     * @since 1.5
     */
    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) {
        //Cannot save locked properties
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * {@code null} if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     * @see #setProperty
     * @see #defaults
     */
    @Override
    public String getProperty(final String key) {
        return properties.getOrDefault(key, null);
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns the
     * default value argument if the property is not found.
     *
     * @param key          the hashtable key.
     * @param defaultValue a default value.
     * @return the value in this property list with the specified key value.
     * @see #setProperty
     * @see #defaults
     */
    @Override
    public String getProperty(final String key, final String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * Returns an enumeration of all the keys in this property list,
     * including distinct keys in the default property list if a key
     * of the same name has not already been found from the main
     * properties list.
     *
     * @return an enumeration of all the keys in this property list, including
     * the keys in the default property list.
     * @throws ClassCastException if any key in this property list
     *                            is not a string.
     * @see Enumeration
     * @see Properties#defaults
     * @see #stringPropertyNames
     */
    @Override
    public Enumeration<?> propertyNames() {
        Hashtable<String, Object> h = new Hashtable<>(properties);
        return h.keys();
    }

    /**
     * Returns a set of keys in this property list where
     * the key and its corresponding value are strings,
     * including distinct keys in the default property list if a key
     * of the same name has not already been found from the main
     * properties list.  Properties whose key or value is not
     * of type <tt>String</tt> are omitted.
     * <p>
     * The returned set is not backed by the <tt>Properties</tt> object.
     * Changes to this <tt>Properties</tt> are not reflected in the set,
     * or vice versa.
     *
     * @return a set of keys in this property list where
     * the key and its corresponding value are strings,
     * including the keys in the default property list.
     * @see Properties#defaults
     * @since 1.6
     */
    @Override
    public Set<String> stringPropertyNames() {
        return properties.keySet();
    }

    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param out an output stream.
     * @throws ClassCastException if any key in this property list
     *                            is not a string.
     */
    @Override
    public void list(final PrintStream out) {
        out.println("-- listing properties --");
        Hashtable<String,Object> h = new Hashtable<>(properties);
        for (Enumeration<String> e = h.keys() ; e.hasMoreElements() ;) {
            String key = e.nextElement();
            String val = (String)h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param out an output stream.
     * @throws ClassCastException if any key in this property list
     *                            is not a string.
     * @since JDK1.1
     */
    @Override
    public void list(final PrintWriter out) {
        out.println("-- listing properties --");
        Hashtable<String,Object> h = new Hashtable<>(properties);
        for (Enumeration<String> e = h.keys() ; e.hasMoreElements() ;) {
            String key = e.nextElement();
            String val = (String)h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return the number of keys in this hashtable.
     */
    @Override
    public synchronized int size() {
        return properties.size();
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return <code>true</code> if this hashtable maps no keys to values;
     * <code>false</code> otherwise.
     */
    @Override
    public synchronized boolean isEmpty() {
        return properties.isEmpty();
    }

    /**
     * Returns an enumeration of the keys in this hashtable.
     *
     * @return an enumeration of the keys in this hashtable.
     * @see Enumeration
     * @see #elements()
     * @see #keySet()
     * @see Map
     */
    @Override
    public synchronized Enumeration<Object> keys() {
        return new Hashtable<Object, Object>(properties).keys();
    }

    /**
     * Returns an enumeration of the values in this hashtable.
     * Use the Enumeration methods on the returned object to fetch the elements
     * sequentially.
     *
     * @return an enumeration of the values in this hashtable.
     * @see Enumeration
     * @see #keys()
     * @see #values()
     * @see Map
     */
    @Override
    public synchronized Enumeration<Object> elements() {
        Hashtable<Object, Object> table = new Hashtable<>();
        properties.forEach((key, value) -> {
            table.put(value, key);
        });

        return table.keys();
    }

    /**
     * Tests if some key maps into the specified value in this hashtable.
     * This operation is more expensive than the {@link #containsKey
     * containsKey} method.
     *
     * <p>Note that this method is identical in functionality to
     * {@link #containsValue containsValue}, (which is part of the
     * {@link Map} interface in the collections framework).
     *
     * @param value a value to search for
     * @return <code>true</code> if and only if some key maps to the
     * <code>value</code> argument in this hashtable as
     * determined by the <tt>equals</tt> method;
     * <code>false</code> otherwise.
     * @throws NullPointerException if the value is <code>null</code>
     */
    @Override
    public synchronized boolean contains(final Object value) {
        if (value == null) return false;
        return properties.containsKey(String.valueOf(value)) || properties.containsValue(String.valueOf(value));
    }

    /**
     * Returns true if this hashtable maps one or more keys to this value.
     *
     * <p>Note that this method is identical in functionality to {@link
     * #contains contains} (which predates the {@link Map} interface).
     *
     * @param value value whose presence in this hashtable is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     * specified value
     * @throws NullPointerException if the value is <code>null</code>
     * @since 1.2
     */
    @Override
    public boolean containsValue(final Object value) {
        if (value == null) return false;
        return properties.containsValue(String.valueOf(value));
    }

    /**
     * Tests if the specified object is a key in this hashtable.
     *
     * @param key possible key
     * @return <code>true</code> if and only if the specified object
     * is a key in this hashtable, as determined by the
     * <tt>equals</tt> method; <code>false</code> otherwise.
     * @throws NullPointerException if the key is <code>null</code>
     * @see #contains(Object)
     */
    @Override
    public synchronized boolean containsKey(final Object key) {
        if (key == null) return false;
        return properties.containsKey(String.valueOf(key));
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key.equals(k))},
     * then this method returns {@code v}; otherwise it returns
     * {@code null}.  (There can be at most one such mapping.)
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     * {@code null} if this map contains no mapping for the key
     * @throws NullPointerException if the specified key is null
     * @see #put(Object, Object)
     */
    @Override
    public synchronized Object get(final Object key) {
        if (key == null) return null;
        return properties.getOrDefault(String.valueOf(key), null);
    }

    /**
     * Increases the capacity of and internally reorganizes this
     * hashtable, in order to accommodate and access its entries more
     * efficiently.  This method is called automatically when the
     * number of keys in the hashtable exceeds this hashtable's capacity
     * and load factor.
     */
    @Override
    protected void rehash() {
        //We are using concurrent hashmap
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * <p>
     * The value can be retrieved by calling the <code>get</code> method
     * with a key that is equal to the original key.
     *
     * @param key   the hashtable key
     * @param value the value
     * @return the previous value of the specified key in this hashtable,
     * or <code>null</code> if it did not have one
     * @throws NullPointerException if the key or value is
     *                              <code>null</code>
     * @see Object#equals(Object)
     * @see #get(Object)
     */
    @Override
    public synchronized Object put(final Object key, final Object value) {
        if (key == null) return null;
        String k = String.valueOf(key);

        Object old = null;
        if (value == null) {
            if (hasAccess(k)) {
                old = properties.remove(k);
                protectedProperties.remove(k);
            }
        }

        return old;
    }

    /**
     * Removes the key (and its corresponding value) from this
     * hashtable. This method does nothing if the key is not in the hashtable.
     *
     * @param key the key that needs to be removed
     * @return the value to which the key had been mapped in this hashtable,
     * or <code>null</code> if the key did not have a mapping
     * @throws NullPointerException if the key is <code>null</code>
     */
    @Override
    public synchronized Object remove(final Object key) {
        if (key == null) return null;
        String k = String.valueOf(key);

        Object old = null;
        if (hasAccess(k)) {
            old = properties.remove(k);
            protectedProperties.remove(k);
        }

        return old;
    }

    /**
     * Copies all of the mappings from the specified map to this hashtable.
     * These mappings will replace any mappings that this hashtable had for any
     * of the keys currently in the specified map.
     *
     * @param t mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     * @since 1.2
     */
    @Override
    public synchronized void putAll(final Map<?, ?> t) {
        properties.clear();
        protectedProperties.clear();

        t.forEach((key, value) -> {
            if (key instanceof String && value != null) {
                String k = (String) key;
                String v = String.valueOf(value);

                setProtected(k, v);
            }
        });
    }

    /**
     * Clears this hashtable so that it contains no keys.
     */
    @Override
    public synchronized void clear() {
        properties.clear();
        protectedProperties.clear();
    }

    /**
     * Creates a shallow copy of this hashtable. All the structure of the
     * hashtable itself is copied, but the keys and values are not cloned.
     * This is a relatively expensive operation.
     *
     * @return a clone of the hashtable
     */
    @Override
    public synchronized LockedProperties clone() {
        LockedProperties clone = (LockedProperties) super.clone();
        clone.properties.putAll(properties);
        clone.protectedProperties.putAll(protectedProperties);

        return clone;
    }

    /**
     * Returns a string representation of this <tt>Hashtable</tt> object
     * in the form of a set of entries, enclosed in braces and separated
     * by the ASCII characters "<tt>,&nbsp;</tt>" (comma and space). Each
     * entry is rendered as the key, an equals sign <tt>=</tt>, and the
     * associated element, where the <tt>toString</tt> method is used to
     * convert the key and element to strings.
     *
     * @return a string representation of this hashtable
     */
    @Override
    public synchronized String toString() {
        return properties.toString();
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @since 1.2
     */
    @Override
    public Set<Object> keySet() {
        return new HashSet<>(properties.keySet());
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @since 1.2
     */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Set<Map.Entry<Object, Object>> entries = new HashSet<>();
        properties.forEach((key, value) -> entries.add(new AbstractMap.SimpleEntry<>(key, value)));
        return entries;
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @since 1.2
     */
    @Override
    public Collection<Object> values() {
        return new ArrayList<>(properties.values());
    }

    /**
     * Compares the specified Object with this Map for equality,
     * as per the definition in the Map interface.
     *
     * @param o object to be compared for equality with this hashtable
     * @return true if the specified Object is equal to this Map
     * @see Map#equals(Object)
     * @since 1.2
     */
    @Override
    public synchronized boolean equals(final Object o) {
        if (!(o instanceof LockedProperties)) return false;
        LockedProperties instance = (LockedProperties) o;

        return properties.equals(instance.properties) && protectedProperties.equals(instance.protectedProperties);
    }

    /**
     * Returns the hash code value for this Map as per the definition in the
     * Map interface.
     *
     * @see Map#hashCode()
     * @since 1.2
     */
    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }

    @Override
    public synchronized Object getOrDefault(final Object key, final Object defaultValue) {
        if (key == null) return null;
        String k = String.valueOf(key);
        String c = properties.getOrDefault(k, null);

        if (c == null) return defaultValue;
        return c;
    }

    @Override
    public synchronized void forEach(final BiConsumer<? super Object, ? super Object> action) {
        properties.forEach(action);
    }

    @Override
    public synchronized void replaceAll(final BiFunction<? super Object, ? super Object, ?> function) {
        return; //Cannot perform in locked properties
    }

    @Override
    public synchronized Object putIfAbsent(final Object key, final Object value) {
        if (key == null || value == null) return null;
        String k = String.valueOf(key);

        Object current = properties.getOrDefault(k, null);
        if (current == null) setProtected(k, String.valueOf(value));

        return current;
    }

    @Override
    public synchronized boolean remove(final Object key, final Object value) {
        if (key == null || value == null) return false;
        String k = String.valueOf(key);
        String v = String.valueOf(value);

        Object c = properties.getOrDefault(k, null);
        if (c == null) return false;

        return properties.remove(k, v);
    }

    @Override
    public synchronized boolean replace(final Object key, final Object oldValue, final Object newValue) {
        if (key == null) return false;
        String k = String.valueOf(key);

        Object old = null;
        if (hasAccess(k)) {
            if (oldValue == null) {
                if (properties.containsKey(k)) return false;
                if (newValue == null) return false;

                setProtected(k, String.valueOf(newValue));
                return true;
            }

            String o = String.valueOf(oldValue);
            String c = properties.getOrDefault(k, null);

            if (c == null) {
                setProtected(k, String.valueOf(newValue));
                return true;
            }

            if (!c.equals(o)) return false;
            if (newValue == null) {
                properties.remove(k);
                protectedProperties.remove(k);

                return true;
            } else {
                properties.put(k, String.valueOf(newValue));
            }
        }

        return true;
    }

    @Override
    public synchronized Object replace(final Object key, final Object value) {
        if (key == null) return null;
        String k = String.valueOf(key);

        Object old = null;
        if (hasAccess(k)) {
            if (value == null) {
                old = properties.remove(k);
                protectedProperties.remove(k);
            } else {
                old = properties.replace(k, String.valueOf(value));
            }
        }

        return old;
    }

    @Override
    public synchronized Object computeIfAbsent(final Object key, final Function<? super Object, ?> mappingFunction) {
        if (key == null || mappingFunction == null) return null;
        String k = String.valueOf(key);

        if (hasAccess(k)) {
            Function<String, String> functionTranslator = (s) -> {
                Object value1 = mappingFunction.apply(s);
                return String.valueOf(value1);
            };

            return properties.computeIfAbsent(String.valueOf(key), functionTranslator);
        }

        return null;
    }

    @Override
    public synchronized Object computeIfPresent(final Object key, final BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        if (key == null || remappingFunction == null) return null;
        String k = String.valueOf(key);

        if (hasAccess(k)) {
            BiFunction<String, String, String> functionTranslator = (s, s2) -> {
                Object value1 = remappingFunction.apply(s, s2);
                return String.valueOf(value1);
            };

            return properties.computeIfPresent(String.valueOf(key), functionTranslator);
        }

        return null;
    }

    @Override
    public synchronized Object compute(final Object key, final BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        if (key == null || remappingFunction == null) return null;
        String k = String.valueOf(key);

        if (hasAccess(k)) {
            BiFunction<String, String, String> functionTranslator = (s, s2) -> {
                Object value1 = remappingFunction.apply(s, s2);
                return String.valueOf(value1);
            };

            return properties.compute(String.valueOf(key), functionTranslator);
        }

        return null;
    }

    @Override
    public synchronized Object merge(final Object key, final Object value, final BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        if (key == null || value == null || remappingFunction == null) return null;
        String k = String.valueOf(key);

        if (hasAccess(k)) {
            BiFunction<String, String, String> functionTranslator = (s, s2) -> {
                Object value1 = remappingFunction.apply(s, s2);
                return String.valueOf(value1);
            };

            return properties.merge(String.valueOf(key), String.valueOf(value), functionTranslator);
        }

        return null;
    }

    /**
     * Set a protected property
     *
     * @param key the property key
     * @param value the property value
     * @throws SecurityException if the key is protected and the writer
     * is not the protected key writer
     */
    public void setProtected(final String key, final String value) throws SecurityException {
        APISource kore = KarmaKore.INSTANCE();
        if (kore == null) {
            setProperty(key, value);
            return;
        }

        SourceRuntime runtime = kore.runtime();
        try {
            Class<?> caller = runtime.getCallerClass();
            if (caller == null) {
                setProperty(key, value);
                return;
            }

            Path callerFile = runtime.getFileFrom(caller);
            if (callerFile == null) {
                setProperty(key, value);
                return;
            }

            if (protectedProperties.containsKey(key)) {
                Path protectedFile = protectedProperties.get(key);
                if (!protectedFile.equals(callerFile)) {
                    throw new SecurityException("Cannot overwrite protected property (" + key + "). Currently owned by: " + protectedFile);
                }
            }

            protectedProperties.put(key, callerFile);
            setProperty(key, value);
        } catch (ClassNotFoundException ex) {
            setProperty(key, value);
        }
    }

    /**
     * Make a property free
     *
     * @param key the property key
     * @throws SecurityException if the caller is not the protected key
     * issuer
     * @throws IllegalStateException if something goes wrong
     */
    public void makeFree(final String key) throws SecurityException, IllegalStateException {
        APISource kore = KarmaKore.INSTANCE();
        if (kore == null) {
            throw new IllegalStateException("Cannot make free a property because kore API is not available");
        }

        SourceRuntime runtime = kore.runtime();
        try {
            Class<?> caller = runtime.getCallerClass();
            if (caller == null) {
                throw new IllegalStateException("Cannot make free a property because we couldn't determine who is calling us");
            }

            Path callerFile = runtime.getFileFrom(caller);
            if (callerFile == null) {
                throw new IllegalStateException("Cannot make free a property because we couldn't determine which file is calling us");
            }

            if (protectedProperties.containsKey(key)) {
                Path protectedFile = protectedProperties.get(key);
                if (!protectedFile.equals(callerFile)) {
                    throw new SecurityException("Cannot change protected property (" + key + "). Currently owned by: " + protectedFile);
                }
            }

            protectedProperties.remove(key);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get if the writer has access
     *
     * @param key the key
     * @return if the writer has access
     */
    private boolean hasAccess(final String key) {
        APISource kore = KarmaKore.INSTANCE();
        if (kore == null) {
            return true;
        }

        if (KarmaAPI.isTestMode()) {
            if (!securityWarning) {
                kore.logger().send(LogLevel.WARNING, "({0}) Skipping security check because we are on test-unit. Aren't we?", LockedProperties.class);
                securityWarning = true;
            }

            return true; //We are in test unit
        }

        SourceRuntime runtime = kore.runtime();
        try {
            Class<?> caller = runtime.getCallerClass();
            if (caller == null) {
                return !protectedProperties.containsKey(key);
            }

            Path callerFile = runtime.getFileFrom(caller);
            if (callerFile == null) {
                return !protectedProperties.containsKey(key);
            }

            if (protectedProperties.containsKey(key)) {
                Path protectedFile = protectedProperties.get(key);
                if (!protectedFile.equals(callerFile)) {
                    return false;
                }
            }
        } catch (ClassNotFoundException ignored) {}

        return true;
    }
}
