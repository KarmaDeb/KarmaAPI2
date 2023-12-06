package es.karmadev.api.kson.io;

import es.karmadev.api.kson.*;
import es.karmadev.api.kson.object.JsonNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a json reader
 */
public final class JsonReader {

    private final byte[] raw;

    /**
     * Initialize the json reader
     *
     * @param rawData the raw json data
     */
    private JsonReader(final byte[] rawData) {
        this.raw = rawData;
    }

    /**
     * Parses the data
     *
     * @return the parsed data
     * @throws KsonException if the data is invalid
     */
    public JsonInstance parse() throws KsonException {
        JsonObject object = JsonObject.newObject();
        String pretty = simplePrettify();

        String[] data = pretty.split("\n");
        for (int i = 0; i < data.length; i++) {
            String line = data[i];
            i = writeToObject(data, i, line, object);
        }

        return object;
    }

    /**
     * Create a simplified pretty version
     * of the parsed json data. This helps the
     * parser to parse the json easily
     *
     * @return the simplified pretty json
     */
    private String simplePrettify() {
        StringBuilder builder = new StringBuilder();

        boolean buildingString = false;
        boolean escape = false;
        int indentLevel = 0;

        String indent = buildIndent(indentLevel);
        boolean writeIndent = false;

        for (int i = 0; i < raw.length; i++) {
            byte b = raw[i];

            char byteChar = (char) b;
            if (!escape && byteChar == '"') {
                buildingString = !buildingString;
            }

            if (byteChar == '\\') {
                escape = !escape;
            } else {
                escape = false;
            }

            if (writeIndent) {
                builder.append(indent);
                writeIndent = false;
            }

            builder.append(byteChar);

            if (!buildingString) {
                switch (byteChar) {
                    case '{':
                    case '[':
                        builder.append("\n");
                        indent = buildIndent(++indentLevel);
                        writeIndent = true;
                        break;
                    case '}':
                        if (i - 1 > 0) {
                            byte previous = raw[i - 1];
                            char previousChar = (char) previous;

                            if (previousChar != '{' && previousChar != '[') {
                                break;
                            }
                        }
                    case ']':
                        builder.append("\n");
                        indent = buildIndent(--indentLevel);
                        writeIndent = true;
                        break;
                    case ',':
                        builder.append("\n");
                        writeIndent = true;
                        break;
                    default:
                        if (i + 1 < raw.length) {
                            byte next = raw[i + 1];
                            char nextChar = (char) next;
                            switch (nextChar) {
                                case '}':
                                case ']':
                                    builder.append("\n");
                                    indent = buildIndent(--indentLevel);
                                    writeIndent = true;
                                    break;
                            }
                        }
                        break;
                }
            }
        }

        return builder.toString();
    }

    /**
     * Build an indent for the required
     * indentation level
     *
     * @param level the indentation level
     * @return the indented level
     */
    private String buildIndent(final int level) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < level; i++) {
            builder.append('\t');
        }

        return builder.toString();
    }

    /**
     * Clean all the first spaces and tabulations
     * from the string, this helps the parser
     * to retrieve a clean key and value from a
     * json line
     *
     * @param string the line string
     * @return the clean line
     */
    private String cleanFirstSpaces(final String string) {
        int rIndex = 0;
        while (string.charAt(rIndex) == ' ' || string.charAt(rIndex) == '\t') {
            rIndex++;
        }

        return string.substring(rIndex);
    }

    /**
     * Capture a group on the sequence of lines, a group
     * refers to a json object, but this method might
     * also call to captureArray
     *
     * @param string the lines
     * @param index the index override
     * @return the group object
     */
    private JsonObject captureGroup(final String[] string, final AtomicInteger index) {
        int captureGroup = 1;
        int currentGroup = 1;

        JsonObject object = JsonObject.newObject();

        int originIndex = 0;
        int vIndex = index.get();
        for (String str : string) {
            if (str.contains(":")) {
                String key = captureKey(str);
                String value = captureValue(str);

                if (value.startsWith("{")) {
                    currentGroup++;

                    AtomicInteger subOverride = new AtomicInteger(vIndex);
                    JsonObject subGroup = captureGroup(Arrays.copyOfRange(string, originIndex + 1, string.length), subOverride);
                    object.put(key, subGroup);
                    vIndex += subOverride.get();

                    continue;
                }
                if (value.startsWith("[")) {
                    AtomicInteger subOverride = new AtomicInteger(vIndex);
                    JsonArray group = captureArray(Arrays.copyOfRange(string, originIndex + 1, string.length), subOverride);
                    object.put(key, group);
                    vIndex += subOverride.get();

                    continue;
                }

                if (captureGroup == currentGroup) {
                    object.put(key, rawToNative(value));
                }
            } else {
                if (str.contains("}")) {
                    currentGroup--;
                    if (currentGroup <= captureGroup) break;
                }
            }

            vIndex++;
            originIndex++;
        }

        index.set(vIndex);
        return object;
    }

    /**
     * Capture an array on the sequence of lines, an array
     * refers to a json array, but this method might
     * also call to captureGroup
     *
     * @param string the lines
     * @param index the index override
     * @return the array object
     */
    private JsonArray captureArray(final String[] string, final AtomicInteger index) {
        int captureGroup = 1;
        int currentGroup = 1;

        JsonArray array = JsonArray.newArray();
        int originIndex = 0;
        int vIndex = index.get();
        for (String str : string) {
            if (!str.contains(":")) {
                String rawValue = cleanFirstSpaces(str);

                if (rawValue.startsWith("{")) {
                    AtomicInteger subOverride = new AtomicInteger(vIndex);
                    JsonObject subGroup = captureGroup(Arrays.copyOfRange(string, originIndex + 1, string.length), subOverride);
                    array.add(subGroup);
                    vIndex += subOverride.get();

                    continue;
                }

                if (rawValue.startsWith("[")) {
                    currentGroup++;

                    AtomicInteger subOverride = new AtomicInteger(vIndex);
                    JsonArray group = captureArray(Arrays.copyOfRange(string, originIndex + 1, string.length), subOverride);
                    array.add(group);
                    vIndex += subOverride.get();

                    continue;
                }

                if (captureGroup == currentGroup) {
                    array.add(rawToNative(rawValue));
                }

                if (str.contains("]")) {
                    currentGroup--;
                    if (currentGroup <= captureGroup) break;
                }
            }

            vIndex++;
            originIndex++;
        }

        index.set(vIndex);
        return array;
    }

    /**
     * Get the capturing key of the current
     * line, this method assumes the line has ':'
     *
     * @param line the line
     * @return the capturing key
     */
    private String captureKey(final String line) {
        String[] info = line.split(":");
        String key = cleanFirstSpaces(info[0]);

        return key.substring(1, key.length() - 1);
    }

    /**
     * Get the capturing value of the current
     * line, this method assumes the line has ':'
     *
     * @param line the line
     * @return the capturing value
     */
    private String captureValue(final String line) {
        String[] info = line.split(":");
        String key = cleanFirstSpaces(info[0]);

        return cleanFirstSpaces(line.replaceFirst(key + ":", ""));
    }

    /**
     * Write information of the current line
     * into the object
     *
     * @param data the pretty data, this is
     *             helpful for recursive calls to captureGroup
     * @param lineIndex the current line index
     * @param line the current line
     * @param object the object to write to
     * @return the new line index override
     */
    private int writeToObject(final String[] data, final int lineIndex, final String line, final JsonObject object) {
        AtomicInteger indexOverride = new AtomicInteger(lineIndex);

        if (line.contains(":")) {
            String key = captureKey(line);
            String value = captureValue(line);

            if (value.startsWith("{")) {
                JsonObject group = captureGroup(Arrays.copyOfRange(data, lineIndex + 1, data.length), indexOverride);
                object.put(key, group);
                return indexOverride.get();
            }

            if (value.startsWith("[")) {
                JsonArray group = captureArray(Arrays.copyOfRange(data, lineIndex + 1, data.length), indexOverride);
                object.put(key, group);
                return indexOverride.get();
            }

            object.put(key, rawToNative(value));
        }

        return indexOverride.get();
    }

    /**
     * Parse a raw text into a native json
     * element
     *
     * @param raw the raw text
     * @return the native element
     */
    private JsonNative rawToNative(final String raw) {
        if (raw.startsWith("\"")) {
            int start = 1;
            int end = raw.length() - 1;
            if (raw.endsWith(",")) end = raw.length() - 2;

            String stringValue = raw.substring(start, end);
            return JsonNative.forSequence(stringValue);
        }

        String noDot = raw;
        if (raw.endsWith(",")) {
            noDot = raw.substring(0, raw.length() - 1);
        }

        if (noDot.equals("null")) {
            return JsonNull.get();
        } else {
            if (noDot.contains(",") || noDot.contains(".")
                    || noDot.contains("e") || noDot.contains("E")) {
                try {
                    Double db = Double.parseDouble(noDot);
                    return JsonNative.forNumber(db);
                } catch (NumberFormatException ignored) {}
            }

            try {
                Long l = Long.parseLong(noDot);
                return JsonNative.forNumber(l);
            } catch (NumberFormatException ex) {
                if (noDot.equalsIgnoreCase("true") || noDot.equalsIgnoreCase("false")) {
                    return JsonNative.forBoolean(Boolean.parseBoolean(noDot));
                }
            }
        }

        return null;
    }

    /**
     * Read a json
     *
     * @param data the raw json data to read
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    public static JsonInstance parse(final byte[] data) throws KsonException {
        JsonReader reader = new JsonReader(data);
        return reader.parse();
    }

    /**
     * Read a json
     *
     * @param json the raw json to read
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    public static JsonInstance read(final String json) throws KsonException {
        byte[] data = json.getBytes();

        JsonReader reader = new JsonReader(data);
        return reader.parse();
    }

    /**
     * Read a json
     *
     * @param stream the stream to read from
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    @Nullable
    public static JsonInstance read(final InputStream stream) throws KsonException {
        byte[] buffer = new byte[4096];
        int read = 0;

        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while ((read = stream.read(buffer, 0, read)) != -1) {
                out.write(buffer, 0, read);
            }

            JsonReader rd = new JsonReader(out.toByteArray());
            return rd.parse();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Read a json
     *
     * @param reader the reader that is reading
     *               the json
     * @return the json instance
     * @throws KsonException if the data fails to parse
     */
    @Nullable
    public static JsonInstance read(final Reader reader) throws KsonException {
        char[] charBuffer = new char[1024];
        int bytesRead = 0;

        try(CharArrayWriter charArrayWriter = new CharArrayWriter();) {
            while ((bytesRead = reader.read(charBuffer, 0, bytesRead)) != -1) {
                charArrayWriter.write(charBuffer, 0, bytesRead);
            }

            char[] rs = charArrayWriter.toCharArray();
            byte[] data = new String(rs).getBytes();

            JsonReader rd = new JsonReader(data);
            return rd.parse();
        } catch (IOException ex) {
            return null;
        }
    }
}