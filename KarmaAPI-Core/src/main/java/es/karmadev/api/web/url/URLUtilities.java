package es.karmadev.api.web.url;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.KarmaAPI;
import es.karmadev.api.core.config.APIConfiguration;
import es.karmadev.api.web.URLConnectionWrapper;
import es.karmadev.api.web.request.HeadEntry;
import es.karmadev.api.web.request.RequestData;
import es.karmadev.api.web.url.domain.SubDomain;
import es.karmadev.api.web.url.domain.WebDomain;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@SuppressWarnings("unused")
public class URLUtilities {

    /**
     * Get a URL or null from the
     * string URL
     *
     * @param url the string url
     * @return the url
     */
    @Nullable
    public static URL fromString(final CharSequence url) {
        try {
            return new URL(url.toString());
        } catch (MalformedURLException ex) {
            ExceptionCollector.catchException(URLUtilities.class, ex);
            return null;
        }
    }

    /**
     * Get the first valid URL of the
     * provided ones
     *
     * @param urls the URL
     * @return the first valid URL
     */
    public static Optional<URL> getOptional(final String... urls) {
        URL validURL = null;

        for (CharSequence url : urls) {
            try {
                URL tempURL = new URL(url.toString());
                if (exists(tempURL)) {
                    validURL = tempURL;
                    break;
                }
            } catch (MalformedURLException ex) {
                ExceptionCollector.catchException(URLUtilities.class, ex);
            }
        }

        return Optional.ofNullable(validURL);
    }

    /**
     * Check if the URL exists
     *
     * @param url the url
     * @return if the url exists
     */
    public static boolean exists(final @Nullable URL url) {
        if (url == null) return false;

        APIConfiguration configuration = new APIConfiguration();
        int code = getResponseCode(url);

        return (configuration.strictURLCodes() ? code == HttpURLConnection.HTTP_OK : code < HttpURLConnection.HTTP_MULT_CHOICE && code >= HttpURLConnection.HTTP_OK);
    }

    /**
     * Get the URL response code
     *
     * @param url the url to get response
     *            code from
     * @return the response code
     */
    public static int getResponseCode(final @Nullable URL url) {
        if (url == null) return -1;
        APIConfiguration configuration = new APIConfiguration();

        try {
            try (URLConnectionWrapper wrapper = URLConnectionWrapper.fromURL(url)) {
                wrapper.setUserAgent(KarmaAPI.USER_AGENT.get());
                wrapper.setConnectTimeout(configuration.requestTimeout());
                wrapper.setInstanceFollowRedirects(true);
                wrapper.setRequestMethod("HEAD");

                return wrapper.getResponseCode();
            }
        } catch (IOException ex) {
            ExceptionCollector.catchException(URLUtilities.class, ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Append data to a URL
     *
     * @param url the url to modify
     * @param target the data to append
     * @return the modified URL
     */
    public static URL append(final URL url, final String... target) {
        String rawURL = url.toString();
        StringBuilder urlString = new StringBuilder();
        if (rawURL.endsWith("/")) {
            urlString.append(rawURL, 0, rawURL.length() - 1);
        } else {
            urlString.append(rawURL);
        }

        for (String data : target) {
            if (data.startsWith("/")) {
                urlString.append(data);
            } else {
                urlString.append("/").append(data);
            }
        }

        return fromString(urlString);
    }

    /**
     * Get the domain of a URL
     *
     * @param url the url
     * @return the URL domain
     */
    @Nullable
    public static WebDomain getDomain(final @Nullable URL url) {
        if (url == null) return null;

        try {
            URI uri = url.toURI();

            String protocol = uri.getScheme();
            String name = uri.getHost();

            String[] tokens = (name == null || name.isEmpty()) ? new String[]{"local"} : name.split("\\.");
            switch (tokens.length) {
                case 1:
                    return WebDomain.of(protocol, new SubDomain(), tokens[0], null);
                case 2:
                    return WebDomain.of(protocol, new SubDomain(), tokens[0], tokens[1]);
                default:
                    String host = tokens[tokens.length - 2];
                    String tld = tokens[tokens.length - 1];

                    int max = tokens.length - 2;
                    String[] subs = Arrays.copyOfRange(tokens, 0, max);

                    return WebDomain.of(protocol, new SubDomain(subs), host, tld);
            }
        } catch (URISyntaxException ex) {
            ExceptionCollector.catchException(URLUtilities.class, ex);
            return null;
        }
    }

    /**
     * Post data to a URL
     *
     * @param url the URL
     * @param request the post request
     * @param entries the header entries
     * @return the response
     */
    public static String post(final URL url, final RequestData request, final HeadEntry... entries) {
        if (url == null) return "";
        APIConfiguration configuration = new APIConfiguration();

        try (URLConnectionWrapper wrapper = URLConnectionWrapper.fromURL(url)) {
            String requestData = request.build();
            if (requestData == null) return "";

            wrapper.setUserAgent(KarmaAPI.USER_AGENT.get());
            wrapper.setConnectTimeout(configuration.requestTimeout());
            wrapper.setInstanceFollowRedirects(true);
            wrapper.setRequestMethod("POST");
            wrapper.setUseCaches(false);
            byte[] raw;

            if (request.getContentType().equals(RequestData.ContentType.FORM)) {
                wrapper.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + request.getBoundary());
                raw = Base64.getDecoder().decode(requestData);
            } else {
                wrapper.setContentType(request.getContentType());
                raw = requestData.getBytes(StandardCharsets.UTF_8);
            }

            wrapper.setRequestProperty("Content-Length", Integer.toString(raw.length));

            wrapper.setDoOutput(true);
            wrapper.setDoInput(true);

            for (HeadEntry entry : entries) {
                wrapper.setRequestProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }

            try (OutputStream out = wrapper.getOutputStream()) {
                out.write(raw);
                out.flush();
            }

            try (InputStream stream = wrapper.getInputStream()) {
                if (stream != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(stream));

                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    return response.toString();
                }

                return "";
            }
        } catch (IOException ex) {
            ExceptionCollector.catchException(URLUtilities.class, ex);
            return "";
        }
    }

    /**
     * Get data from a URL
     *
     * @param url the URL
     * @param entries the header entries
     * @return the response
     */
    public static String get(final URL url, final HeadEntry... entries) {
        if (url == null) return "";
        //APIConfiguration configuration = new APIConfiguration();

        try (URLConnectionWrapper wrapper = URLConnectionWrapper.fromURL(url)) {
            wrapper.setUserAgent(KarmaAPI.USER_AGENT.get());
            //wrapper.setConnectTimeout(configuration.requestTimeout());
            wrapper.setInstanceFollowRedirects(true);
            wrapper.setRequestMethod("GET");
            wrapper.setUseCaches(false);

            wrapper.setDoInput(true);

            for (HeadEntry entry : entries) {
                wrapper.setRequestProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }

            try (InputStream stream = wrapper.getInputStream()) {
                if (stream != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(stream));

                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    return response.toString();
                }

                return "";
            }
        } catch (IOException ex) {
            ExceptionCollector.catchException(URLUtilities.class, ex);
            return "";
        }
    }

    /**
     * Download a resource
     *
     * @param url the url to download from
     * @param entries the request head entries
     * @return the resource
     */
    public static InputStream download(final URL url, final HeadEntry... entries) {
        return download("GET", url, entries);
    }

    /**
     * Download a resource
     *
     * @param method the request method
     * @param url the url to download from
     * @param entries the request head entries
     * @return the resource
     */
    public static InputStream download(final String method, final URL url, final HeadEntry... entries) {
        if (url == null) return null;
        //APIConfiguration configuration = new APIConfiguration();

        try (URLConnectionWrapper wrapper = URLConnectionWrapper.fromURL(url)) {
            wrapper.setUserAgent(KarmaAPI.USER_AGENT.get());
            //wrapper.setConnectTimeout(configuration.requestTimeout());
            wrapper.setInstanceFollowRedirects(true);
            wrapper.setRequestMethod(method.toUpperCase());
            wrapper.setUseCaches(false);

            wrapper.setDoInput(true);

            for (HeadEntry entry : entries) {
                wrapper.setRequestProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }

            return wrapper.getInputStream();
        } catch (IOException ex) {
            ExceptionCollector.catchException(URLUtilities.class, ex);
        }

        return null;
    }
}
