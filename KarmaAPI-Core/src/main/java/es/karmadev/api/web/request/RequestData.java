package es.karmadev.api.web.request;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.file.util.PathUtilities;
import lombok.Getter;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Post request
 */
public final class RequestData {

    @Getter
    private ContentType contentType = ContentType.ENCODED;

    private final Map<String, Object> data = new LinkedHashMap<>();
    @Getter
    private final String boundary = "===" + System.currentTimeMillis() + "===";

    /**
     * Create the post request
     */
    private RequestData() {}

    /**
     * Add data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData add(final String key, final CharSequence value) {
        data.put(key, value.toString());
        return this;
    }

    /**
     * Add data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData add(final String key, final File value) {
        data.put(key, value);
        return this;
    }

    /**
     * Add data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData add(final String key, final Path value) {
        data.put(key, value);
        return this;
    }

    /**
     * Add data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData add(final String key, final Collection<String> value) {
        data.put(key, value);
        return this;
    }

    /**
     * Add data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData add(final String key, final Number value) {
        data.put(key, value);
        return this;
    }

    /**
     * Add data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData add(final String key, final boolean value) {
        data.put(key, value);
        return this;
    }

    /**
     * Append existing data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData append(final String key, final RequestData value) {
        data.put(key, value);
        return this;
    }

    /**
     * Append existing data to the request
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public RequestData append(final String key, final Collection<RequestData> value) {
        data.put(key, value);
        return this;
    }

    /**
     * Remove data from the request
     *
     * @param key the key
     * @return this instance
     */
    public Object remove(final String key) {
        return data.remove(key);
    }

    /**
     * Set the request content type
     *
     * @param type the content type
     * @return this instance
     */
    public RequestData contentType(final ContentType type) {
        contentType = type;
        return this;
    }

    /**
     * Build the request
     *
     * @return the raw request data
     * @throws UnsupportedEncodingException if the URL encoded content type fails to build
     */
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public String build() throws UnsupportedEncodingException {
        String startChar = "";
        String finalChar = "";

        switch (contentType) {
            case PRETTY_JSON:
                startChar = "\n";
                finalChar = "\t";
            case JSON:
                StringBuilder jsonBuilder = new StringBuilder("{").append(finalChar);
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    jsonBuilder.append(buildIndent(1, startChar)).append("\"").append(key).append("\":");
                    if (value instanceof File) {
                        File file = (File) value;
                        if (file.isFile()) {
                            byte[] data = new byte[(int) file.length()];
                            try (FileInputStream fis = new FileInputStream(file)) {
                                fis.read(data);
                                jsonBuilder.append(" \"").append(Base64.getEncoder().encodeToString(data)).append("\",").append(finalChar);
                            } catch (IOException ex) {
                                ExceptionCollector.catchException(RequestData.class, ex);
                            }
                        }
                    }
                    if (value instanceof Path) {
                        Path path = (Path) value;
                        if (!Files.isDirectory(path)) {
                            byte[] data = PathUtilities.readBytes(path);
                            if (data.length > 0) {
                                jsonBuilder.append(" \"").append(Base64.getEncoder().encodeToString(data)).append("\",").append(finalChar);
                            }
                        }
                    }

                    if (value instanceof RequestData) {
                        RequestData sub = (RequestData) value;
                        jsonBuilder.append(" ").append(sub.contentType(contentType).build(2, false, false));
                    }
                    if (value instanceof String) {
                        jsonBuilder.append(" \"").append(value).append("\",").append(finalChar);
                    }
                    if (value instanceof Number || value instanceof Boolean) {
                        jsonBuilder.append(" ").append(value).append(",").append(finalChar);
                    }
                    if (value instanceof Collection) {
                        Collection<?> unknownCollection = (Collection<?>) value;
                        jsonBuilder.append(" [").append(finalChar);

                        int index = 0;
                        try {
                            Collection<RequestData> subRequests = (Collection<RequestData>) unknownCollection;
                            for (RequestData sub : subRequests) {
                                jsonBuilder.append(sub.contentType(contentType).build(2, true, index == subRequests.size() - 1));
                                index++;
                            }
                        } catch (ClassCastException ex) {
                            for (Object object : unknownCollection) {
                                jsonBuilder.append(buildIndent(2, startChar)).append("\"").append(object).append("\"")
                                        .append((index != unknownCollection.size() - 1 ? "," : "")).append(finalChar);
                                index++;
                            }
                        }

                        jsonBuilder.append(buildIndent(1, startChar)).append("],").append(finalChar);
                    }
                }

                jsonBuilder.deleteCharAt(jsonBuilder.length() - (finalChar.isEmpty() ? 1 : 2));
                jsonBuilder.append("}");
                return jsonBuilder.toString();
            case FORM:
                String charset = "UTF-8";

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                        try {
                            outputStream.write(("--" + boundary + "\r\n").getBytes(charset));
                            outputStream.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n" + value + "\r\n").getBytes(charset));
                        } catch (IOException ex) {
                            ExceptionCollector.catchException(RequestData.class, ex);
                        }
                    }
                    if (value instanceof File) {
                        File file = (File) value;
                        if (file.isFile()) {
                            try {
                                outputStream.write(("--" + boundary + "\r\n").getBytes(charset));
                                outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes(charset));
                                outputStream.write(("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n").getBytes(charset));
                                Files.copy(file.toPath(), outputStream);
                            } catch (IOException ex) {
                                ExceptionCollector.catchException(RequestData.class, ex);
                            }
                        }
                    }
                    if (value instanceof Path) {
                        Path path = (Path) value;
                        if (!Files.isDirectory(path)) {
                            try {
                                outputStream.write(("--" + boundary + "\r\n").getBytes(charset));
                                outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + path.getFileName().toString() + "\"\r\n").getBytes(charset));
                                outputStream.write(("Content-Type: " + Files.probeContentType(path) + "\r\n\r\n").getBytes(charset));
                                Files.copy(path, outputStream);
                            } catch (IOException ex) {
                                ExceptionCollector.catchException(RequestData.class, ex);
                            }
                        }
                    }
                }

                try {
                    outputStream.write(("--" + boundary + "\r\n").getBytes(charset));
                } catch (IOException ex) {
                    ExceptionCollector.catchException(RequestData.class, ex);
                    return null;
                }

                byte[] body = outputStream.toByteArray();
                return Base64.getEncoder().encodeToString(body);
            case ENCODED:
            default:
                StringBuilder urlEncodedBuilder = new StringBuilder();

                int index = 0;
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                    Object value = entry.getValue();

                    if (value instanceof File) {
                        File file = (File) value;
                        if (file.isFile()) {
                            byte[] data = new byte[(int) file.length()];
                            try (FileInputStream fis = new FileInputStream(file)) {
                                fis.read(data);
                                String encoded = URLEncoder.encode(Base64.getEncoder().encodeToString(data), StandardCharsets.UTF_8.name());
                                urlEncodedBuilder.append(key).append("=").append(encoded).append("&");
                            } catch (IOException ex) {
                                ExceptionCollector.catchException(RequestData.class, ex);
                            }
                        }
                    }
                    if (value instanceof Path) {
                        Path path = (Path) value;
                        if (!Files.isDirectory(path)) {
                            byte[] data = PathUtilities.readBytes(path);
                            if (data.length > 0) {
                                String encoded = URLEncoder.encode(Base64.getEncoder().encodeToString(data), StandardCharsets.UTF_8.name());
                                urlEncodedBuilder.append(key).append("=").append(encoded).append("&");
                            }
                        }
                    }
                    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                        String raw = URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8.name());
                        urlEncodedBuilder.append(key).append("=").append(raw).append("&");
                    }
                    if (value instanceof RequestData) {
                        RequestData sub = (RequestData) value;
                        urlEncodedBuilder.append(key).append("=").append("%7B").append(sub.contentType(contentType).build(0, false, index == data.size() - 1)).append("%7D&");
                    }
                    if (value instanceof Collection) {
                        Collection<?> unknownCollection = (Collection<?>) value;
                        try {
                            Collection<RequestData> subRequests = (Collection<RequestData>) unknownCollection;
                            for (RequestData sub : subRequests) urlEncodedBuilder.append(sub.contentType(contentType).build(0, false, index == data.size() - 1)).append("&");
                        } catch (ClassCastException ex) {
                            urlEncodedBuilder.append(key).append("=%5B");

                            int subIndex = 0;
                            for (Object object : unknownCollection) {
                                String raw = URLEncoder.encode(String.valueOf(object), StandardCharsets.UTF_8.name());
                                urlEncodedBuilder.append(raw).append((subIndex == unknownCollection.size() - 1 ? "" : ","));
                            }

                            urlEncodedBuilder.append("%5D&");
                        }
                    }

                    index++;
                }

                urlEncodedBuilder.deleteCharAt(urlEncodedBuilder.length() - 1);
                return urlEncodedBuilder.toString();
        }
    }

    /**
     * Build with indent
     *
     * @param indent the indent
     * @param applyFirst apply indent on first
     *                   item
     * @param last is this the last item?
     * @return the result with indent
     * @throws UnsupportedEncodingException if the URL encoded content type fails to build
     */
    @SuppressWarnings("unchecked")
    private String build(final int indent, final boolean applyFirst, final boolean last) throws UnsupportedEncodingException {
        String startChar = "";
        String finalChar = "";

        switch (contentType) {
            case PRETTY_JSON:
                finalChar = "\n";
                startChar = "\t";
            case JSON:
                StringBuilder jsonBuilder = new StringBuilder((applyFirst ? buildIndent(indent, startChar) : "")).append("{").append(finalChar);
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    jsonBuilder.append(buildIndent((applyFirst ? indent + 1 : indent), startChar)).append("\"").append(key).append("\":");

                    if (value instanceof File) {
                        File file = (File) value;
                        if (file.isFile()) {
                            byte[] data = new byte[(int) file.length()];
                            try (FileInputStream fis = new FileInputStream(file)) {
                                fis.read(data);
                                jsonBuilder.append(" \"").append(Base64.getEncoder().encodeToString(data)).append("\",").append(finalChar);
                            } catch (IOException ex) {
                                ExceptionCollector.catchException(RequestData.class, ex);
                            }
                        }
                    }
                    if (value instanceof Path) {
                        Path path = (Path) value;
                        if (!Files.isDirectory(path)) {
                            byte[] data = PathUtilities.readBytes(path);
                            if (data.length > 0) {
                                jsonBuilder.append(" \"").append(Base64.getEncoder().encodeToString(data)).append("\",").append(finalChar);
                            }
                        }
                    }

                    if (value instanceof RequestData) {
                        RequestData sub = (RequestData) value;
                        jsonBuilder.append(" ").append(sub.contentType(contentType).build((applyFirst ? indent + 2 : indent + 1), false, false));
                    }
                    if (value instanceof String) {
                        jsonBuilder.append(" \"").append(value).append("\",").append(finalChar);
                    }
                    if (value instanceof Number || value instanceof Boolean) {
                        jsonBuilder.append(" ").append(value).append(",").append(finalChar);
                    }
                    if (value instanceof Collection) {
                        Collection<?> unknownCollection = (Collection<?>) value;
                        jsonBuilder.append(" [").append(finalChar);

                        int index = 0;
                        try {
                            Collection<RequestData> subRequests = (Collection<RequestData>) unknownCollection;
                            for (RequestData sub : subRequests) {
                                jsonBuilder.append(sub.contentType(contentType).build(indent + 1, true, index == unknownCollection.size()))
                                        .append((index != unknownCollection.size() - 1 ? "," : "")).append(finalChar);
                                index++;
                            }
                        } catch (ClassCastException ex) {
                            for (Object object : unknownCollection) {
                                jsonBuilder.append(buildIndent((applyFirst ? indent + 2 : indent + 1), startChar))
                                        .append("\"").append(object).append("\"").append((index != unknownCollection.size() - 1 ? "," : "")).append(finalChar);
                                index++;
                            }
                        }

                        jsonBuilder.append(buildIndent((applyFirst ? indent + 1 : indent), startChar)).append("],").append(finalChar);
                    }
                }

                jsonBuilder.deleteCharAt(jsonBuilder.length() - (finalChar.isEmpty() ? 1 : 2));
                jsonBuilder.append(buildIndent((applyFirst ? indent : indent - 1), startChar)).append("}").append((last ? "" : ",")).append(finalChar);
                return jsonBuilder.toString();
            case FORM:
                return "NOT SUPPORTED";
            case ENCODED:
            default:
                StringBuilder urlEncodedBuilder = new StringBuilder();

                int index = 0;
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                    Object value = entry.getValue();

                    if (value instanceof File) {
                        File file = (File) value;
                        if (file.isFile()) {
                            byte[] data = new byte[(int) file.length()];
                            try (FileInputStream fis = new FileInputStream(file)) {
                                fis.read(data);
                                String encoded = URLEncoder.encode(Base64.getEncoder().encodeToString(data), StandardCharsets.UTF_8.name());
                                urlEncodedBuilder.append(key).append("=").append(encoded).append((last ? "" : "&"));
                            } catch (IOException ex) {
                                ExceptionCollector.catchException(RequestData.class, ex);
                            }
                        }
                    }
                    if (value instanceof Path) {
                        Path path = (Path) value;
                        if (!Files.isDirectory(path)) {
                            byte[] data = PathUtilities.readBytes(path);
                            if (data.length > 0) {
                                String encoded = URLEncoder.encode(Base64.getEncoder().encodeToString(data), StandardCharsets.UTF_8.name());
                                urlEncodedBuilder.append(key).append("=").append(encoded).append((last ? "" : "&"));;
                            }
                        }
                    }
                    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                        String raw = URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8.name());
                        urlEncodedBuilder.append(key).append("=").append(raw).append((last ? "" : "&"));
                    }
                    if (value instanceof RequestData) {
                        RequestData sub = (RequestData) value;
                        urlEncodedBuilder.append(key).append("=").append("%7B").append(sub.contentType(contentType).build(0, false, index == data.size() - 1)).append("%7D").append((last ? "" : "&"));
                    }
                    if (value instanceof Collection) {
                        Collection<?> unknownCollection = (Collection<?>) value;
                        try {
                            Collection<RequestData> subRequests = (Collection<RequestData>) unknownCollection;
                            for (RequestData sub : subRequests) urlEncodedBuilder.append(sub.contentType(contentType).build(0, false, index == data.size() - 1)).append((last ? "" : "&"));
                        } catch (ClassCastException ex) {
                            urlEncodedBuilder.append(key).append("=%5B");

                            int subIndex = 0;
                            for (Object object : unknownCollection) {
                                String raw = URLEncoder.encode(String.valueOf(object), StandardCharsets.UTF_8.name());
                                urlEncodedBuilder.append(raw).append((subIndex == unknownCollection.size() - 1 ? "" : ","));
                            }

                            urlEncodedBuilder.append("%5D").append((last ? "" : "&"));
                        }
                    }

                    index++;
                }

                if (!last) urlEncodedBuilder.deleteCharAt(urlEncodedBuilder.length() - 1);
                return urlEncodedBuilder.toString();
        }
    }

    /**
     * Build the indent
     *
     * @param size the indent size
     * @return the indent
     */
    private String buildIndent(final int size, final String character) {
        if (character.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) builder.append("\t");
        return builder.toString();
    }

    /**
     * Create a new post request
     *
     * @return the new post request
     */
    public static RequestData newRequest() {
        return new RequestData();
    }

    /**
     * Content type
     */
    public enum ContentType {
        /**
         * Json content type
         */
        JSON,
        /**
         * Json content type (pretty)
         */
        PRETTY_JSON,
        /**
         * Form content type
         */
        FORM,
        /**
         * URL encoded
         */
        ENCODED
    }
}
