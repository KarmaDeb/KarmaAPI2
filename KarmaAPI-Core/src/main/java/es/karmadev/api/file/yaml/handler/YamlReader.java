package es.karmadev.api.file.yaml.handler;

import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.file.yaml.YamlFileHandler;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle;

/**
 * Karma YAML reader, using snakeyaml
 */
public class YamlReader {

    private final byte[] raw;
    private final Map<String, Tag> tags = new ConcurrentHashMap<>();

    /**
     * Create a new yaml reader
     *
     * @param stream the stream to read from
     * @throws IOException if the stream fails to read
     */
    public YamlReader(final InputStream stream) throws IOException {
        raw = StreamUtils.streamToString(stream).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Create a yaml handler for this
     * yaml reader
     *
     * @return the yaml handler
     * @throws IOException if the yaml fails to read
     */
    public YamlFileHandler toHandler() throws IOException {
        Map<String, Object> mapped = map();
        return new SimpleYamlHandler(mapped);
    }

    /**
     * Create a yaml source from a resource file
     *
     * @param placeholders replace the values with the key placeholder
     * @return the parsed yaml data
     * @throws IOException if there's some problem while loading
     * the yaml or closing the original stream
     */
    public String parse(final boolean placeholders) throws IOException {
        InputStream resource = StreamUtils.create(raw);
        try (InputStreamReader isr = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            LoaderOptions options = new LoaderOptions();
            options.setProcessComments(true);

            StringBuilder rawBuilder = new StringBuilder();

            Yaml yaml = new Yaml(options);
            MappingNode data = (MappingNode) yaml.compose(isr);
            List<CommentLine> blockComments = data.getBlockComments();
            if (blockComments != null) {
                blockComments.forEach((comment) -> {
                    rawBuilder.append(comment.getValue()).append("\n");
                });
            }

            List<NodeTuple> tuples = data.getValue();
            for (NodeTuple tuple : tuples) {
                ScalarNode key = (ScalarNode) tuple.getKeyNode();

                rawBuilder.append(getComments("", key));

                Node value = tuple.getValueNode();
                if (value instanceof MappingNode) {
                    rawBuilder.append(key.getValue()).append(":").append(getInlineComments(key)).append("\n");

                    MappingNode map = (MappingNode) value;
                    rawBuilder.append(mapString((placeholders ? key.getValue() + "." : null), 1, map, null));
                } else {
                    if (value instanceof SequenceNode) {
                        SequenceNode sequence = (SequenceNode) value;
                        if (placeholders) {
                            rawBuilder.append(key.getValue()).append(": ${").append(key.getValue()).append("}\n");
                        } else {
                            rawBuilder.append(mapStringSequence(null, 0, key, sequence, null));
                        }
                    } else {
                        ScalarNode scalarValue = (ScalarNode) value;
                        String stringValue = scalarValue.getValue();
                        Tag tag = scalarValue.getTag();
                        tags.put(key.getValue(), tag);
                        String t = tag.getClassName();
                        if (t.equals("null") || t.equals("str")) {
                            if (stringValue.contains("'")) {
                                stringValue = "\"" + stringValue + "\"";
                            } else {
                                stringValue = "'" + stringValue + "'";
                            }
                        }

                        if (placeholders) {
                            rawBuilder.append(key.getValue()).append(": ${").append(key.getValue()).append("}").append(getInlineComments(value)).append("\n");
                        } else {
                            rawBuilder.append(key.getValue()).append(": ").append(stringValue).append(getInlineComments(value)).append("\n");
                        }
                    }
                }
            }

            return rawBuilder.toString();
        }
    }

    /**
     * Map the yaml file
     *
     * @return the mapped data
     * @throws IOException if the stream is not open
     */
    public Map<String, Object> map() throws IOException {
        String string = parse(false);
        Yaml yaml = new Yaml();

        return yaml.load(string);
    }

    /**
     * Dump the yaml file
     *
     * @param file the file
     * @return the dumped string
     * @throws IOException if the reader fails to read
     * the yaml
     */
    public String dump(final YamlFileHandler file) throws IOException {
        /*YamlFileHandler defaults = file.defaults();
        if (defaults == null) {
            DumperOptions options = new DumperOptions();
            options.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setAllowUnicode(true);
            options.setProcessComments(true);
            options.setIndent(2);

            Yaml yaml = new Yaml(options);
            return yaml.dump(file.raw());
        }*/

        InputStream resource = StreamUtils.create(raw);
        try (InputStreamReader isr = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            LoaderOptions options = new LoaderOptions();
            options.setProcessComments(true);

            StringBuilder rawBuilder = new StringBuilder();

            Yaml yaml = new Yaml(options);
            MappingNode data = (MappingNode) yaml.compose(isr);
            List<CommentLine> blockComments = data.getBlockComments();
            if (blockComments != null) {
                blockComments.forEach((comment) -> {
                    rawBuilder.append(comment.getValue()).append("\n");
                });
            }

            List<NodeTuple> tuples = data.getValue();
            for (NodeTuple tuple : tuples) {
                ScalarNode key = (ScalarNode) tuple.getKeyNode();
                rawBuilder.append(getComments("", key));

                Node value = tuple.getValueNode();
                if (value instanceof MappingNode) {
                    rawBuilder.append(key.getValue()).append(":").append(getInlineComments(key)).append("\n");

                    MappingNode map = (MappingNode) value;
                    rawBuilder.append(mapString(key.getValue() + ".", 1, map, file));
                } else {
                    if (value instanceof SequenceNode) {
                        SequenceNode sequence = (SequenceNode) value;
                        rawBuilder.append(mapStringSequence(key.getValue() + ".", 0, key, sequence, file));
                    } else {
                        ScalarNode scalarValue = (ScalarNode) value;
                        String stringValue = String.valueOf(file.get(key.getValue()));
                        Object v = file.get(key.getValue());
                        if (v == null) stringValue = scalarValue.getValue();

                        Tag tag = scalarValue.getTag();
                        tags.put(key.getValue(), tag);
                        String t = tag.getClassName();
                        if (t.equals("null") || t.equals("str")) {
                            if (stringValue.contains("'")) {
                                stringValue = "\"" + stringValue + "\"";
                            } else {
                                stringValue = "'" + stringValue + "'";
                            }
                        }

                        rawBuilder.append(key.getValue()).append(": ").append(stringValue).append(getInlineComments(value)).append("\n");
                    }
                }
            }

            return rawBuilder.toString();
        }
    }

    /**
     * Export the yaml to the specified file
     *
     * @param target the target file
     * @throws IOException if the yaml fails to be parsed
     */
    public void export(final Path target) throws IOException {
        String raw = parse(false);
        Files.write(target, raw.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Export the yaml to the specified file
     *
     * @param target the target file
     * @throws IOException if the yaml fails to be parsed
     */
    @SuppressWarnings("all")
    public void export(final File target) throws IOException {
        if (!target.exists()) target.createNewFile();
        String raw = parse(false);
        try (FileWriter writer = new FileWriter(target)) {
            writer.write(raw);
            writer.flush();
        }
    }

    private StringBuilder mapString(final String path, final int indent, final MappingNode map, final YamlFileHandler handler) {
        StringBuilder rawBuilder = new StringBuilder();
        StringBuilder indentString = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentString.append(' ').append(' ');
        }
        String tabs = indentString.toString();

        for (NodeTuple subNode : map.getValue()) {
            ScalarNode subKey = (ScalarNode) subNode.getKeyNode();
            Node subValue = subNode.getValueNode();

            rawBuilder.append(getComments(tabs, subKey));

            if (subValue instanceof ScalarNode) {
                ScalarNode scalarSubValue = (ScalarNode) subValue;
                String value = (handler == null ? scalarSubValue.getValue() : String.valueOf(handler.get(path + subKey.getValue())));
                if (handler != null) {
                    Object v = handler.get(path);
                    if (v == null) value = scalarSubValue.getValue();
                }

                Tag tag = scalarSubValue.getTag();
                tags.put(path + subKey.getValue(), tag);
                String t = tag.getClassName();
                if (t.equals("null") || t.equals("str")) {
                    if (value.contains("'")) {
                        value = "\"" + value + "\"";
                    } else {
                        value = "'" + value + "'";
                    }
                }

                if (path == null || handler != null) {
                    rawBuilder.append(tabs).append(subKey.getValue()).append(": ").append(value).append(getInlineComments(scalarSubValue)).append("\n");
                } else {
                    rawBuilder.append(tabs).append(subKey.getValue()).append(": ${").append(path).append(subKey.getValue()).append("}").append(getInlineComments(scalarSubValue)).append("\n");
                }
            } else {
                if (subValue instanceof SequenceNode) {
                    SequenceNode sequence = (SequenceNode) subValue;
                    if (path == null || handler != null) {
                        rawBuilder.append(mapStringSequence((path != null ? path + subKey.getValue() + "." : null), indent, subKey, sequence, handler));
                    } else {
                        rawBuilder.append(tabs).append(subKey.getValue()).append(": ${").append(path).append(subKey.getValue()).append("}").append(getInlineComments(subKey)).append("\n");
                    }
                } else {
                    rawBuilder.append(tabs).append(subKey.getValue()).append(":").append(getInlineComments(subKey).append("\n"));

                    if (path == null || handler == null) {
                        rawBuilder.append(mapString((path != null ? path + subKey.getValue() + "." : null), indent + 1, (MappingNode) subValue, handler));
                    } else {
                        rawBuilder.append(mapString(path + subKey.getValue() + ".", indent + 1, (MappingNode) subValue, handler));
                    }
                }
            }
        }

        return rawBuilder;
    }

    private StringBuilder mapStringSequence(final String path, final int indent, final ScalarNode key, final SequenceNode sequence, final YamlFileHandler handler) {
        StringBuilder builder = new StringBuilder();
        StringBuilder indentString = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentString.append(' ').append(' ');
        }
        String tabs = indentString.toString();

        List<String> nodes = new ArrayList<>();
        List<Node> originalNodes = sequence.getValue();
        if (originalNodes.isEmpty() || (handler != null && handler.getList(path).isEmpty())) {
            return builder.append(tabs)
                    .append(key.getValue()).append(": ")
                    .append("[]").append(getInlineComments(sequence));
        }
        if (handler == null || handler.get(path) == null) {
            originalNodes.forEach((node) -> {
                ScalarNode scalar = (ScalarNode) node;
                String value = scalar.getValue();
                if (value.contains("'")) {
                    value = "\"" + value + "\"";
                } else {
                    value = "'" + value + "'";
                }

                nodes.add(value);
            });
        } else {
            handler.getList(path).forEach((value) -> {
                if (value.contains("'")) {
                    value = "\"" + value + "\"";
                } else {
                    value = "'" + value + "'";
                }

                nodes.add(value);
            });
        }

        StringBuilder sequenceBuilder = new StringBuilder();
        if (sequence.getFlowStyle().equals(FlowStyle.FLOW)) {
            sequenceBuilder.append("[");
            for (String value : nodes) {
                sequenceBuilder.append(value).append(", ");
            }

            builder.append(tabs)
                    .append(key.getValue()).append(": ")
                    .append(sequenceBuilder.substring(0, Math.max(0, sequenceBuilder.length() - 2)))
                    .append("]").append(getInlineComments(sequence));
        } else {
            sequenceBuilder.append("\n");
            for (String value : nodes) {
                sequenceBuilder.append(tabs).append(" - ").append(value).append("\n");
            }

            builder.append(tabs)
                    .append(key.getValue()).append(": ")
                    .append(sequenceBuilder.substring(0, Math.max(0, sequenceBuilder.length() - 1)));
        }

        return builder;
    }

    private StringBuilder getComments(final String indent, final Node node) {
        StringBuilder commentBuilder = new StringBuilder();

        List<CommentLine> comments = node.getBlockComments();
        if (comments != null) {
            for (CommentLine comment : comments) {
                String value = comment.getValue();
                if (comment.getCommentType().equals(CommentType.BLANK_LINE)) {
                    commentBuilder.append("\n");
                } else {
                    commentBuilder.append(indent).append("#").append(value).append("\n");
                }
            }
        }

        return commentBuilder;
    }

    private StringBuilder getInlineComments(final Node node) {
        StringBuilder commentBuilder = new StringBuilder();

        List<CommentLine> comments = node.getInLineComments();
        if (comments != null) {
            for (CommentLine comment : comments) {
                String value = comment.getValue();
                if (!value.replaceAll("\\s", "").isEmpty()) {
                    commentBuilder.append(" ").append("#").append(value);
                }
            }
        }

        return commentBuilder;
    }
}
