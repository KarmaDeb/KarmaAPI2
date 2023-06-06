package ml.karmaconfigs.api.common.karma.file;

import com.google.gson.*;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.strings.ListSpacer;
import es.karmadev.api.strings.StringUtils;
import ml.karmaconfigs.api.common.karma.file.element.KarmaPrimitive;
import ml.karmaconfigs.api.common.karma.file.element.KarmaSection;
import ml.karmaconfigs.api.common.karma.file.element.multi.KarmaArray;
import ml.karmaconfigs.api.common.karma.file.element.multi.KarmaMap;
import ml.karmaconfigs.api.common.karma.file.element.section.SectionContainer;
import ml.karmaconfigs.api.common.karma.file.element.types.*;
import ml.karmaconfigs.api.common.karma.file.error.KarmaFormatException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The KarmaFile, that contains all the file data
 *
 * @author KarmaDev
 * @since 1.3.2-SNAPSHOT
 * @deprecated Deprecated because JSON already does
 * the job
 */
@Deprecated
public class KarmaMain {

    private final Path document;

    private final Map<String, Element<?>> content = new LinkedHashMap<>();
    private final Map<Element<?>, String> reverse = new LinkedHashMap<>();
    private final Map<String, Integer> indexes = new HashMap<>();

    private String raw = "";
    private InputStream internal = null;

    /**
     * Initialize the file
     */
    public KarmaMain() {
        document = Paths.get(StringUtils.generateString() + ".kf");
        PathUtilities.createPath(document);
        PathUtilities.write(document, "(\"main\"\n)");
    }

    /**
     * Initialize the file
     *
     * @param doc the file that must be read
     * @throws IllegalStateException if something goes wrong
     */
    public KarmaMain(final Path doc) throws RuntimeException {
        document = doc;
        preCache();
    }

    /**
     * Initialize the file
     *
     * @param source the source file
     * @param name   the file name
     * @param path   the file path
     */
    public KarmaMain(final KarmaSource source, final String name, final String... path) {
        Path main = source.workingDirectory();
        for (String str : path)
            main = main.resolve(str);

        document = main.resolve(name);
        preCache();
    }

    /**
     * Initialize the file
     *
     * @param doc the file that must be read
     * @throws IOException if the temporal file could not
     *                     be created
     */
    public KarmaMain(final InputStream doc) throws IOException {
        String random = StringUtils.generateString();
        document = Files.createTempFile(random, "-kf");
        Files.copy(doc, document, StandardCopyOption.REPLACE_EXISTING);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> PathUtilities.destroy(document)));

        preCache();
    }

    /**
     * Initialize the file
     *
     * @param raw the raw karma main data
     * @throws IOException if the temporal file could not
     *                     be created
     */
    public KarmaMain(final String raw) throws IOException {
        String random = StringUtils.generateSplit();
        document = Files.createTempFile(random, "-kf");
        Files.write(document, raw.getBytes(StandardCharsets.UTF_8));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PathUtilities.destroy(document);
        }));

        preCache();
    }

    /**
     * Set the internal file to read from when generating defaults
     * or saving
     *
     * @param in the internal file
     * @return this instance
     */
    public KarmaMain internal(final InputStream in) {
        internal = in;
        return this;
    }

    /**
     * Pre cache all the file data to retrieve
     * it faster
     */
    public void preCache() {
        if (ObjectUtils.isNullOrEmpty(raw)) {
            List<String> lines = PathUtilities.readAllLines(document);
            if (lines.isEmpty() && internal != null) {
                try {
                    Files.copy(internal, document, StandardCopyOption.REPLACE_EXISTING);
                    lines = PathUtilities.readAllLines(document);
                } catch (Throwable ignored) {
                }
            }

            if (!lines.isEmpty()) {
                String fileLines = StringUtils.listToString(lines, ListSpacer.NEW_LINE);
                Pattern blockComment = Pattern.compile("\\*\\((?:.|[\\n\\r])*?\\)\\*|\\*/.*");
                Set<String> comments = new LinkedHashSet<>();
                Matcher commentMatcher = blockComment.matcher(fileLines);
                String remove_string = StringUtils.generateString();
                while (commentMatcher.find()) {
                    int start = commentMatcher.start();
                    int end = commentMatcher.end();

                    comments.add(fileLines.substring(start, end));
                }

                for (String comment : comments) {
                    fileLines = fileLines.replace(comment, remove_string);
                }

                String[] data = fileLines.split("\n");
                StringBuilder builder = new StringBuilder();
                for (String fl : data) {
                    if (!fl.replaceAll("\\s", "").equals(remove_string)) {
                        builder.append(fl).append("\n");
                    }
                }
                String result = builder.toString();
                lines = new ArrayList<>(Arrays.asList(StringUtils.replaceLast(result, "\n", "").split("\n")));

                StringBuilder rawBuilder = new StringBuilder();

                boolean underComment = false;
                boolean jump = true;
                boolean parsedFirst = false;

                String breaking = null;
                int index = 0;

                Set<String> added = new HashSet<>();
                for (String line : lines) {
                    if (!line.replaceAll("\\s", "").startsWith("*/")) {
                        int size = line.length();
                        indexes.put(line, ++index);

                        if (breaking != null) {
                            throw new KarmaFormatException(document, breaking, index);
                        }

                        boolean string = false;
                        for (int i = 0; i < size; i++) {
                            char current = line.charAt(i);
                            int nextIndex = (i + 1 != size ? i + 1 : i);
                            char next = line.charAt(nextIndex);

                            if (!underComment) {
                                jump = true;

                                if (current == '*') {
                                    if (next == '(') {
                                        underComment = true;
                                        jump = false;
                                    } else {
                                        i = size;
                                        continue;
                                    }
                                }

                                if (!underComment) {
                                    if (current == '(') {
                                        if (next != '"') {
                                            if (!parsedFirst) {
                                                parsedFirst = true;
                                                added.add("main");
                                                indexes.put("main", index);
                                            } else {
                                                breaking = "Error, found invalid section definition at " + line + ", it must be (\"x\" where 'x' is any value!";
                                            }
                                        } else {
                                            StringBuilder secName = new StringBuilder();
                                            boolean broke = false;
                                            for (int x = (nextIndex + 1); x < size; x++) {
                                                char tmp = line.charAt(x);
                                                if (tmp == '"') {
                                                    broke = true;
                                                    break;
                                                }

                                                secName.append(tmp);
                                            }

                                            if (broke) {
                                                String section = secName.toString();
                                                if (!added.contains(section)) {
                                                    added.add(section);
                                                    indexes.put(section, index);
                                                } else {
                                                    breaking = "Error, found repeated section definition " + section;
                                                }
                                            } else {
                                                breaking = "Error, found invalid section definition at " + line + ", it must be (\"x\" where 'x' is any value!";
                                            }
                                        }
                                    }

                                    if (current == '"')
                                        string = !string;

                                    if (!string) {
                                        char prev = line.charAt((i != 0 ? (i - 1) : 0));
                                        char prev1 = line.charAt((i > 2 ? i - 2 : 0));
                                        if (current == '-' && next == '>') {
                                            char cont = line.charAt((nextIndex + 1 != (size - 1) ? (nextIndex + 1) : nextIndex));
                                            char cont1 = line.charAt((nextIndex + 2 != (size - 1) ? (nextIndex + 2) : nextIndex));

                                            boolean error = false;
                                            if (Character.isSpaceChar(prev)) {
                                                if (!Character.isSpaceChar(prev1)) {
                                                    if (Character.isSpaceChar(cont)) {
                                                        if (!Character.isSpaceChar(cont1)) {
                                                            rawBuilder.append(current);
                                                        } else {
                                                            error = true;
                                                        }
                                                    } else {
                                                        error = true;
                                                    }
                                                } else {
                                                    error = true;
                                                }
                                            } else {
                                                if (prev == '<') {
                                                    error = !Character.isSpaceChar(prev1);
                                                } else {
                                                    error = true;
                                                }
                                            }

                                            if (error) {
                                                breaking = "Error, found invalid key -> value definition at " + line + ". It must be 'Key' -> \"Value\"";
                                            }
                                        } else {
                                            if (prev == '-' && prev1 == '<')
                                                rawBuilder.append(prev);

                                            rawBuilder.append(current);
                                        }
                                    } else {
                                        rawBuilder.append(current);
                                    }
                                }
                            } else {
                                underComment = current != ')' && next != '*';
                                if (!underComment) {
                                    i++;
                                    jump = true;
                                }
                            }
                        }

                        if (jump) {
                            rawBuilder.append("\n");
                        }
                    }
                }

                //We need raw text to parse easily the data...
                raw = rawBuilder.toString();
                String[] tmp = raw.split("\n");
                for (int i = 0; i < tmp.length; i++) {
                    if (!ObjectUtils.isNullOrEmpty(tmp[i])) {
                        raw = rawBuilder.substring(i);
                        break;
                    }
                }

                data = raw.split("\n");
                String main = data[0];
                if (main.equals("(") || main.equals("(\"main\"")) {
                    String parent = "main";
                    for (int i = 1; i < data.length; i++) {
                        String line = data[i];
                        if (line.replaceAll("\\s", "").startsWith("(")) {
                            String name = line.replaceAll("\\s", "");
                            name = name.replaceFirst("\\(", "");
                            name = name.replaceFirst("\"", "");
                            name = name.substring(0, name.length() - 1);

                            parent = parent + "." + name;
                        } else {
                            if (line.contains("->")) {
                                boolean rec = false;
                                Pattern pattern = Pattern.compile("' .?-> ");
                                Pattern badPattern = Pattern.compile("\" .?-> ");

                                Matcher matcher = pattern.matcher(line);
                                Matcher badMatcher = badPattern.matcher(line);

                                if (!matcher.find() && !badMatcher.find()) {
                                    throw new KarmaFormatException(document, "Error, couldn't find valid key format -> or <-> at ( " + line + " )", indexes.getOrDefault(line, -1));
                                }

                                int start;
                                int end;
                                boolean bad = false;
                                try {
                                    start = matcher.start();
                                    end = matcher.end();
                                } catch (Throwable ex) {
                                    start = badMatcher.start();
                                    end = badMatcher.end();
                                    bad = true;
                                }

                                String target = String.valueOf((bad ? '"' : '\''));

                                String match = line.substring(start + 2, end - 2).replaceAll("\\s", "");
                                if (match.equalsIgnoreCase("<-")) {
                                    rec = true;
                                }

                                String[] dt = line.split((rec ? "<->" : "->"));

                                String tmpName = dt[0].replaceAll("\\s", "");
                                if (!tmpName.startsWith(target) && !tmpName.endsWith(target))
                                    throw new KarmaFormatException(document, "Error, invalid key format, it must be 'x' where x is any value", indexes.getOrDefault(line, -1));

                                String name = StringUtils.replaceLast(dt[0].replaceFirst(target, ""), target, "");
                                String key = parent + "." + name.replaceAll("\\s", "");
                                StringBuilder value = new StringBuilder();
                                for (int x = 1; x < dt.length; x++) {
                                    value.append(dt[x]).append((x != dt.length - 1 ? (rec ? "<->" : "->") : ""));
                                }

                                if (!ObjectUtils.isNullOrEmpty(value)) {
                                    if (value.toString().replaceAll("\\s", "").startsWith("{")) {
                                        boolean keyed = false;
                                        boolean simple = false;
                                        Element<?> array = new KarmaArray();
                                        int subIndex = 0;

                                        String parentKey = key;
                                        for (int x = (i + 1); x < data.length; x++, subIndex++) {
                                            line = data[x];

                                            if (!line.replaceAll("\\s", "").endsWith("}")) {
                                                if (!line.replaceAll("\\s", "").isEmpty()) {
                                                    if (line.contains("<->")) {
                                                        if (!(array instanceof ElementMap))
                                                            array = new KarmaMap();

                                                        if (!simple) {
                                                            keyed = true;
                                                            dt = line.split("->");

                                                            tmpName = dt[0].replaceAll("\\s", "");
                                                            if (!tmpName.startsWith("'") && !tmpName.endsWith("'"))
                                                                throw new KarmaFormatException(document, "Error, invalid key format, it must be 'x' where x is any value", indexes.getOrDefault(line, -1));

                                                            int i1 = dt[0].indexOf("'");
                                                            int i2 = dt[0].lastIndexOf("'");

                                                            name = StringUtils.replaceLast(dt[0], "<", "");
                                                            key = name.substring(i1 + 1, i2);
                                                            value = new StringBuilder();
                                                            for (int y = 1; y < dt.length; y++) {
                                                                value.append(dt[y]).append((y != dt.length - 1 ? "->" : ""));
                                                            }

                                                            ElementMap<ElementPrimitive> ka = (KarmaMap) array;

                                                            String v = value.toString().replaceFirst("\t", "").replaceFirst(" ", "");
                                                            if (!ObjectUtils.isNullOrEmpty(v)) {
                                                                if (v.startsWith("\"") || v.startsWith("'")) {
                                                                    if (!v.endsWith((v.startsWith("\"") ? "\"" : "'"))) {
                                                                        throw new KarmaFormatException(document, "Error, invalid text format. It seems that you mixed quotes or forgot to close string", indexes.getOrDefault(line, -1));
                                                                    } else {
                                                                        v = v.substring(1, v.length() - 1);
                                                                        ElementPrimitive obj;
                                                                        if (v.length() == 1) {
                                                                            obj = new KarmaPrimitive(v.charAt(0));
                                                                        } else {
                                                                            obj = new KarmaPrimitive(v);
                                                                        }

                                                                        ka.putRecursive(key, obj);
                                                                    }
                                                                } else {
                                                                    if (v.replaceAll("\\s", "").equalsIgnoreCase("true") || v.replaceAll("\\s", "").equalsIgnoreCase("false")) {
                                                                        boolean bool = Boolean.parseBoolean(v.replaceAll("\\s", ""));
                                                                        ElementPrimitive obj = new KarmaPrimitive(bool);

                                                                        ka.putRecursive(key, obj);
                                                                    } else {
                                                                        if (v.contains(",")) {
                                                                            Number number = Double.parseDouble(v.replaceAll("\\s", "").replace(",", "."));
                                                                            ElementPrimitive obj = new KarmaPrimitive(number);

                                                                            ka.putRecursive(key, obj);
                                                                        } else {
                                                                            if (v.contains(".")) {
                                                                                Number number = Float.parseFloat(v.replaceAll("\\s", ""));
                                                                                ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                ka.putRecursive(key, obj);
                                                                            } else {
                                                                                try {
                                                                                    Number number = Integer.parseInt(v.replaceAll("\\s", ""));
                                                                                    ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                    ka.putRecursive(key, obj);
                                                                                } catch (NumberFormatException e) {
                                                                                    if (v.startsWith("0x")) {
                                                                                        String bData = v.replace("0x", "");
                                                                                        ka.putRecursive(key, new KarmaPrimitive((byte) Integer.parseInt(bData, 16)));
                                                                                    } else {
                                                                                        if (v.equals("null")) {
                                                                                            ka.putRecursive(key, KarmaPrimitive.forNull());
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            throw new KarmaFormatException(document, "Error, invalid list format. It seems that you mixed a simple list and a keyed list", indexes.getOrDefault(line, -1));
                                                        }
                                                    } else {
                                                        if (line.contains("->")) {
                                                            if (!(array instanceof ElementMap))
                                                                array = new KarmaMap();

                                                            if (!simple) {
                                                                keyed = true;
                                                                dt = line.split("->");

                                                                tmpName = dt[0].replaceAll("\t", "").replaceAll("\\s", "");
                                                                if (!tmpName.startsWith("'") && !tmpName.endsWith("'"))
                                                                    throw new KarmaFormatException(document, "Error, invalid key format, it must be 'x' where x is any value", indexes.getOrDefault(line, -1));

                                                                int i1 = dt[0].indexOf("'");
                                                                int i2 = dt[0].lastIndexOf("'");

                                                                name = StringUtils.replaceLast(dt[0], "<", "");
                                                                key = name.substring(i1 + 1, i2);
                                                                value = new StringBuilder();
                                                                for (int y = 1; y < dt.length; y++) {
                                                                    value.append(dt[y]).append((y != dt.length - 1 ? "->" : ""));
                                                                }

                                                                ElementMap<ElementPrimitive> ka = (KarmaMap) array;

                                                                String v = value.toString().replaceFirst("\t", "").replaceFirst(" ", "");
                                                                if (!ObjectUtils.isNullOrEmpty(v)) {
                                                                    if (v.startsWith("\"") || v.startsWith("'")) {
                                                                        if (!v.endsWith((v.startsWith("\"") ? "\"" : "'"))) {
                                                                            throw new KarmaFormatException(document, "Error, invalid text format. It seems that you mixed quotes or forgot to close string", indexes.getOrDefault(line, -1));
                                                                        } else {
                                                                            v = v.substring(1, v.length() - 1);
                                                                            ElementPrimitive obj;
                                                                            if (v.length() == 1) {
                                                                                obj = new KarmaPrimitive(v.charAt(0));
                                                                            } else {
                                                                                obj = new KarmaPrimitive(v);
                                                                            }
                                                                            ;

                                                                            ka.put(key, obj);
                                                                        }
                                                                    } else {
                                                                        if (v.replaceAll("\\s", "").equalsIgnoreCase("true") || v.replaceAll("\\s", "").equalsIgnoreCase("false")) {
                                                                            boolean bool = Boolean.parseBoolean(v.replaceAll("\\s", ""));
                                                                            ElementPrimitive obj = new KarmaPrimitive(bool);

                                                                            ka.put(key, obj);
                                                                        } else {
                                                                            if (v.contains(",")) {
                                                                                Number number = Double.parseDouble(v.replaceAll("\\s", "").replace(",", "."));
                                                                                ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                ka.put(key, obj);
                                                                            } else {
                                                                                if (v.contains(".")) {
                                                                                    Number number = Float.parseFloat(v.replaceAll("\\s", ""));
                                                                                    ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                    ka.put(key, obj);
                                                                                } else {
                                                                                    try {
                                                                                        Number number = Integer.parseInt(v.replaceAll("\\s", ""));
                                                                                        ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                        ka.put(key, obj);
                                                                                    } catch (NumberFormatException e) {
                                                                                        if (v.startsWith("0x")) {
                                                                                            String bData = v.replace("0x", "");
                                                                                            ka.put(key, new KarmaPrimitive((byte) Integer.parseInt(bData, 16)));
                                                                                        } else {
                                                                                            if (v.equals("null")) {
                                                                                                ka.put(key, KarmaPrimitive.forNull());
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                throw new KarmaFormatException(document, "Error, invalid list format. It seems that you mixed a simple list and a keyed list", indexes.getOrDefault(line, -1));
                                                            }
                                                        } else {
                                                            if (!keyed) {
                                                                if (!(array instanceof ElementArray))
                                                                    array = new KarmaArray();

                                                                simple = true;

                                                                value = new StringBuilder();
                                                                boolean parsing = false;
                                                                for (int y = 1; y < line.length(); y++) {
                                                                    char character = line.charAt(y);
                                                                    if (!parsing) {
                                                                        if (!Character.isSpaceChar(character)) {
                                                                            parsing = true;
                                                                        }
                                                                    }

                                                                    if (parsing) {
                                                                        value.append(character);
                                                                    }
                                                                }

                                                                ElementArray<ElementPrimitive> ka = (KarmaArray) array;

                                                                String v = value.toString().replaceFirst("\t", "");
                                                                if (!ObjectUtils.isNullOrEmpty(v)) {
                                                                    if (!v.equals("{")) {
                                                                        if (v.startsWith("\"") || v.startsWith("'")) {
                                                                            if (!v.endsWith((v.startsWith("\"") ? "\"" : "'"))) {
                                                                                throw new KarmaFormatException(document, "Error, invalid text format. It seems that you mixed quotes or forgot to close string", indexes.getOrDefault(line, -1));
                                                                            } else {
                                                                                v = v.substring(1, v.length() - 1);
                                                                                ElementPrimitive obj;
                                                                                if (v.length() == 1) {
                                                                                    obj = new KarmaPrimitive(v.charAt(0));
                                                                                } else {
                                                                                    obj = new KarmaPrimitive(v);
                                                                                }

                                                                                ka.add(obj);
                                                                            }
                                                                        } else {
                                                                            if (v.replaceAll("\\s", "").equalsIgnoreCase("true") || v.replaceAll("\\s", "").equalsIgnoreCase("false")) {
                                                                                boolean bool = Boolean.parseBoolean(v.replaceAll("\\s", ""));
                                                                                ElementPrimitive obj = new KarmaPrimitive(bool);

                                                                                ka.add(obj);
                                                                            } else {
                                                                                if (v.contains(",")) {
                                                                                    Number number = Double.parseDouble(v.replaceAll("\\s", "").replace(",", "."));
                                                                                    ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                    ka.add(obj);
                                                                                } else {
                                                                                    if (v.contains(".")) {
                                                                                        Number number = Float.parseFloat(v.replaceAll("\\s", ""));
                                                                                        ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                        ka.add(obj);
                                                                                    } else {
                                                                                        try {
                                                                                            Number number = Integer.parseInt(v.replaceAll("\\s", ""));
                                                                                            ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                            ka.add(obj);
                                                                                        } catch (
                                                                                                NumberFormatException e) {
                                                                                            if (v.startsWith("0x")) {
                                                                                                String bData = v.replace("0x", "");
                                                                                                ka.add(new KarmaPrimitive((byte) Integer.parseInt(bData, 16)));
                                                                                            } else {
                                                                                                if (v.equals("null")) {
                                                                                                    ka.add(KarmaPrimitive.forNull());
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                throw new KarmaFormatException(document, "Error, invalid list format. It seems that you mixed a simple list and a keyed list", indexes.getOrDefault(line, -1));
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                i = x;
                                                break;
                                            }
                                        }

                                        content.put(parentKey, array);
                                        if (rec)
                                            reverse.put(array, parentKey);
                                    } else {
                                        if (value.toString().replaceAll("\\s", "").startsWith("[")) {
                                            KarmaMap map = new KarmaMap();
                                            int subIndex = 0;

                                            String parentKey = key;
                                            for (int x = (i + 1); x < data.length; x++, subIndex++) {
                                                line = data[x];

                                                if (!line.replaceAll("\\s", "").endsWith("]")) {
                                                    if (!line.replaceAll("\\s", "").isEmpty()) {
                                                        if (line.contains("->")) {
                                                            dt = line.split("->");

                                                            tmpName = dt[0].replaceAll("\\s", "");
                                                            if (!tmpName.startsWith("'") && !tmpName.endsWith("'")) {
                                                                if (!tmpName.startsWith("\"") && !tmpName.startsWith("\"")) {
                                                                    throw new KarmaFormatException(document, "Error, invalid key format, it must be 'x' or \"x\" where x is any value", indexes.getOrDefault(line, -1));
                                                                }
                                                            }

                                                            int i1 = tmpName.startsWith("\"") ? dt[0].indexOf("\"") : dt[0].indexOf("'");
                                                            int i2 = tmpName.endsWith("\"") ? dt[0].lastIndexOf("\"") : dt[0].lastIndexOf("'");

                                                            name = StringUtils.replaceLast(dt[0], "<", "");
                                                            key = name.substring(i1 + 1, i2);
                                                            value = new StringBuilder();
                                                            for (int y = 1; y < dt.length; y++) {
                                                                value.append(dt[y]).append((y != dt.length - 1 ? "->" : ""));
                                                            }

                                                            String v = value.toString().replaceFirst("\t", "").replaceFirst(" ", "");
                                                            if (!ObjectUtils.isNullOrEmpty(v)) {
                                                                if (v.startsWith("\"") || v.startsWith("'")) {
                                                                    if (!v.endsWith((v.startsWith("\"") ? "\"" : "'"))) {
                                                                        throw new KarmaFormatException(document, "Error, invalid text format. It seems that you mixed quotes or forgot to close string", indexes.getOrDefault(line, -1));
                                                                    } else {
                                                                        v = v.substring(1, v.length() - 1);
                                                                        ElementPrimitive obj;
                                                                        if (v.length() == 1) {
                                                                            obj = new KarmaPrimitive(v.charAt(0));
                                                                        } else {
                                                                            obj = new KarmaPrimitive(v);
                                                                        }

                                                                        map.putRecursive(key, obj);
                                                                    }
                                                                } else {
                                                                    if (v.replaceAll("\\s", "").equalsIgnoreCase("true") || v.replaceAll("\\s", "").equalsIgnoreCase("false")) {
                                                                        boolean bool = Boolean.parseBoolean(v.replaceAll("\\s", ""));
                                                                        ElementPrimitive obj = new KarmaPrimitive(bool);

                                                                        map.putRecursive(key, obj);
                                                                    } else {
                                                                        if (v.contains(",")) {
                                                                            Number number = Double.parseDouble(v.replaceAll("\\s", "").replace(",", "."));
                                                                            ElementPrimitive obj = new KarmaPrimitive(number);

                                                                            map.putRecursive(key, obj);
                                                                        } else {
                                                                            if (v.contains(".")) {
                                                                                Number number = Float.parseFloat(v.replaceAll("\\s", ""));
                                                                                ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                map.putRecursive(key, obj);
                                                                            } else {
                                                                                try {
                                                                                    Number number = Integer.parseInt(v.replaceAll("\\s", ""));
                                                                                    ElementPrimitive obj = new KarmaPrimitive(number);

                                                                                    map.putRecursive(key, obj);
                                                                                } catch (NumberFormatException e) {
                                                                                    if (v.startsWith("0x")) {
                                                                                        String bData = v.replace("0x", "");
                                                                                        map.putRecursive(key, new KarmaPrimitive((byte) Integer.parseInt(bData, 16)));
                                                                                    } else {
                                                                                        if (v.equals("null")) {
                                                                                            map.putRecursive(key, KarmaPrimitive.forNull());
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            throw new KarmaFormatException(document, "Error, invalid map format. Missing key and value syntax 'key' -> value", indexes.getOrDefault(line, -1));
                                                        }
                                                    }
                                                } else {
                                                    i = x;
                                                    break;
                                                }
                                            }

                                            content.put(parentKey, map);
                                            if (rec)
                                                reverse.put(map, parentKey);
                                        } else {
                                            String v = value.toString().replaceFirst("\t", "").replaceFirst(" ", "");
                                            if (v.startsWith("\"") || v.startsWith("'")) {
                                                if (!v.endsWith((v.startsWith("\"") ? "\"" : "'"))) {
                                                    throw new KarmaFormatException(document, "Error, invalid text format. It seems that you mixed quotes or forgot to close string", indexes.getOrDefault(line, -1));
                                                } else {
                                                    v = v.substring(1, v.length() - 1);
                                                    ElementPrimitive obj;
                                                    if (v.length() == 1) {
                                                        obj = new KarmaPrimitive(v.charAt(0));
                                                    } else {
                                                        obj = new KarmaPrimitive(v);
                                                    }

                                                    content.put(key, obj);
                                                    if (rec)
                                                        reverse.put(obj, key);
                                                }
                                            } else {
                                                if (v.replaceAll("\\s", "").equalsIgnoreCase("true") || v.replaceAll("\\s", "").equalsIgnoreCase("false")) {
                                                    boolean bool = Boolean.parseBoolean(v.replaceAll("\\s", ""));
                                                    ElementPrimitive obj = new KarmaPrimitive(bool);

                                                    content.put(key, obj);
                                                    if (rec)
                                                        reverse.put(obj, key);
                                                } else {
                                                    if (v.contains(",")) {
                                                        Number number = Double.parseDouble(v.replaceAll("\\s", "").replace(",", "."));
                                                        ElementPrimitive obj = new KarmaPrimitive(number);

                                                        content.put(key, obj);
                                                        if (rec)
                                                            reverse.put(obj, key);
                                                    } else {
                                                        if (v.contains(".")) {
                                                            Number number = Float.parseFloat(v.replaceAll("\\s", ""));
                                                            ElementPrimitive obj = new KarmaPrimitive(number);

                                                            content.put(key, obj);
                                                            if (rec)
                                                                reverse.put(obj, key);
                                                        } else {
                                                            try {
                                                                Number number = Integer.parseInt(v.replaceAll("\\s", ""));
                                                                ElementPrimitive obj = new KarmaPrimitive(number);

                                                                content.put(key, obj);
                                                                if (rec)
                                                                    reverse.put(obj, key);
                                                            } catch (NumberFormatException e) {
                                                                if (v.startsWith("0x")) {
                                                                    String bData = v.replace("0x", "");
                                                                    content.put(key, new KarmaPrimitive((byte) Integer.parseInt(bData, 16)));
                                                                } else {
                                                                    if (v.equals("null")) {
                                                                        content.put(key, KarmaPrimitive.forNull());
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    ElementPrimitive obj = new KarmaPrimitive("");

                                    content.put(key, obj);
                                    if (rec)
                                        reverse.put(obj, key);
                                }
                            } else {
                                if (line.replaceAll("\\s", "").endsWith(")")) {
                                    if (parent.contains(".")) {
                                        String[] pData = parent.split("\\.");
                                        parent = StringUtils.replaceLast(parent, "." + pData[pData.length - 1], "");
                                        if (pData.length == 1)
                                            parent = pData[0];
                                    }
                                }
                            }
                        }
                    }

                    if (!parent.equals("main")) {
                        String[] pData = parent.split("\\.");
                        throw new KarmaFormatException(document, "Error, non closed section path ( " + parent + " )", indexes.getOrDefault(pData[pData.length - 1], -1));
                    }
                } else {
                    throw new KarmaFormatException(document, "Error, found invalid main section name at " + main + "; it must be \"main\" or empty!", indexes.getOrDefault(main, -1));
                }
            }
        }
    }

    /**
     * Export the default file, reading from internal.
     * <p>
     * PLEASE NOTE:
     * Executing this method will replace all the file contents without
     * performing any type of check first
     *
     * @return if the file could be exported
     */
    public boolean exportDefaults() {
        if (internal != null) {
            try {
                PathUtilities.createPath(document);
                Files.copy(internal, document, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (Throwable ignored) {
            }
        }

        return false;
    }

    /**
     * Get the file content as a raw string without comments
     *
     * @return the raw contents
     */
    public String getRaw() {
        if (ObjectUtils.isNullOrEmpty(raw))
            preCache();

        return raw;
    }

    /**
     * Get all the main section keys
     *
     * @return the file keys
     * @throws KarmaFormatException if the file could not be parsed correctly
     */
    public Set<String> getKeys() throws KarmaFormatException {
        return new LinkedHashSet<>(content.keySet());
    }

    /**
     * Get a value
     *
     * @param key the value key
     * @return the value or null if not a section
     */
    @Nullable
    public KarmaSection getSection(final String key) throws KarmaFormatException {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + key;

        KarmaSection section = null;
        for (String k : content.keySet()) {
            if (!k.equals(tmpKey)) {
                if (k.startsWith(tmpKey)) {
                    String[] keyData = k.split("\\.");
                    if (keyData[keyData.length - 2].equals(key.replaceFirst("main\\.", ""))) {
                        section = new SectionContainer(this, k.replace("." + keyData[keyData.length - 2] + "." + keyData[keyData.length - 1], ""), key.replaceFirst("main\\.", ""));
                        break;
                    }
                }
            }
        }

        return section;
    }

    /**
     * Get a value
     *
     * @param key the value key
     * @return the value
     */
    public Element<?> get(final String key) throws KarmaFormatException {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + key;

        return content.getOrDefault(tmpKey, KarmaPrimitive.forNull());
    }

    /**
     * Get a value
     *
     * @param key the value key
     * @param def the default value
     * @return the value
     */
    public <T extends Element<?>> Element<?> get(final String key, final T def) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + key;

        return content.getOrDefault(tmpKey, def);
    }

    /**
     * Get a key
     *
     * @param element the key value
     * @return the key
     */
    public String get(final Element<?> element) {
        return reverse.getOrDefault(element, "").replaceFirst("main\\.", "");
    }

    /**
     * Get a key
     *
     * @param element the key value
     * @param def     the default key
     * @return the key
     */
    public String get(final Element<?> element, final String def) {
        return reverse.getOrDefault(element, def).replaceFirst("main\\.", "");
    }

    /**
     * Get if a key element is recursive
     *
     * @param key the key
     * @return if the key element is recursive
     */
    public boolean isRecursive(final String key) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + key;

        Element<?> element = content.getOrDefault(tmpKey, KarmaPrimitive.forNull());
        if (element != null) {
            //Return true if the key element can be retrieved with the key and vice versa
            String tmp = reverse.getOrDefault(element, null);
            return tmpKey.equals(tmp);
        }

        return false;
    }

    /**
     * Get if a element is recursive
     *
     * @param element the element
     * @return if the element is recursive
     */
    public boolean isRecursive(final Element<?> element) {
        String key = reverse.getOrDefault(element, null);
        if (key != null) {
            //Return true if the element key can be retrieved with the element and vice versa
            Element<?> tmp = content.getOrDefault(key, null);
            return tmp == element;
        }

        return false;
    }

    /**
     * Get if a key is set
     *
     * @param key the key to find
     * @return if the key is set
     */
    public boolean isSet(final String key) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + key;

        Element<?> result = content.getOrDefault(tmpKey, null);
        return result != null && !result.isElementNull();
    }

    /**
     * Get if the document file exists
     *
     * @return if the document file exists
     */
    public boolean exists() {
        return Files.exists(document);
    }

    /**
     * Set a value
     *
     * @param key     the value key
     * @param element the value
     */
    public <T extends Element<?>> void set(final String key, final T element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            content.put(tmpKey, element);
        }
    }

    /**
     * Set a value
     *
     * @param key     the value key
     * @param element the value
     */
    public void setRaw(final String key, final String element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            content.put(tmpKey, new KarmaPrimitive(element));
        }
    }

    /**
     * Set a value
     *
     * @param key     the value key
     * @param element the value
     */
    public void setRaw(final String key, final boolean element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        content.put(tmpKey, new KarmaPrimitive(element));
    }

    /**
     * Set a value
     *
     * @param key     the value key
     * @param element the value
     */
    public void setRaw(final String key, final Number element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            content.put(tmpKey, new KarmaPrimitive(element));
        }
    }

    /**
     * Set a value
     *
     * @param key     the value key
     * @param element the value
     */
    public void setRaw(final String key, final byte element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        content.put(tmpKey, new KarmaPrimitive(element));
    }

    /**
     * Set a value
     *
     * @param key     the value key
     * @param element the value
     */
    public void setRaw(final String key, final char element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        content.put(tmpKey, new KarmaPrimitive(element));
    }

    /**
     * Set a value
     *
     * @param key     the value key
     * @param element the value
     */
    public void setRaw(final String key, final Object element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            try {
                content.put(tmpKey, new KarmaPrimitive(element.toString()));
            } catch (Throwable ex) {
                content.put(tmpKey, new KarmaPrimitive(String.valueOf(element)));
            }
        }
    }

    /**
     * Set recursively a value
     *
     * @param key the value key
     * @param element the value
     */
    public void setRecursive(final String key, final Element<?> element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            content.put(tmpKey, element);
            reverse.put(element, key);
        }
    }

    /**
     * Set recursively a value
     *
     * @param key the value key
     * @param element the value
     */
    public void setRawRecursive(final String key, final String element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            ElementPrimitive primitive = new KarmaPrimitive(element);

            content.put(tmpKey, primitive);
            reverse.put(primitive, tmpKey);
        }
    }

    /**
     * Set recursively a value
     *
     * @param key the value key
     * @param element the value
     */
    public void setRawRecursive(final String key, final boolean element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        ElementPrimitive primitive = new KarmaPrimitive(element);

        content.put(tmpKey, primitive);
        reverse.put(primitive, tmpKey);
    }

    /**
     * Set recursively a value
     *
     * @param key the value key
     * @param element the value
     */
    public void setRawRecursive(final String key, final Number element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            ElementPrimitive primitive = new KarmaPrimitive(element);

            content.put(tmpKey, primitive);
            reverse.put(primitive, tmpKey);
        }
    }

    /**
     * Set recursively a value
     *
     * @param key the value key
     * @param element the value
     */
    public void setRawRecursive(final String key, final byte element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        ElementPrimitive primitive = new KarmaPrimitive(element);

        content.put(tmpKey, primitive);
        reverse.put(primitive, tmpKey);
    }

    /**
     * Set recursively a value
     *
     * @param key the value key
     * @param element the value
     */
    public void setRawRecursive(final String key, final char element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        ElementPrimitive primitive = new KarmaPrimitive(element);

        content.put(tmpKey, primitive);
        reverse.put(primitive, tmpKey);
    }

    /**
     * Set recursively a value
     *
     * @param key the value key
     * @param element the value
     */
    public void setRawRecursive(final String key, final Object element) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        if (element != null) {
            ElementPrimitive primitive;
            try {
                primitive = new KarmaPrimitive(element.toString());
            } catch (Throwable ex) {
                primitive = new KarmaPrimitive(String.valueOf(element));
            }

            content.put(tmpKey, primitive);
            reverse.put(primitive, tmpKey);
        }
    }

    /**
     * Unsets a key
     *
     * @param key the key to unset
     */
    public void unset(final String key) {
        String tmpKey = key;
        if (!tmpKey.startsWith("main."))
            tmpKey = "main." + tmpKey;

        Element<?> e = content.remove(tmpKey);
        if (e != null) {
            reverse.remove(e); //Just in case
        }
    }

    /**
     * Create the document if it doesn't
     * exist
     */
    public void create() {
        PathUtilities.createPath(document);
    }

    /**
     * Save the file
     *
     * @throws KarmaFormatException if the file could not be parsed correctly
     */
    public boolean save() {
        return save(document);
    }

    /**
     * Save the file
     *
     * @param flName the target file name
     * @param sub    the file subdirectories
     * @throws KarmaFormatException if the file could not be parsed correctly
     */
    public boolean save(final String flName, final String... sub) throws KarmaFormatException {
        return save(Paths.get(flName, sub));
    }

    /**
     * Save the file
     *
     * @param target the target file
     * @throws KarmaFormatException if the file could not be parsed correctly
     */
    public boolean save(final Path target) throws KarmaFormatException {
        if (!exists())
            create();

        KarmaSource source = KarmaKore.INSTANCE();
        try {
            source.logger().send("Saving file {0}", LogLevel.DEBUG, PathUtilities.pathString(target));

            List<String> write = new ArrayList<>();
            List<String> lines = PathUtilities.readAllLines(document);
            Set<String> wrote_keys = new HashSet<>();

            if (!lines.isEmpty()) {
                source.logger().send("File is not empty", LogLevel.DEBUG);

                write.add("(\"main\"");
                Pattern keyMatcher = Pattern.compile("'.*' .?->");
                Pattern badKeyMatcher = Pattern.compile("\".*\" .?->");

                StringBuilder section = new StringBuilder();

                int index = 1;
                boolean bigComment = false;
                boolean readingList = false;
                boolean readingMap = false;
                for (String line : lines) {
                    String noSpace = line.replaceAll("\\s", "");
                    if (noSpace.equals("\n") || ObjectUtils.isNullOrEmpty(noSpace)) {
                        write.add(line);
                        continue;
                    }

                    Matcher matcher = keyMatcher.matcher(line);
                    Matcher badMatcher = badKeyMatcher.matcher(line);

                    if (matcher.find() || badMatcher.find() && !readingList && !readingMap) {
                        source.logger().send("Found key!", LogLevel.DEBUG);

                        if (section.toString().isEmpty())
                            section.append("main");

                        int start;
                        int end;
                        try {
                            start = matcher.start();
                            end = matcher.end();
                        } catch (Throwable ex) {
                            start = badMatcher.start();
                            end = badMatcher.end();
                        }

                        String space = line.substring(0, start);
                        String result = line.substring(start, end);

                        source.logger().send("The path is: {0} ( From line: {1} )", LogLevel.DEBUG, result, line);

                        boolean recursive = line.endsWith("<->");
                        String name = result.substring(1, result.length() - (recursive ? 5 : 4));

                        source.logger().send("Key name is: {0}", LogLevel.DEBUG, name);

                        String key = section + "." + name;
                        String value = line.replaceFirst(line.substring(0, start) + result + " ", "");

                        Element<?> element = content.getOrDefault(key, KarmaPrimitive.forNull());
                        if (element != null) {
                            recursive = isRecursive(key);
                        }

                        if (element == null && internal != null) {
                            KarmaMain tmp = new KarmaMain(internal);
                            element = tmp.content.getOrDefault(key, KarmaPrimitive.forNull());
                        }

                        if (element != null) {
                            wrote_keys.add(key);

                            source.logger().send("Key {0} has a known value: {1}", LogLevel.DEBUG, key, element);
                            if (element.isPrimitive() && !readingList && !readingMap) {
                                source.logger().send("Wrote!", LogLevel.DEBUG);
                                ElementPrimitive primitive = element.getAsPrimitive();
                                if (primitive.isString() || primitive.isCharacter()) {
                                    write.add(space + "'" + name + "' " + (recursive ? "<-> '" : "-> '") + element + "'");
                                } else {
                                    write.add(space + "'" + name + "' " + (recursive ? "<-> " : "-> ") + element);
                                }
                            } else {
                                if (!readingList && !readingMap) {
                                    if (value.startsWith("{")) {
                                        if (element.isMap()) {
                                            readingMap = true;
                                            write.add(space + "'" + name + "' " + (recursive ? "<-> " : "-> ") + "[");

                                            source.logger().send("Writing map", LogLevel.DEBUG);

                                            ElementMap<ElementPrimitive> kA = (KarmaMap) element;
                                            kA.forEachKey((k) -> {
                                                ElementPrimitive primitive = kA.get(k);
                                                boolean rec = kA.isRecursive(k) || kA.isRecursive(primitive);

                                                if (primitive.isString() || primitive.isCharacter()) {
                                                    write.add(space + "\t'" + k + "' " + (rec ? "<-> '" : "-> '") + primitive + "'");
                                                } else {
                                                    write.add(space + "\t'" + k + "' " + (rec ? "<-> " : "-> ") + primitive);
                                                }
                                            });
                                        } else {
                                            readingList = true;
                                            write.add(space + "'" + name + "' " + (recursive ? "<-> " : "-> ") + "{");
                                            source.logger().send("Writing list", LogLevel.DEBUG);

                                            ElementArray<ElementPrimitive> a = (KarmaArray) element;
                                            for (ElementPrimitive sub : a) {
                                                if (sub.isString() || sub.isCharacter()) {
                                                    write.add(space + "\t'" + sub + "'");
                                                } else {
                                                    write.add(space + "\t" + sub);
                                                }
                                            }
                                        }
                                    } else{
                                        if (value.startsWith("[")) {
                                            readingMap = true;
                                            write.add(space + "'" + name + "' " + (recursive ? "<-> " : "-> ") + "[");

                                            source.logger().send("Writing map", LogLevel.DEBUG);

                                            ElementMap<ElementPrimitive> kA = (KarmaMap) element;
                                            kA.forEachKey((k) -> {
                                                ElementPrimitive primitive = kA.get(k);
                                                boolean rec = kA.isRecursive(k) || kA.isRecursive(primitive);

                                                if (primitive.isString() || primitive.isCharacter()) {
                                                    write.add(space + "\t'" + k + "' " + (rec ? "<-> '" : "-> '") + primitive + "'");
                                                } else {
                                                    write.add(space + "\t'" + k + "' " + (rec ? "<-> " : "-> ") + primitive);
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    if (line.endsWith("}")) {
                                        readingList = false;
                                        if (readingMap) {
                                            readingMap = false;
                                            write.add(line.replace("}", "]"));
                                        } else {
                                            write.add(line);
                                        }
                                    } else {
                                        if (line.endsWith("]")) {
                                            readingMap = false;
                                            write.add(line);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (!readingList && !readingMap) {
                                source.logger().log(LogLevel.DEBUG,
                                        "An error occurred while saving file {0}. Required key {1} is not defined{2}. The file will be try to be saved anyway",
                                        PathUtilities.pathString(document),
                                        key,
                                        (internal == null ? " ( setting internal file may fix the issue )" : " ( internal file does not contain the key neither )"));
                            }
                        }
                    } else {
                        if (readingList) {
                            if (line.endsWith("}")) {
                                readingList = false;
                                write.add(line);
                            }
                        } else {
                            if (readingMap) {
                                if (line.endsWith("]") || line.endsWith("}")) {
                                    readingMap = false;
                                    write.add(line.replace("}", "]"));
                                }
                            } else {
                                Pattern sectMatcher = Pattern.compile("\\(\".*\"");

                                matcher = sectMatcher.matcher(line);

                                if (line.replaceAll("\\s", "").equals(")") && !section.toString().equals("main")) {
                                    write.add(line);

                                    String current = section.toString();
                                    String[] data = current.split("\\.");
                                    if (data.length >= 1)
                                        current = current.replace("." + data[data.length - 1], "");

                                    section = new StringBuilder(current);
                                    continue;
                                }

                                if (matcher.find()) {
                                    int start = matcher.start();
                                    int end = matcher.end();

                                    if (line.contains("\"")) {
                                        String name = line.substring(start + 2, end - 1);

                                        if (section.length() > 0) {
                                            section.append(".").append(name);
                                        } else {
                                            section = new StringBuilder(name);
                                        }

                                        if (!name.equalsIgnoreCase("main")) {
                                            write.add(line);
                                        }
                                    } else {
                                        if (!section.toString().equals("main")) {
                                            section = new StringBuilder("main");
                                        } else {
                                            throw new KarmaFormatException(document, "Error, couldn't save file because the main section has been defined two or more times", index);
                                        }
                                    }
                                } else {
                                    if (!bigComment) {
                                        Pattern comment = Pattern.compile("(\\*\\(.[^)*]*\\)\\*)|(\\*\\(\\n[^)*]*\\)\\*)|(\\*/.[^\\n]*)");
                                        matcher = comment.matcher(line);

                                        if (matcher.find()) {
                                            write.add(line);
                                        } else {
                                            bigComment = line.replaceAll("\\s", "").startsWith("*(");
                                            if (bigComment) {
                                                write.add(line);
                                            }
                                        }
                                    } else {
                                        write.add(line);
                                        bigComment = !line.endsWith(")*");
                                    }
                                }
                            }
                        }
                    }
                }

                Map<String, Map<String, Element<?>>> sections = new LinkedHashMap<>();
                for (String key : content.keySet()) {
                    if (key.contains(".")) {
                        String[] data = key.split("\\.");
                        if (data.length > 2) {
                            Map<String, Element<?>> values = sections.getOrDefault(key, new LinkedHashMap<>());
                            values.put(key, content.get(key));

                            sections.put(key, values);
                            source.logger().send("Adding section {0}", LogLevel.DEBUG, key);
                        } else {
                            Map<String, Element<?>> values = sections.getOrDefault("main", new LinkedHashMap<>());
                            values.put(data[1], content.get(key));

                            sections.put("main", values);
                            source.logger().send("Adding section {0}", LogLevel.DEBUG, "main");
                        }
                    }
                }

                int i2;
                int sectionIndex = 0;
                for (String s : sections.keySet()) {
                    boolean wrote = false;
                    sectionIndex++;

                    if (!s.equals("main")) {
                        if (!wrote_keys.contains(s)) {
                            wrote = true;

                            if (s.contains(".")) {
                                String[] data = s.split("\\.");

                                i2 = 1;
                                StringBuilder realKeyBuilder = new StringBuilder("main");
                                for (String sub : data) {
                                    if (!sub.equals("main")) {
                                        realKeyBuilder.append(".").append(sub);

                                        StringBuilder b = new StringBuilder();
                                        for (int i = 0; i < i2; i++)
                                            b.append("\t");

                                        write.add(b + "(\"" + sub + "\"");
                                        Map<String, Element<?>> values = sections.getOrDefault(realKeyBuilder.toString(), new LinkedHashMap<>());

                                        source.logger().send("Section: {0} ({1})", LogLevel.DEBUG, sub, realKeyBuilder);

                                        if (!values.isEmpty()) {
                                            for (String key : values.keySet()) {
                                                Element<?> value = values.get(key);
                                                if (value.isArray()) {
                                                    source.logger().send("Writing list {0}", LogLevel.DEBUG, key);

                                                    write.add(b + "\t'" + key + "' -> {");
                                                    ElementArray<ElementPrimitive> array = (KarmaArray) value;

                                                    array.forEach((element) -> {
                                                        if (element.isString() || element.isCharacter()) {
                                                            write.add(b + "\t\t'" + element + "'");
                                                        } else {
                                                            write.add(b + "\t\t" + element);
                                                        }
                                                    });
                                                    write.add(b + "\t}");
                                                } else {
                                                    if (value.isMap()) {
                                                        source.logger().send("Writing map {0}", LogLevel.DEBUG, key);

                                                        write.add(b + "\t'" + key + "' -> [");
                                                        ElementMap<ElementPrimitive> array = (KarmaMap) value;

                                                        array.forEachKey((k) -> {
                                                            ElementPrimitive val = array.get(k + " ( " + key + " )");
                                                            if (val.isString() || val.isCharacter()) {
                                                                write.add(b + "\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " '" + val + "'");
                                                            } else {
                                                                write.add(b + "\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " " + val);
                                                            }
                                                        });
                                                        write.add(b + "\t]");
                                                    } else {
                                                        source.logger().send("Writing key {0} with value: {1}", LogLevel.DEBUG, key, values.get(key));

                                                        Element<?> element = values.get(key);
                                                        if (element.isPrimitive()) {
                                                            ElementPrimitive primitive = element.getAsPrimitive();
                                                            if (primitive.isString() || primitive.isCharacter()) {
                                                                write.add(b + "\t'" + key + "' -> '" + primitive + "'");
                                                            } else {
                                                                write.add(b + "\t'" + key + "' -> " + primitive);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        write.add(b + ")");
                                        i2++;
                                    }
                                }
                            } else {
                                source.logger().send("Section: {0}", LogLevel.DEBUG, s);

                                write.add("\t(\"" + s + "\"");
                                Map<String, Element<?>> values = sections.getOrDefault(s, new LinkedHashMap<>());

                                for (String key : values.keySet()) {
                                    Element<?> value = values.get(key);

                                    if (value.isArray()) {
                                        source.logger().send("Writing list {0}", LogLevel.DEBUG, key);

                                        write.add("\t\t'" + key + "' -> {");
                                        ElementArray<ElementPrimitive> array = (KarmaArray) value;

                                        array.forEach((element) -> {
                                            if (element.isString() || element.isCharacter()) {
                                                write.add("\t\t\t'" + element + "'");
                                            } else {
                                                write.add("\t\t\t" + element);
                                            }
                                        });
                                        write.add("\t\t}");
                                    } else {
                                        if (value.isMap()) {
                                            source.logger().send("Writing map {0}", LogLevel.DEBUG, key);

                                            write.add("\t\t'" + key + "' -> [");
                                            ElementMap<ElementPrimitive> array = (KarmaMap) value;

                                            array.forEachKey((k) -> {
                                                ElementPrimitive val = array.get(k);
                                                if (val.isString() || val.isCharacter()) {
                                                    write.add("\t\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " '" + val + "'");
                                                } else {
                                                    write.add("\t\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " " + val);
                                                }
                                            });
                                            write.add("\t\t]");
                                        } else {
                                            source.logger().send("Writing key {0} with value: {1}", LogLevel.DEBUG, key, values.get(key));

                                            Element<?> element = values.get(key);
                                            if (element.isPrimitive()) {
                                                ElementPrimitive primitive = element.getAsPrimitive();
                                                if (primitive.isString() || primitive.isCharacter()) {
                                                    write.add("\t\t'" + key + "' -> '" + primitive + "'");
                                                } else {
                                                    write.add("\t\t'" + key + "' -> " + primitive);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        source.logger().send("Section: main", LogLevel.DEBUG);

                        Map<String, Element<?>> values = sections.getOrDefault(s, new LinkedHashMap<>());

                        for (String key : values.keySet()) {
                            if (!wrote_keys.contains("main." + key)) {
                                wrote = true;

                                write.add("");
                                Element<?> value = values.get(key);

                                if (value.isArray()) {
                                    source.logger().send("Writing list {0}", LogLevel.DEBUG, key);

                                    write.add("\t'" + key + "' -> {");
                                    ElementArray<ElementPrimitive> array = (KarmaArray) value;

                                    array.forEach((element) -> {
                                        if (element.isString() || element.isCharacter()) {
                                            write.add("\t\t'" + element + "'");
                                        } else {
                                            write.add("\t\t" + element);
                                        }
                                    });
                                    write.add("\t}");
                                } else {
                                    if (value.isMap()) {
                                        source.logger().send("Writing map {0}", LogLevel.DEBUG, key);

                                        write.add("\t'" + key + "' -> [");
                                        ElementMap<ElementPrimitive> array = (KarmaMap) value;

                                        array.forEachKey((k) -> {
                                            ElementPrimitive val = array.get(k);
                                            if (val.isString() || val.isCharacter()) {
                                                write.add("\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " '" + val + "'");
                                            } else {
                                                write.add("\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " " + val);
                                            }
                                        });
                                        write.add("\t]");
                                    } else {
                                        source.logger().send("Writing key {0} with value: {1}", LogLevel.DEBUG, key, values.get(key));

                                        Element<?> element = values.get(key);
                                        if (element.isPrimitive()) {
                                            ElementPrimitive primitive = element.getAsPrimitive();
                                            if (primitive.isString() || primitive.isCharacter()) {
                                                write.add("\t'" + key + "' -> '" + primitive + "'");
                                            } else {
                                                write.add("\t'" + key + "' -> " + primitive);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (wrote) {
                        if (sectionIndex != sections.size()) {
                            write.add("");
                        }
                    }
                }

                write.add(")");

                String firstLine = write.get(0);
                if (!firstLine.equals("(") && !firstLine.equals("(\"main\"")) {
                    write.add(0, "(\"main\"");
                }
            } else {
                source.logger().send("File is empty. Storing only set paths", LogLevel.DEBUG);

                //Basically the file is new and we must set the values
                Map<String, Map<String, Element<?>>> sections = new LinkedHashMap<>();
                for (String key : content.keySet()) {
                    if (key.contains(".")) {
                        String[] data = key.split("\\.");
                        if (data.length > 2) {
                            String realKey = data[data.length - 1];
                            String realPath = key.replace("." + realKey, "");

                            Map<String, Element<?>> values = sections.getOrDefault(realPath, new LinkedHashMap<>());
                            values.put(realKey, content.get(key));

                            sections.put(realPath, values);
                        } else {
                            Map<String, Element<?>> values = sections.getOrDefault("main", new LinkedHashMap<>());
                            values.put(data[1], content.get(key));

                            sections.put("main", values);
                        }
                    }
                }

                int index;
                int sectionIndex = 0;
                for (String section : sections.keySet()) {
                    sectionIndex++;

                    if (!section.equals("main")) {
                        if (section.contains(".")) {
                            String[] data = section.split("\\.");

                            index = 1;
                            StringBuilder realKeyBuilder = new StringBuilder("main");
                            for (String sub : data) {
                                if (!sub.equals("main")) {
                                    realKeyBuilder.append(".").append(sub);

                                    StringBuilder b = new StringBuilder();
                                    for (int i = 0; i < index; i++)
                                        b.append("\t");

                                    write.add(b + "(\"" + sub + "\"");
                                    Map<String, Element<?>> values = sections.getOrDefault(realKeyBuilder.toString(), new LinkedHashMap<>());

                                    source.logger().send("Section: {0}", LogLevel.DEBUG, sub);

                                    for (String key : values.keySet()) {
                                        Element<?> value = values.get(key);
                                        if (value.isArray()) {
                                            source.logger().send("Writing list {0}", LogLevel.DEBUG, key);

                                            write.add(b + "\t'" + key + "' -> {");
                                            ElementArray<ElementPrimitive> array = (KarmaArray) value;

                                            array.forEach((element) -> {
                                                if (element.isString() || element.isCharacter()) {
                                                    write.add(b + "\t\t'" + element + "'");
                                                } else {
                                                    write.add(b + "\t\t" + element);
                                                }
                                            });
                                            write.add(b + "\t}");
                                        } else {
                                            if (value.isMap()) {
                                                source.logger().send("Writing map {0}", LogLevel.DEBUG, key);

                                                write.add(b + "\t'" + key + "' -> [");
                                                ElementMap<ElementPrimitive> array = (KarmaMap) value;

                                                array.forEachKey((k) -> {
                                                    ElementPrimitive val = array.get(k);
                                                    if (val.isString() || val.isCharacter()) {
                                                        write.add(b + "\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " '" + val + "'");
                                                    } else {
                                                        write.add(b + "\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " " + val);
                                                    }
                                                });
                                                write.add(b + "\t]");
                                            } else {
                                                source.logger().send("Writing key {0} with value: {1}", LogLevel.DEBUG, key, values.get(key));

                                                Element<?> element = values.get(key);
                                                if (element.isPrimitive()) {
                                                    ElementPrimitive primitive = element.getAsPrimitive();
                                                    if (primitive.isString() || primitive.isCharacter()) {
                                                        write.add(b + "\t'" + key + "' -> '" + primitive + "'");
                                                    } else {
                                                        write.add(b + "\t'" + key + "' -> " + primitive);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    write.add(b + ")");

                                    index++;
                                }
                            }
                        } else {
                            source.logger().send("Section: {0}", LogLevel.DEBUG, section);

                            write.add("\t(\"" + section + "\"");
                            Map<String, Element<?>> values = sections.getOrDefault(section, new LinkedHashMap<>());

                            for (String key : values.keySet()) {
                                Element<?> value = values.get(key);

                                if (value.isArray()) {
                                    source.logger().send("Writing list {0}", LogLevel.DEBUG, key);

                                    write.add("\t\t'" + key + "' -> {");
                                    ElementArray<ElementPrimitive> array = (KarmaArray) value;

                                    array.forEach((element) -> {
                                        if (element.isString() || element.isCharacter()) {
                                            write.add("\t\t\t'" + element + "'");
                                        } else {
                                            write.add("\t\t\t" + element);
                                        }
                                    });
                                    write.add("\t\t}");
                                } else {
                                    if (value.isMap()) {
                                        source.logger().send("Writing map {0}", LogLevel.DEBUG, key);

                                        write.add("\t\t'" + key + "' -> [");
                                        ElementMap<ElementPrimitive> array = (KarmaMap) value;

                                        array.forEachKey((k) -> {
                                            ElementPrimitive val = array.get(k);
                                            if (val.isString() || val.isCharacter()) {
                                                write.add("\t\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " '" + val + "'");
                                            } else {
                                                write.add("\t\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " " + val);
                                            }
                                        });
                                        write.add("\t\t]");
                                    } else {
                                        source.logger().send("Writing key {0} with value: {1}", LogLevel.DEBUG, key, values.get(key));

                                        Element<?> element = values.get(key);
                                        if (element.isPrimitive()) {
                                            ElementPrimitive primitive = element.getAsPrimitive();
                                            if (primitive.isString() || primitive.isCharacter()) {
                                                write.add("\t\t'" + key + "' -> '" + primitive + "'");
                                            } else {
                                                write.add("\t\t'" + key + "' -> " + primitive);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        source.logger().send("Section: main", LogLevel.DEBUG);

                        write.add("(\"main\"");
                        Map<String, Element<?>> values = sections.getOrDefault(section, new LinkedHashMap<>());

                        for (String key : values.keySet()) {
                            write.add("");
                            Element<?> value = values.get(key);

                            if (value.isArray()) {
                                source.logger().send("Writing list {0}", LogLevel.DEBUG, key);

                                write.add("\t'" + key + "' -> {");
                                ElementArray<ElementPrimitive> array = (KarmaArray) value;

                                array.forEach((element) -> {
                                    if (element.isString() || element.isCharacter()) {
                                        write.add("\t\t'" + element + "'");
                                    } else {
                                        write.add("\t\t" + element);
                                    }
                                });
                                write.add("\t}");
                            } else {
                                if (value.isMap()) {
                                    source.logger().send("Writing map {0}", LogLevel.DEBUG, key);

                                    write.add("\t'" + key + "' -> [");
                                    ElementMap<ElementPrimitive> array = (KarmaMap) value;

                                    array.forEachKey((k) -> {
                                        ElementPrimitive val = array.get(k);
                                        if (val.isString() || val.isCharacter()) {
                                            write.add("\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " '" + val + "'");
                                        } else {
                                            write.add("\t\t'" + k + "' " + (array.isRecursive(k) ? "<->" : "->") + " " + val);
                                        }
                                    });
                                    write.add("\t]");
                                } else {
                                    source.logger().send("Writing key {0} with value: {1}", LogLevel.DEBUG, key, values.get(key));

                                    Element<?> element = values.get(key);
                                    if (element.isPrimitive()) {
                                        ElementPrimitive primitive = element.getAsPrimitive();
                                        if (primitive.isString() || primitive.isCharacter()) {
                                            write.add("\t'" + key + "' -> '" + primitive + "'");
                                        } else {
                                            write.add("\t'" + key + "' -> " + primitive);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (sectionIndex != sections.size()) {
                        write.add("");
                    }
                }

                write.add(")");

                String firstLine = write.get(0);
                if (!firstLine.equals("(") && !firstLine.equals("(\"main\"")) {
                    write.add(0, "(\"main\"");
                }
            }

            PathUtilities.createPath(target);
            Files.write(target, StringUtils.listToString(write, ListSpacer.NEW_LINE).getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Get the document
     *
     * @return the file document
     */
    public Path getDocument() {
        return document;
    }

    /**
     * Validate the file
     *
     * @throws IOException if something goes wrong
     */
    public void validate() throws IOException {
        if (internal != null) {
            if (!exists()) {
                create();
                Files.copy(internal, document, StandardCopyOption.REPLACE_EXISTING);
            }

            if (raw.isEmpty()) {
                Files.copy(internal, document, StandardCopyOption.REPLACE_EXISTING);
            } else {
                clearCache();
                preCache(); //We must update the cache

                KarmaMain tmp = new KarmaMain(internal);

                List<String> write = new ArrayList<>();
                write.add("(\"main\"");

                List<String> lines = PathUtilities.readAllLines(tmp.document);

                Pattern keyMatcher = Pattern.compile("'.*' .?->");
                Pattern badKeyMatcher = Pattern.compile("\".*\" .?->");

                StringBuilder section = new StringBuilder();

                int index = 1;
                boolean bigComment = false;
                boolean readingList = false;
                boolean readingMap = false;
                for (String line : lines) {
                    String noSpace = line.replaceAll("\\s", "");
                    if (noSpace.equals("\n") || ObjectUtils.isNullOrEmpty(noSpace)) {
                        write.add(line);
                        continue;
                    }

                    Matcher matcher = keyMatcher.matcher(line);
                    Matcher badMatcher = badKeyMatcher.matcher(line);

                    if (matcher.find() || badMatcher.find()) {
                        if (section.toString().isEmpty())
                            section.append("main");

                        int start;
                        int end;
                        try {
                            start = matcher.start();
                            end = matcher.end();
                        } catch (Throwable ex) {
                            start = badMatcher.start();
                            end = badMatcher.end();
                        }

                        String space = line.substring(0, start);
                        String result = line.substring(start, end);
                        boolean recursive = line.endsWith("<->");
                        String name = result.substring(1, result.length() - (recursive ? 5 : 4));

                        String key = section + "." + name;
                        String value = line.replaceFirst(line.substring(0, start) + result + " ", "");

                        Element<?> element = content.getOrDefault(key, KarmaPrimitive.forNull());
                        if (element == null) {
                            element = tmp.get(key, null);
                        } else {
                            recursive = isRecursive(key);

                            Element<?> original = tmp.get(key, null);
                            if (original.isPrimitive()) {
                                if (!element.isPrimitive()) {
                                    element = original;
                                } else {
                                    ElementPrimitive originalPrimitive = (ElementPrimitive) original;
                                    ElementPrimitive elementPrimitive = element.getAsPrimitive();

                                    if (!originalPrimitive.isElementNull()) {
                                        if (!originalPrimitive.getValue().type().equals(elementPrimitive.getValue().type())) {
                                            element = original;
                                        }
                                    }
                                }
                            } else {
                                if (original.isArray() && !element.isArray() ||
                                        original.isMap() && !element.isMap()) {
                                    element = original;
                                }
                            }
                        }

                        if (element != null) {
                            if (element.isPrimitive() && !readingList && !readingMap) {
                                ElementPrimitive primitive = element.getAsPrimitive();

                                String val = element.toString();
                                if (val.equals("true") || val.equals("false")) {
                                    if (!primitive.isBoolean())
                                        element = new KarmaPrimitive(Boolean.parseBoolean(val));
                                } else {
                                    Number num = parseNumber(val);
                                    if (num != null && !primitive.isNumber())
                                        element = new KarmaPrimitive(num);
                                }

                                if (primitive.isString() || primitive.isCharacter()) {
                                    write.add(space + "'" + name + "' " + (recursive ? "<-> '" : "-> '") + element + "'");
                                } else {
                                    write.add(space + "'" + name + "' " + (recursive ? "<-> " : "-> ") + element);
                                }
                            }

                            if (!readingList && !readingMap) {
                                if (value.startsWith("{")) {
                                    if (element.isMap()) {
                                        readingMap = true;
                                        write.add(space + "'" + name + "' " + (recursive ? "<->" : "->") + " [");

                                        ElementArray<ElementPrimitive> a = (KarmaArray) element;
                                        for (ElementPrimitive sub : a) {
                                            if (sub.isString() || sub.isCharacter()) {
                                                write.add(space + "\t'" + sub + "'");
                                            } else {
                                                write.add(space + "\t" + sub);
                                            }
                                        }
                                    } else {
                                        readingList = true;
                                        write.add(space + "'" + name + "' " + (recursive ? "<->" : "->") + " {");

                                        ElementArray<ElementPrimitive> a = (KarmaArray) element;
                                        for (ElementPrimitive sub : a) {
                                            if (sub.isString() || sub.isCharacter()) {
                                                write.add(space + "\t'" + sub + "'");
                                            } else {
                                                write.add(space + "\t" + sub);
                                            }
                                        }
                                    }
                                } else {
                                    if (value.startsWith("[")) {
                                        readingMap = true;
                                        write.add(space + "'" + name + "' " + (recursive ? "<->" : "->") + " [");

                                        ElementMap<ElementPrimitive> kA = (KarmaMap) element;
                                        kA.forEachKey((k) -> {
                                            ElementPrimitive kAElement = kA.get(k);
                                            boolean rec = kA.isRecursive(k) || kA.isRecursive(kAElement);

                                            if (kAElement.isString() || kAElement.isCharacter()) {
                                                write.add(space + "\t'" + k + "' " + (rec ? "<-> " : "-> ") + "'" + kAElement + "'");
                                            } else {
                                                write.add(space + "\t'" + k + "' " + (rec ? "<-> " : "-> ") + kAElement);
                                            }
                                        });
                                    }
                                }
                            } else {
                                if (line.endsWith("}")) {
                                    readingList = false;
                                    if (readingMap) {
                                        readingMap = false;
                                        write.add(line.replace("}", "]"));
                                    } else {
                                        write.add(line);
                                    }
                                } else {
                                    if (line.endsWith("]")) {
                                        readingMap = false;
                                        write.add(line);
                                    }
                                }
                            }
                        }
                    } else {
                        if (readingList) {
                            if (line.endsWith("}")) {
                                readingList = false;
                                write.add(line);
                            }
                        } else {
                            if (readingMap) {
                                if (line.endsWith("]") || line.endsWith("}")) {
                                    readingMap = false;
                                    write.add(line.replace("}", "]"));
                                }
                            } else {
                                Pattern sectMatcher = Pattern.compile("\\(\".*\"");

                                matcher = sectMatcher.matcher(line);

                                if (line.replaceAll("\\s", "").equals(")") && !section.toString().equals("main")) {
                                    write.add(line);

                                    String current = section.toString();
                                    String[] data = current.split("\\.");
                                    if (data.length >= 1)
                                        current = current.replace("." + data[data.length - 1], "");

                                    section = new StringBuilder(current);
                                    continue;
                                }

                                if (matcher.find()) {
                                    int start = matcher.start();
                                    int end = matcher.end();

                                    if (line.contains("\"")) {
                                        String name = line.substring(start + 2, end - 1);

                                        if (section.length() > 0) {
                                            section.append(".").append(name);
                                        } else {
                                            section = new StringBuilder(name);
                                        }

                                        if (!name.equalsIgnoreCase("main")) {
                                            write.add(line);
                                        }
                                    } else {
                                        if (!section.toString().equals("main")) {
                                            section = new StringBuilder("main");
                                        } else {
                                            throw new KarmaFormatException(document, "Error, couldn't save file because the main section has been defined two or more times", index);
                                        }
                                    }
                                } else {
                                    if (!bigComment) {
                                        Pattern comment = Pattern.compile("(\\*\\(.[^)*]*\\)\\*)|(\\*\\(\\n[^)*]*\\)\\*)|(\\*/.[^\\n]*)");
                                        matcher = comment.matcher(line);

                                        if (matcher.find()) {
                                            write.add(line);
                                        } else {
                                            bigComment = line.replaceAll("\\s", "").startsWith("*(");
                                            if (bigComment) {
                                                write.add(line);
                                            }
                                        }
                                    } else {
                                        write.add(line);
                                        bigComment = !line.endsWith(")*");
                                    }
                                }
                            }
                        }
                    }
                }
                write.add(")");

                PathUtilities.createPath(document);
                Files.write(document, StringUtils.listToString(write, ListSpacer.NEW_LINE).getBytes(StandardCharsets.UTF_8));

                clearCache();
                preCache();
            }
        }
    }

    /**
     * Clear raw cache
     * <p>
     * ONLY RECOMMENDED DOING WHEN RELOADING THE FILE
     */
    public void clearCache() {
        raw = "";

        content.clear();
        reverse.clear();
        indexes.clear();
    }

    /**
     * Delete the document
     */
    public void delete() {
        PathUtilities.destroy(document);
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return raw;
    }

    /**
     * Parse a string into a number
     *
     * @param string the string
     * @return the number or null if not a number
     */
    private Number parseNumber(final String string) {
        Number number = null;
        try {
            number = Double.parseDouble(string);
        } catch (NumberFormatException db) {
            try {
                number = Float.parseFloat(string);
            } catch (NumberFormatException fl) {
                try {
                    number = Long.parseLong(string);
                } catch (NumberFormatException lo) {
                    try {
                        number = Short.parseShort(string);
                    } catch (NumberFormatException sh) {
                        try {
                            number = Byte.parseByte(string);
                        } catch (NumberFormatException by) {
                            try {
                                number = Integer.parseInt(string);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
        }

        return number;
    }

    /**
     * Get KarmaAPI configuration
     *
     * @return the KarmaAPI configuration
     */
    public static KarmaMain getConfiguration() {
        KarmaMain main = new KarmaMain(KarmaKore.INSTANCE(), "config.kf")
                .internal(KarmaMain.class.getResourceAsStream("/config.kf"));

        try {
            main.validate();
        } catch (Throwable ignored) {
        }

        return main;
    }

    /**
     * Migrate the specified file from
     * KarmaMain to Json
     *
     * @param source the source file
     * @param destination the destination file
     * @return if the file was able to be migrated
     */
    public static boolean migrateJson(final Path source, final Path destination) {
        if (Files.exists(source)) {
            PathUtilities.createPath(destination);
            KarmaMain main = new KarmaMain(source);

            JsonObject mainObject = new JsonObject();
            JsonObject modObject = mainObject;
            JsonObject previousObject = modObject;
            Map<String, JsonObject> paths = new LinkedHashMap<>();
            for (String key : main.getKeys()) {
                key = key.substring(5);
                if (key.contains(".")) {
                    String[] data = key.split("\\.");
                    String vKey = key;
                    String current = key;
                    for (int i = 1; i < data.length; i++) {
                        String prev = data[i - 1];
                        String nav = data[i];
                        vKey = StringUtils.splitAt(key, "." + nav);
                        current = nav;
                        previousObject = modObject;
                        modObject = paths.computeIfAbsent(vKey, (object) -> new JsonObject());
                        previousObject.add(prev, modObject);
                    }

                    Element<?> value = main.get(key);
                    if (value.isPrimitive()) {
                        ElementPrimitive element = value.getAsPrimitive();
                        if (element.isNumber()) {
                            modObject.addProperty(current, (Number) element.getValue().get());
                        }
                        if (element.isBoolean()) {
                            modObject.addProperty(current, (Boolean) element.getValue().get());
                        }
                        if (element.isCharacter()) {
                            modObject.addProperty(current, (Character) element.getValue().get());
                        } else {
                            if (element.isString()) {
                                modObject.addProperty(current, (String) element.getValue().get());
                            }
                        }
                    }
                    if (value.isArray()) {
                        KarmaArray array = (KarmaArray) value.getAsArray();
                        JsonArray jsonArray = new JsonArray();

                        array.forEach((element) -> {
                            if (element.isNumber()) {
                                jsonArray.add((Number) element.getValue().get());
                            }
                            if (element.isBoolean()) {
                                jsonArray.add((Boolean) element.getValue().get());
                            }
                            if (element.isCharacter()) {
                                jsonArray.add((Character) element.getValue().get());
                            } else {
                                if (element.isString()) {
                                    jsonArray.add((String) element.getValue().get());
                                }
                            }
                        });
                        modObject.add(current, jsonArray);
                    }
                    if (value.isMap()) {
                        KarmaMap map = (KarmaMap) value.getAsMap();
                        JsonObject jsonObject = new JsonObject();

                        map.forEachKey((mapKey) -> {
                            ElementPrimitive element = map.get(mapKey);
                            if (element.isNumber()) {
                                jsonObject.addProperty(mapKey, (Number) element.getValue().get());
                            }
                            if (element.isBoolean()) {
                                jsonObject.addProperty(mapKey, (Boolean) element.getValue().get());
                            }
                            if (element.isCharacter()) {
                                jsonObject.addProperty(mapKey, (Character) element.getValue().get());
                            } else {
                                if (element.isString()) {
                                    jsonObject.addProperty(mapKey, (String) element.getValue().get());
                                }
                            }
                        });

                        modObject.add(current, jsonObject);
                    }
                    if (value.isElementNull()) {
                        modObject.add(current, JsonNull.INSTANCE);
                    }
                } else {
                    modObject = mainObject;

                    Element<?> value = main.get(key);
                    if (value.isPrimitive()) {
                        ElementPrimitive element = value.getAsPrimitive();
                        if (element.isNumber()) {
                            modObject.addProperty(key, (Number) element.getValue().get());
                        }
                        if (element.isBoolean()) {
                            modObject.addProperty(key, (Boolean) element.getValue().get());
                        }
                        if (element.isCharacter()) {
                            modObject.addProperty(key, (Character) element.getValue().get());
                        } else {
                            if (element.isString()) {
                                modObject.addProperty(key, (String) element.getValue().get());
                            }
                        }
                    }
                    if (value.isArray()) {
                        KarmaArray array = (KarmaArray) value.getAsArray();
                        JsonArray jsonArray = new JsonArray();

                        array.forEach((element) -> {
                            if (element.isNumber()) {
                                jsonArray.add((Number) element.getValue().get());
                            }
                            if (element.isBoolean()) {
                                jsonArray.add((Boolean) element.getValue().get());
                            }
                            if (element.isCharacter()) {
                                jsonArray.add((Character) element.getValue().get());
                            } else {
                                if (element.isString()) {
                                    jsonArray.add((String) element.getValue().get());
                                }
                            }
                        });
                        modObject.add(key, jsonArray);
                    }
                    if (value.isMap()) {
                        KarmaMap map = (KarmaMap) value.getAsMap();
                        JsonObject jsonObject = new JsonObject();

                        map.forEachKey((mapKey) -> {
                            ElementPrimitive element = map.get(mapKey);
                            if (element.isNumber()) {
                                jsonObject.addProperty(mapKey, (Number) element.getValue().get());
                            }
                            if (element.isBoolean()) {
                                jsonObject.addProperty(mapKey, (Boolean) element.getValue().get());
                            }
                            if (element.isCharacter()) {
                                jsonObject.addProperty(mapKey, (Character) element.getValue().get());
                            } else {
                                if (element.isString()) {
                                    jsonObject.addProperty(mapKey, (String) element.getValue().get());
                                }
                            }
                        });

                        modObject.add(key, jsonObject);
                    }
                    if (value.isElementNull()) {
                        modObject.add(key, JsonNull.INSTANCE);
                    }
                }
            }

            Gson gson;
            try {
                gson = new GsonBuilder().setPrettyPrinting().create();
            } catch (Throwable ex) {
                gson = new GsonBuilder().create();
            }

            String raw = gson.toJson(mainObject);
            return PathUtilities.write(destination, raw);
        }

        return false;
    }
}