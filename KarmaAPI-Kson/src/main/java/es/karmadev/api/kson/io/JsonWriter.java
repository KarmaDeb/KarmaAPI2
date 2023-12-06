package es.karmadev.api.kson.io;

import es.karmadev.api.kson.*;
import lombok.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * Represents a json writer.
 * A writer is the responsible for
 * transforming the {@link es.karmadev.api.kson.JsonInstance json instance}
 * into readable text
 */
public final class JsonWriter {

    private final JsonInstance instance;
    private boolean prettyPrinting = false;
    private int indentation = 0;

    /**
     * Initialize the json writer
     *
     * @param instance the instance to write
     */
    public JsonWriter(final @NonNull JsonInstance instance) {
        this.instance = instance;
    }

    /**
     * Set the writer pretty print
     * support
     *
     * @param prettyPrinting the pretty print support
     */
    public void setPrettyPrinting(final boolean prettyPrinting) {
        this.prettyPrinting = prettyPrinting;
    }

    /**
     * Set the indentation level. Only works when
     * {@link #prettyPrinting pretty printing} is true
     *
     * @param indentation the new indentation level
     */
    public void setIndentation(final int indentation) {
        this.indentation = indentation;
    }

    /**
     * Write the element into the
     * writer
     *
     * @param writer the writer
     * @throws AssertionError if the instance type is unknown
     * @throws KsonException if the writer fails
     */
    public void export(final Writer writer) throws AssertionError, KsonException {
        try {
            if (instance instanceof JsonObject) {
                writeJsonObjectToWriter(writer);
            } else if (instance instanceof JsonArray) {
                writeJsonArrayToWriter(writer);
            } else if (instance instanceof JsonNative) {
                writeJsonNativeToWriter(writer);
            } else {
                throw new AssertionError("Cannot write unknown json type");
            }
        } catch (IOException ex) {
            throw new KsonException(ex);
        }
    }

    private void writeJsonObjectToWriter(final Writer writer) throws IOException {
        JsonObject object = (JsonObject) instance;
        String indentation = buildIndentation();

        writer.write("{");
        if (prettyPrinting) {
            writer.write("\n");
        }

        int index = 0;
        Collection<String> keys = object.getKeys(false);
        for (String key : keys) {
            JsonInstance instance = object.getChild(key);
            if (instance == null) {
                index++;
                continue;
            }

            String value = instance.toString(this.prettyPrinting, this.indentation + 1);
            if (this.prettyPrinting) {
                writer.write(indentation + '\t');
            }

            writer.write(String.format("\"%s\":%s%s", key, (prettyPrinting ? " " : ""), value));
            if (index++ < keys.size() - 1) {
                writer.write(",");
            }

            if (prettyPrinting) {
                writer.write("\n");
            }
        }

        writer.write(indentation);
        writer.write("}");
    }

    private void writeJsonArrayToWriter(final Writer writer) throws IOException {
        JsonArray array = (JsonArray) instance;
        String indentation = buildIndentation();

        writer.write("[");
        if (prettyPrinting) {
            writer.write("\n");
        }

        int index = 0;
        for (JsonInstance instance : array) {
            if (instance == null || instance.isNull()) continue;

            String value = instance.toString(this.prettyPrinting, this.indentation + 1);
            if (this.prettyPrinting) {
                writer.write(indentation + '\t');
            }

            writer.write(value);
            if (index++ < array.size() - 1) {
                writer.write(",");
            }

            if (prettyPrinting) {
                writer.write("\n");
            }
        }

        writer.write(indentation);
        writer.write("]");
    }

    private void writeJsonNativeToWriter(final Writer writer) throws IOException {
        JsonNative nat = (JsonNative) instance;
        if (nat.isNull()) {
            writer.write("null");
        } else if (nat.isString()) {
            writer.write(String.format("\"%s\"", nat.getString()));
        } else if (nat.isNumber()) {
            writer.write(String.valueOf(nat.getNumber()));
        } else {
            writer.write(String.valueOf(nat.getBoolean()));
        }
    }

    private String buildIndentation() {
        if (!prettyPrinting || indentation <= 0) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indentation; i++) {
            builder.append('\t');
        }

        return builder.toString();
    }
}
