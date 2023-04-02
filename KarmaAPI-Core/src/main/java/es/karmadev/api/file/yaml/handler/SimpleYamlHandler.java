package es.karmadev.api.file.yaml.handler;

import es.karmadev.api.file.RawType;
import es.karmadev.api.file.yaml.YamlFileHandler;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Simple yaml data handler
 */
class SimpleYamlHandler implements YamlFileHandler {

    /**
     * Yaml file
     */
    private final Path file;
    /**
     * Yaml data
     */
    private final Map<String, Object> data;
    /**
     * Yaml source
     */
    private final YamlReader source;

    /**
     * Initialize the yaml handler
     *
     * @param data the yaml data
     */
    SimpleYamlHandler(final Map<String, Object> data) {
        this(null, data, null);
    }

    /**
     * Initialize the yaml handler
     *
     * @param file the yaml file
     * @param data the yaml data
     */
    SimpleYamlHandler(final Path file, final Map<String, Object> data) {
        this(file, data, null);
    }

    /**
     * Initialize the yaml handler
     *
     * @param file the yaml file
     * @param data the yaml data
     * @param source the yaml source
     */
    SimpleYamlHandler(final Path file, final Map<String, Object> data, final YamlReader source) {
        this.file = file;
        this.data = data;
        this.source = source;
    }

    /**
     * Compare the objects
     *
     * @param path  the path to the value
     * @param value the value to compare
     * @return if the values are the same type
     */
    @Override
    public boolean compareValue(final String path, final Object value) {
        Object current = get(path);
        if (current == null) return value == null;

        if (current instanceof CharSequence) {
            String sequence = ((CharSequence) current).toString();

            if (value instanceof Character) return sequence.length() == 1;
            if (value instanceof Boolean) {
                return sequence.equalsIgnoreCase("y") ||
                        sequence.equalsIgnoreCase("1") ||
                        sequence.equalsIgnoreCase("true") ||
                        sequence.equalsIgnoreCase("n") ||
                        sequence.equalsIgnoreCase("0") ||
                        sequence.equalsIgnoreCase("false");
            }

            return value instanceof CharSequence;
        }
        if (current instanceof Number) {
            if (value instanceof Boolean) {
                Number number = (Number) current;
                return number.shortValue() == 0 || number.shortValue() == 1;
            }
            return value instanceof Number;
        }

        if (current instanceof Boolean) {
            return value instanceof Boolean;
        }
        return current.getClass().isAssignableFrom(value.getClass()) || value.getClass().isAssignableFrom(current.getClass()) ||
                current.getClass().isInstance(value) || value.getClass().isInstance(current);
    }

    /**
     * Transform a value into the specified raw type
     *
     * @param path the path to the value
     * @param type the new value type
     * @return if the value was able to be transformed
     */
    @Override
    public boolean transform(final String path, final RawType type) {
        RawType current = getType(path);
        switch (current) {
            case BOOLEAN:
                boolean bool = getBoolean(path);
                switch (type) {
                    case STRING:
                        set(path, (bool ? "true" : "false"));
                        return true;
                    case CHARACTER:
                        set(path, (bool ? 'y' : 'n'));
                        return true;
                    case NUMBER:
                        set(path, (bool ? 1 : 0));
                        return true;
                    default:
                        return false;
                }
            case NUMBER:
                Number number = (Number) get(path);
                switch (type) {
                    case CHARACTER:
                        set(path, (char) number.intValue());
                        return true;
                    case STRING:
                        set(path, number.intValue());
                        return true;
                    case BOOLEAN:
                        short val = number.shortValue();
                        if (val == 0) {
                            set(path, false);
                            return true;
                        }
                        if (val == 1) {
                            set(path, true);
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            case STRING:
                String str = getString(path);

                switch (type) {
                    case NUMBER:
                        try {
                            set(path, Long.parseLong(str));
                            return true;
                        } catch (NumberFormatException ex) {
                            try {
                                set(path, Double.parseDouble(str));
                                return true;
                            } catch (NumberFormatException ex2) {
                                return false;
                            }
                        }
                    case BOOLEAN:
                        if (str.equalsIgnoreCase("y") || str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1")) {
                            set(path, true);
                            return true;
                        }
                        if (str.equalsIgnoreCase("n") || str.equalsIgnoreCase("false") || str.equalsIgnoreCase("0")) {
                            set(path, true);
                            return true;
                        }

                        return false;
                    case CHARACTER:
                        if (str.length() >= 1) {
                            set(path, str.substring(0, 1));
                            return true;
                        }

                        return false;
                    default:
                        return false;
                }
            case CHARACTER:
                char chr = getCharacter(path);

                switch (type) {
                    case BOOLEAN:
                        if (chr == 'y' || chr == '1') {
                            set(path, true);
                            return true;
                        }
                        if (chr == 'n' || chr == '0') {
                            set(path, true);
                            return true;
                        }

                        return false;
                    case NUMBER:
                        set(path, (int) chr);
                        return true;
                    case STRING:
                        set(path, String.valueOf(chr));
                        return true;
                    default:
                        return false;
                }
            case NULL:
            case OBJECT:
            case SERIALIZABLE:
            case LIST:
            case SECTION:
            default:
                return false;
        }
    }

    /**
     * Get an object type
     *
     * @param path the path to the value object
     * @return the value type
     */
    @Override
    public RawType getType(final String path) {
        Object current = get(path);
        if (current == null) return RawType.NULL;

        if (current instanceof CharSequence) {
            String sequence = ((CharSequence) current).toString();

            if (sequence.length() == 1) return RawType.CHARACTER;
            return RawType.STRING;
        }
        if (current instanceof Number) {
            return RawType.NUMBER;
        }

        if (current instanceof Boolean) return RawType.BOOLEAN;
        if (current instanceof List) return RawType.LIST;
        if (current instanceof Map) return RawType.SECTION;

        try {
            getSerialized(path);
            return RawType.SERIALIZABLE;
        } catch (IOException | ClassNotFoundException ex) {
            return RawType.OBJECT;
        }
    }


    /**
     * Get an object
     *
     * @param path the object path
     * @param def  the object default value
     * @return the value or default value if
     * not found
     */
    @Override @SuppressWarnings("unchecked")
    public Object get(final String path, final Object def) {
        if (path.contains(".")) {
            String[] pathData = path.split("\\.");
            Map<String, Object> currentData = data;
            for (int i = 0; i < pathData.length; i++) {
                String dir = pathData[i];
                Object tmpValue = currentData.getOrDefault(dir, null);

                if (i == pathData.length - 1) return tmpValue;
                if (!(tmpValue instanceof Map)) return def;

                currentData = (Map<String, Object>) tmpValue;
            }
        }

        return data.getOrDefault(path, def);
    }

    /**
     * Get a string
     *
     * @param path the string path
     * @param def  the string default value
     * @return the value or default value if
     * not found
     */
    @Override
    public String getString(final String path, final String def) {
        Object value = get(path, def);
        if (!(value instanceof String)) return def;

        return (String) value;
    }

    /**
     * Get a character
     *
     * @param path the character path
     * @param def  the character default value
     * @return the value or default value if
     * not found
     */
    @Override
    public char getCharacter(final String path, final char def) {
        Object value = get(path, def);
        if (!(value instanceof String)) return def;

        return ((String) value).charAt(0);
    }

    /**
     * Get a byte
     *
     * @param path the byte path
     * @param def  the byte default value
     * @return the value or default value if
     * not found
     */
    @Override
    public byte getByte(final String path, final byte def) {
        Object value = get(path, def);
        if (!(value instanceof Number)) return def;

        return ((Number) value).byteValue();
    }

    /**
     * Get a short
     *
     * @param path the short path
     * @param def  the short default value
     * @return the value or default value if
     * not found
     */
    @Override
    public short getShort(final String path, final short def) {
        Object value = get(path, def);
        if (!(value instanceof Number)) return def;

        return ((Number) value).shortValue();
    }

    /**
     * Get an integer
     *
     * @param path the integer path
     * @param def  the integer default value
     * @return the value or default value if
     * not found
     */
    @Override
    public int getInteger(final String path, final int def) {
        Object value = get(path, def);
        if (!(value instanceof Number)) return def;

        return ((Number) value).intValue();
    }

    /**
     * Get a long
     *
     * @param path the long path
     * @param def  the long default value
     * @return the value or default value if
     * not found
     */
    @Override
    public long getLong(final String path, final long def) {
        Object value = get(path, def);
        if (!(value instanceof Number)) return def;

        return ((Number) value).longValue();
    }

    /**
     * Get a double
     *
     * @param path the double path
     * @param def  the double default value
     * @return the value or default value if
     * not found
     */
    @Override
    public double getDouble(final String path, final double def) {
        Object value = get(path, def);
        if (!(value instanceof Number)) return def;

        return ((Number) value).doubleValue();
    }

    /**
     * Get a float
     *
     * @param path the float path
     * @param def  the float default value
     * @return the value or default value if
     * not found
     */
    @Override
    public float getFloat(final String path, final float def) {
        Object value = get(path, def);
        if (!(value instanceof Number)) return def;

        return ((Number) value).floatValue();
    }

    /**
     * Get a boolean
     *
     * @param path the boolean path
     * @param def  the boolean default value
     * @return the value or default value if
     * not found
     */
    @Override
    public boolean getBoolean(final String path, final boolean def) {
        Object value = get(path, def);
        if (value instanceof Number) {
            Number number = (Number) value;
            short integer = number.shortValue();
            if (integer == 1) return true;
            if (integer == 0) return false;
            return def;
        }
        if (value instanceof String) {
            String string = (String) value;
            if (string.equalsIgnoreCase("y") || string.equalsIgnoreCase("true") || string.equalsIgnoreCase("1")) return true;
            if (string.equalsIgnoreCase("n") || string.equalsIgnoreCase("false") || string.equalsIgnoreCase("2")) return false;
            return def;
        }

        if (!(value instanceof Boolean)) return def;

        return (boolean) value;
    }

    /**
     * Get a serialized instance
     *
     * @param path the instance path
     * @param def  the instance default value
     * @return the value or default value if
     * not found
     *
     * @throws IOException if the unserialization fails
     * @throws ClassNotFoundException if the unserialization loads a class which does not exist anymore
     */
    @Override
    public Object getSerialized(final String path, final Object def) throws IOException, ClassNotFoundException {
        Object value = get(path, def);
        if (!(value instanceof String)) return def;

        byte[] data = Base64.getDecoder().decode((String) value);
        try(ByteArrayInputStream bi = new ByteArrayInputStream(data); ObjectInputStream si = new ObjectInputStream(bi)) {
            return si.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Get a list
     *
     * @param path the list path
     * @return the list
     */
    @Override
    public List<String> getList(final String path) {
        Object value = get(path, null);
        if (!(value instanceof List)) return new ArrayList<>();

        List<?> unknownList = (List<?>) value;
        List<String> result = new ArrayList<>();
        for (Object object : unknownList) {
            if (object != null) {
                result.add(String.valueOf(object));
            }
        }

        return result;
    }

    /**
     * Get a section
     *
     * @param path the section path
     * @return the section
     */
    @Override
    public @SuppressWarnings("unchecked") YamlFileHandler getSection(final String path) {
        Object value = get(path);
        if (!(value instanceof Map)) return null;

        Map<String, Object> map = (Map<String, Object>) value;
        return new YamlSection(map);
    }

    /**
     * Check if the path is set
     *
     * @param path the path
     * @return if the path is set
     */
    @Override
    public boolean isSet(final String path) {
        return get(path) != null;
    }

    /**
     * Check if the path is a section
     *
     * @param path the path
     * @return if the path is a section
     */
    @Override
    public boolean isSection(final String path) {
        return get(path) instanceof Map;
    }

    /**
     * Check if the path is the specified
     * type
     *
     * @param path the path
     * @param type the expected value
     * @return if the path is the specified
     * value
     */
    @Override
    public boolean isType(final String path, final RawType type) {
        return getType(path).equals(type);
    }

    /**
     * Get all the keys
     *
     * @param deep get keys and sub keys
     * @return the keys
     */
    @Override @SuppressWarnings("unchecked")
    public Collection<String> getKeys(final boolean deep) {
        List<String> keys = new ArrayList<>(data.keySet());

        if (deep) {
            List<String> deepKeys = new ArrayList<>();

            String currentPath = "";
            for (String key : keys) {
                Object value = data.getOrDefault(key, null);
                if (value == null) continue;

                if (value instanceof Map) {
                    deepKeys.add(key);
                    Map<String, Object> data = (Map<String, Object>) value;
                    deepKeys.addAll(mapKeys(key + ".", data));
                } else {
                    deepKeys.add(currentPath + key);
                }
            }

            return deepKeys;
        }

        return keys;
    }

    /**
     * Map the keys of a map
     *
     * @param path the main path
     * @param data the map data
     * @return the recursive keys
     */
    @SuppressWarnings("unchecked")
    private Collection<String> mapKeys(final String path, final Map<String, Object> data) {
        List<String> deepKeys = new ArrayList<>();

        for (String key : data.keySet()) {
            Object value = data.getOrDefault(key, null);
            if (value == null) continue;

            if (value instanceof Map) {
                deepKeys.add(path + key);
                Map<String, Object> subData = (Map<String, Object>) value;
                deepKeys.addAll(mapKeys(path + key + ".", subData));
            } else {
                deepKeys.add(path + key);
            }
        }

        return deepKeys;
    }

    /**
     * Save a value
     *
     * @param path the path
     * @param value the value
     */
    @SuppressWarnings("unchecked")
    private void save(final String path, final Object value) {
        if (path.contains(".")) {
            String[] pathData = path.split("\\.");
            Map<String, Object> node = data;

            for (int i = 0; i < pathData.length; i++) {
                String dir = pathData[i];
                Object dataValue = node.getOrDefault(dir, null);
                if (dataValue == null) {
                    if (i == pathData.length - 1) {
                        node.put(dir, value);
                    } else {
                        Map<String, Object> map = new LinkedHashMap<>();
                        node.put(dir, map);
                        node = map;
                    }
                } else {
                    if (i == pathData.length - 1) {
                        if (!(dataValue instanceof Map)) {
                            node.put(dir, value);
                        } else {
                            if (value instanceof Map) {
                                node.put(dir, value);
                            }
                        }
                    } else {
                        Object nodeValue = node.getOrDefault(dir, null);
                        if (nodeValue == null) nodeValue = new LinkedHashMap<>();
                        if (!(nodeValue instanceof Map)) return;

                        node = (Map<String, Object>) nodeValue;
                    }
                }
            }
        } else {
            data.put(path, value);
        }
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final String value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final char value) {
        save(path, String.valueOf(value));
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final byte value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final short value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final int value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final long value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final double value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final float value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final boolean value) {
        save(path, value);
    }

    /**
     * Save data
     *
     * @param path  the data path
     * @param value the data value
     */
    @Override
    public void set(final String path, final Serializable value) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream object = new ObjectOutputStream(out);
            object.writeObject(value);
            object.flush();

            save(path, Base64.getEncoder().encodeToString(out.toByteArray()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save data
     *
     * @param path   the data path
     * @param values the data value
     */
    @Override
    public void set(final String path, final List<String> values) {
        save(path, values);
    }

    /**
     * Save data
     *
     * @param path    the data path
     * @param section the data value
     */
    @Override
    public void set(final String path, final YamlFileHandler section) {
        for (String key : section.getKeys(true)) {
            String fullPath = path + "." + key;
            save(fullPath, section.get(key));
        }
    }

    /**
     * Get the yaml file raw data
     *
     * @return the yaml raw data
     */
    @Override
    public Map<String, Object> raw() {
        return new LinkedHashMap<>(data);
    }

    /**
     * Get the yaml handle
     *
     * @return the yaml handle
     */
    @Override @Nullable
    public Path handle() {
        return file;
    }

    /**
     * Save the current yaml file handler
     * to the specified file
     *
     * @param path the yaml file path
     * @return the saved yaml file
     * @throws IOException if the file fails to save
     */
    @Override
    public YamlFileHandler saveTo(final Path path) throws IOException {
        SimpleYamlHandler handler = new SimpleYamlHandler(path, data, source);
        handler.save();
        return handler;
    }

    /**
     * Save the yaml file
     *
     * @throws IOException if the file fails to save
     */
    @Override
    public void save() throws IOException {
        if (file == null || data == null) throw new IOException("Cannot save YAML without file or data");

        if (source != null) {
            String dump = source.dump(this);
            Files.write(file, dump.getBytes(StandardCharsets.UTF_8));

            return;
        }

        DumperOptions options = new DumperOptions();
        options.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        options.setProcessComments(true);
        options.setIndent(2);

        Yaml yaml = new Yaml(options);
        yaml.dump(data, Files.newBufferedWriter(file));
    }
}
