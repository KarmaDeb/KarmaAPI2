package es.karmadev.api.web.minecraft;

import com.google.gson.*;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.runner.async.AsyncTaskExecutor;
import es.karmadev.api.schedule.task.completable.TaskCompletor;
import es.karmadev.api.schedule.task.completable.late.LateTask;
import es.karmadev.api.web.minecraft.response.data.*;
import es.karmadev.api.web.minecraft.response.data.component.*;
import es.karmadev.api.web.request.HeadEntry;
import es.karmadev.api.web.url.URLUtilities;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * KarmaAPI minecraft online API
 * @deprecated See {@code UUIDFetcher uuid-fetcher}
 */
@SuppressWarnings({"DataFlowIssue", "unused"})
@Deprecated
public class MineAPI {

    private static long threadIndex = 0;
    private final static APISource source = KarmaKore.INSTANCE();
    private final static Gson gson = new GsonBuilder().create();
    public final static int VERY_SMALL = 64;
    public final static int SMALL = 128;
    public final static int MEDIUM = 256;
    public final static int BIG = 512;
    public final static int DEFAULT = 1024;
    public static long CACHE_LIFETIME = TimeUnit.DAYS.toMillis(3);

    /**
     * Try to push the nick data
     *
     * @param nick the nick
     * @return the response
     */
    public static TaskCompletor<OKARequest> publish(final String nick) {
        TaskCompletor<OKARequest> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            OKARequest request = runMethod("push", nick);
            task.complete(request);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Try to push the nick data
     * synchronously
     *
     * @param nick the nick
     * @return the response
     */
    public static OKARequest publishAndWait(final String nick) {
        return runMethod("push", nick);
    }

    /**
     * Fetch a user information
     *
     * @param nick the nick
     * @return the user information
     */
    public static TaskCompletor<OKARequest> fetch(final String nick) {
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick).getBytes());
        return fetch(offlineId);
    }

    /**
     * Fetch a user information
     * synchronously
     *
     * @param nick the nick
     * @return the response
     */
    public static OKARequest fetchAndWait(final String nick) {
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick).getBytes());
        return fetchAndWait(offlineId);
    }

    /**
     * Fetch a user information
     *
     * @param id the user unique id
     * @return the user information
     */
    public static TaskCompletor<OKARequest> fetch(final UUID id) {
        TaskCompletor<OKARequest> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            OKARequest request = runMethod("fetch", id.toString());
            task.complete(request);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Fetch a user information
     * synchronously
     *
     * @param id the user unique id
     * @return the response
     */
    public static OKARequest fetchAndWait(final UUID id) {
        return runMethod("fetch", id.toString());
    }

    /**
     * Fetch the head image of the client
     *
     * @param name the client name
     * @return the head image or null if none
     */
    public static TaskCompletor<OKAHeadRequest> fetchHead(final String name) {
        TaskCompletor<OKAHeadRequest> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            OKAHeadRequest request = fetchHeadAndWait(name, DEFAULT);
            task.complete(request);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Fetch the head image of the client
     *
     * @param name the client name
     * @return the head image or null if none
     */
    public static OKAHeadRequest fetchHeadAndWait(final String name) {
        return fetchHeadAndWait(name, DEFAULT);
    }

    /**
     * Fetch the head image of the client
     *
     * @param name the client name
     * @param request_size request_size the image size
     * @return the head image or null if none
     */
    public static TaskCompletor<OKAHeadRequest> fetchHead(final String name, final int request_size) {
        TaskCompletor<OKAHeadRequest> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            OKAHeadRequest request = fetchHeadAndWait(name, request_size);
            task.complete(request);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Fetch the head image of the client
     *
     * @param name the client name
     * @param request_size request_size the image size
     * @return the head image or null if none
     */
    public static OKAHeadRequest fetchHeadAndWait(final String name, final int request_size) {
        URL url = URLUtilities.getOptional(
                "https://api.karmadev.es/head/" + name + "/" + request_size + "/",
                "https://backup.karmadev.es/api/head/" + name + "/" + request_size + "/").orElse(null);

        if (url == null) {
            return OKAHeadRequest.empty();
        }

        URL modified = URLUtilities.append(url, "?display=json");
        String okaResponse = URLUtilities.get(modified, HeadEntry.valueOf("Source-Identifier", source.identifier()));
        try {
            JsonObject object = gson.fromJson(okaResponse, JsonObject.class);
            if (object.has("message") && object.has("code")) {
                String message = object.get("message").getAsString();
                String code = object.get("code").getAsString();

                if (!code.equals("ERR_TOO_MANY_REQUESTS")) {
                    source.logger().send(LogLevel.SEVERE, "Failed to execute request {0} ({1} - {2})", url, message, code);
                }

                return OKAHeadRequest.empty(okaResponse);
            }

            long id = object.get("id").getAsLong();
            int size = object.get("size").getAsInt();
            String rawUrl = object.get("url").getAsString();
            String value = object.getAsJsonObject("texture").get("value").getAsString();
            String signature = object.getAsJsonObject("texture").get("signature").getAsString();
            String head = object.get("image").getAsString();

            URL skinURL = new URL(rawUrl);
            return OKAHeadRequest.builder()
                    .uri(url.toURI())
                    .id(id)
                    .size(size)
                    .texture(SkinComponent.of(skinURL, value, signature, null))
                    .head(head)
                    .json(okaResponse).build();
        } catch (JsonSyntaxException | MalformedURLException | URISyntaxException ex) {
            source.logger().log(ex, "Failed to fetch minecraft head from web API");
        }

        return OKAHeadRequest.empty();
    }

    /**
     * Fetch the server information
     *
     * @param address the server address
     * @param port the server port
     */
    public static TaskCompletor<OKAServerRequest> fetchServer(final String address, final int port) {
        TaskCompletor<OKAServerRequest> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            OKAServerRequest request = fetchServerAndWait(address, port);
            task.complete(request);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Fetch the server information
     *
     * @param address the server address
     */
    public static TaskCompletor<OKAServerRequest> fetchServer(final String address) {
        TaskCompletor<OKAServerRequest> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            OKAServerRequest request = fetchServerAndWait(address, 25565);
            task.complete(request);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Fetch the server information
     *
     * @param address the server address
     * @param port the server port
     */
    public static OKAServerRequest fetchServerAndWait(final String address, final int port) {
        URL url = URLUtilities.getOptional(
                "https://api.karmadev.es/?address=" + address + "&port=" + port + "&display=json",
                "https://backup.karmadev.es/api/?address=" + address + "&port=" + port + "&display=json").orElse(null);

        if (url == null) {
            return OKAServerRequest.empty(address, port);
        }

        URL modified = URLUtilities.append(url, "?display=json");
        String okaResponse = URLUtilities.get(modified, HeadEntry.valueOf("Source-Identifier", source.identifier()));
        try {
            JsonObject object = gson.fromJson(okaResponse, JsonObject.class);
            if (object.has("message") && object.has("code")) {
                String message = object.get("message").getAsString();
                String code = object.get("code").getAsString();

                if (!code.equals("ERR_TOO_MANY_REQUESTS")) {
                    source.logger().send(LogLevel.SEVERE, "Failed to execute request {0} ({1} - {2})", url, message, code);
                }

                return OKAServerRequest.empty(address, port, okaResponse);
            }

            long cache = object.get("cache").getAsLong();
            Instant instant = Instant.ofEpochMilli(cache);
            String platform = object.getAsJsonObject("version").get("platform").getAsString();
            int protocol = object.getAsJsonObject("version").get("protocol").getAsInt();
            int players = object.getAsJsonObject("players").get("online").getAsInt();
            int maxPlayers = object.getAsJsonObject("players").get("max").getAsInt();
            String[] motd = new String[2];
            JsonArray array = object.get("motd").getAsJsonArray();
            int index = 0;
            for (JsonElement element : array) {
                if (index > 1) {
                    break;
                }

                motd[index++] = element.getAsString();
            }
            String icon = object.get("icon").getAsString();
            String srvHost = object.getAsJsonObject("srv").get("host").getAsString();
            int srvPort = object.getAsJsonObject("srv").get("port").getAsInt();
            long latency = object.getAsJsonObject("srv").get("latency").getAsLong();

            return OKAServerRequest.builder()
                    .uri(url.toURI())
                    .cache(instant)
                    .platform(platform)
                    .protocol(protocol)
                    .onlinePlayers(players)
                    .maxPlayers(maxPlayers)
                    .motd(motd)
                    .icon(icon)
                    .address(InetSocketAddress.createUnresolved(address, port))
                    .srv(ServerComponent.of(srvHost, srvPort, latency))
                    .json(okaResponse).build();
        } catch (JsonSyntaxException | URISyntaxException ex) {
            source.logger().log(ex, "Failed to fetch minecraft server from web API");
        }

        return OKAServerRequest.empty(address, port);
    }

    /**
     * Get the API size
     *
     * @return the API size
     */
    public static Long sizeAndWait() {
        URL url = URLUtilities.getOptional(
                "https://api.karmadev.es/fetch/@all",
                "https://backup.karmadev.es/api/fetch/@all").orElse(null);

        if (url == null) {
            return -1L;
        }

        String okaResponse = URLUtilities.get(url, HeadEntry.valueOf("Source-Identifier", source.identifier()));
        try {
            JsonObject object = gson.fromJson(okaResponse, JsonObject.class);
            if (object.has("message") && object.has("code")) {
                String message = object.get("message").getAsString();
                String code = object.get("code").getAsString();

                if (!code.equals("ERR_TOO_MANY_REQUESTS")) {
                    source.logger().send(LogLevel.SEVERE, "Failed to execute request {0} ({1} - {2})", url, message, code);
                }
                return -1L;
            }

            return object.get("stored").getAsLong();
        } catch (JsonSyntaxException ex) {
            source.logger().log(ex, "Failed to fetch minecraft data from web API");
        }

        return -1L;
    }

    /**
     * Get the API size
     *
     * @return the API size
     */
    public static TaskCompletor<Long> size() {
        TaskCompletor<Long> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            long amount = sizeAndWait();
            task.complete(amount);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Get the API max pages
     *
     * @return the API pages
     */
    public static Long pagesAndWait() {
        URL url = URLUtilities.getOptional(
                "https://api.karmadev.es/fetch/@all",
                "https://backup.karmadev.es/api/fetch/@all").orElse(null);

        if (url == null) {
            return -1L;
        }

        String okaResponse = URLUtilities.get(url, HeadEntry.valueOf("Source-Identifier", source.identifier()));
        try {
            JsonObject object = gson.fromJson(okaResponse, JsonObject.class);
            if (object.has("message") && object.has("code")) {
                String message = object.get("message").getAsString();
                String code = object.get("code").getAsString();

                if (!code.equals("ERR_TOO_MANY_REQUESTS")) {
                    source.logger().send(LogLevel.WARNING, "Failed to execute request {0} ({1} - {2})", url, message, code);
                }

                return -1L;
            }

            return object.get("pages").getAsLong();
        } catch (JsonSyntaxException ex) {
            source.logger().log(ex, "Failed to fetch minecraft data from web API");
        }

        return -1L;
    }

    /**
     * Get the API max pages
     *
     * @return the API pages
     */
    public static TaskCompletor<Long> pages() {
        TaskCompletor<Long> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            long amount = pagesAndWait();
            task.complete(amount);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Get all the information
     *
     * @param page the information page
     * @return the information
     */
    public static MultiOKARequest fetchAllAndWait(final long page) {
        URL url = URLUtilities.getOptional(
                "https://api.karmadev.es/fetch/@all",
                "https://backup.karmadev.es/api/fetch/@all").orElse(null);

        if (url == null) {
            return MultiOKARequest.empty();
        }

        String okaResponse = URLUtilities.get(url, HeadEntry.valueOf("Source-Identifier", source.identifier()));
        try {
            JsonObject multiObject = gson.fromJson(okaResponse, JsonObject.class);
            if (multiObject.has("message") && multiObject.has("code")) {
                String message = multiObject.get("message").getAsString();
                String code = multiObject.get("code").getAsString();

                if (!code.equals("ERR_TOO_MANY_REQUESTS")) {
                    source.logger().send(LogLevel.WARNING, "Failed to execute request {0} ({1} - {2})", url, message, code);
                }
                return MultiOKARequest.empty();
            }

            long stored = multiObject.get("stored").getAsLong();
            long pages = multiObject.get("pages").getAsLong();

            List<OKARequest> accounts = new ArrayList<>();
            JsonObject fetched = multiObject.getAsJsonObject("fetched");
            String host = URLUtilities.getDomain(url).root();

            for (String nick : fetched.keySet()) {
                try {
                    URL virtualID = new URL("https://" + host + "/api/fetch/" + nick);
                    JsonObject object = fetched.get(nick).getAsJsonObject();
                    object.addProperty("name", nick);

                    String raw = gson.toJson(object);

                    OKARequest request = build(virtualID, raw, object);
                    accounts.add(request);
                } catch (URISyntaxException | MalformedURLException ignored) {}
            }

            return MultiOKARequest.builder()
                    .stored(stored)
                    .page(Math.max(1, page))
                    .pages(pages)
                    .fetched(accounts.size())
                    .accounts(accounts.toArray(new OKARequest[0])).build();
        } catch (JsonSyntaxException ex) {
            source.logger().log(ex, "Failed to fetch minecraft data from web API");
        }

        return MultiOKARequest.empty();
    }

    /**
     * Get all the information
     *
     * @param page the information page
     * @return the information
     */
    public static TaskCompletor<MultiOKARequest> fetchAll(final long page) {
        LateTask<MultiOKARequest> task = new LateTask<>();
        AsyncTaskExecutor.EXECUTOR.schedule(() -> {
            MultiOKARequest request = fetchAllAndWait(page);
            task.complete(request);
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Get a UUID from a trimmed UUID
     *
     * @param id the trimmed UUID
     * @return the full UUID
     */
    @Nullable
    public static UUID fromTrimmed(final String id) {
        UUID result = null;
        try {
            if (!ObjectUtils.isNullOrEmpty(id)) {
                if (!id.contains("-")) {
                    StringBuilder builder = new StringBuilder(id);
                    builder.insert(20, "-");
                    builder.insert(16, "-");
                    builder.insert(12, "-");
                    builder.insert(8, "-");
                    result = UUID.fromString(builder.toString());
                } else {
                    result = UUID.fromString(id);
                }
            }
        } catch (Throwable ignored) {}

        return result;
    }

    private static OKARequest runMethod(final String method, final String argument) {
        if (method.equalsIgnoreCase("push")) {
            UUID id = fromTrimmed(argument);
            if (id != null) {
                return OKARequest.empty();
            }
        }

        URL url = URLUtilities.getOptional(
                "https://api.karmadev.es/" + method + "/" + argument,
                "https://backup.karmadev.es/api/" + method + "/" + argument).orElse(null);

        if (url == null) {
            source.logger().log(LogLevel.SEVERE, "Failed to execute {0} on {1} because URL was null", method, argument);
            return OKARequest.empty();
        }

        Path cacheFile = source.workingDirectory().resolve("cache").resolve("oka").resolve(argument.replace("-", "") + ".json");

        if (method.equalsIgnoreCase("fetch")) {
            if (Files.exists(cacheFile)) {
                try(BufferedReader reader = Files.newBufferedReader(cacheFile)) {
                    JsonElement element = gson.fromJson(reader, JsonElement.class);
                    if (element.isJsonObject()) {
                        JsonObject cachedRequest = element.getAsJsonObject();
                        if (cachedRequest.has("cache")) {
                            long cachedTimeLimit = cachedRequest.get("cache").getAsLong();

                            Instant now = Instant.now();
                            Instant instant = Instant.ofEpochMilli(cachedTimeLimit);
                            if (instant.isAfter(now)) {
                                String raw = gson.toJson(element);
                                try {
                                    return build(url, raw, cachedRequest);
                                } catch (JsonSyntaxException | URISyntaxException ex) {
                                    source.logger().log(ex, "Failed to push minecraft data into web API");
                                }
                            }
                        }
                    }
                } catch (JsonSyntaxException | IOException ignored) {}
            }
        }

        String okaResponse = URLUtilities.get(url, HeadEntry.valueOf("Source-Identifier", source.identifier()));
        try {
            JsonObject object = gson.fromJson(okaResponse, JsonObject.class);
            if (method.equalsIgnoreCase("fetch")) {
                Instant now = Instant.now().plusMillis(CACHE_LIFETIME);
                object.addProperty("cache", now.toEpochMilli());
                PathUtilities.createPath(cacheFile);

                try(BufferedWriter writer = Files.newBufferedWriter(cacheFile)) {
                    gson.toJson(object, writer);
                } catch (IOException ex) {
                    source.logger().log(ex, "Failed to store account cache");
                }
            }

            return build(url, okaResponse, object);
        } catch (JsonSyntaxException | URISyntaxException ex) {
            source.logger().log(ex, "Failed to push minecraft data into web API");
        }

        return OKARequest.empty(okaResponse);
    }

    private static OKARequest build(final URL url, final String raw, final JsonObject object) throws URISyntaxException {
        if (object.has("message") && object.has("code")) {
            String message = object.get("message").getAsString();
            String code = object.get("code").getAsString();

            if (!code.equals("ERR_TOO_MANY_REQUESTS")) {
                source.logger().send(LogLevel.SEVERE,"Failed to execute request {0} ({1} - {2})", url, message, code);
            }
            return OKARequest.empty(raw);
        }

        long id = object.get("id").getAsLong();
        String genName = object.get("name").getAsString();
        UUID nameId = fromTrimmed(genName);
        if (nameId != null) {
            return OKARequest.empty(raw);
        }

        String creation = object.get("created").getAsString();

        String offline_uuid = object.get("offline")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("data")
                .get("id").getAsString();
        String online_uuid = object.get("online")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("data")
                .get("id").getAsString();
        SkinComponent skin = SkinComponent.empty();
        CapeComponent cape = CapeComponent.empty();

        JsonArray properties = object.getAsJsonArray("properties");
        for (JsonElement element : properties) {
            JsonObject subElement = element.getAsJsonObject();
            String name = subElement.get("name").getAsString();

            if (name.equalsIgnoreCase("skin")) {
                if (subElement.has("url") && subElement.has("data") && subElement.has("value") && subElement.has("signature")) {
                    try {
                        URL skinURL = new URL(subElement.get("url").getAsString());
                        String data = subElement.get("data").getAsString();
                        String value = subElement.get("value").getAsString();
                        String signature = subElement.get("signature").getAsString();

                        skin = SkinComponent.of(skinURL, data, value, signature);
                    } catch (MalformedURLException ignored) {}
                }
            } else {
                if (subElement.has("url") && subElement.has("data")) {
                    try {
                        URL capeURL = new URL(subElement.get("url").getAsString());
                        String data = subElement.get("data").getAsString();

                        cape = CapeComponent.of(capeURL, data);
                    } catch (MalformedURLException ignored) {}
                }
            }
        }

        UUID offline = null;
        UUID online = null;
        try {
            offline = UUID.fromString(offline_uuid);
            if (!online_uuid.equalsIgnoreCase("unknown")) {
                online = UUID.fromString(online_uuid);
            }
        } catch (IllegalArgumentException ignored) {}

        return OKARequest.builder()
                .uri(url.toURI())
                .id(id)
                .nick(genName)
                .creation(creation)
                .offline(offline)
                .online(online)
                .skin(skin)
                .cape(cape)
                .json(raw)
                .build();
    }
}
