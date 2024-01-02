package es.karmadev.api.minecraft.uuid;

import com.google.gson.*;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.web.request.HeadEntry;
import es.karmadev.api.web.url.URLUtilities;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Minecraft UUID utilities
 */
public final class UUIDFetcher {

    private static APISource source = SourceManager.getUnsafePrincipal();
    static {
        while (source == null) source = SourceManager.getUnsafePrincipal();
    }

    private final static String MOJANG = "https://api.mojang.com/users/profiles/minecraft/%s";
    private final static String MINETOOLS = "https://api.minetools.eu/uuid/%s";
    private final static String ASHCON = "https://api.ashcon.app/mojang/v2/user/%s";

    /**
     * Get an uuid from its simple version
     *
     * @param raw the raw UUID
     * @return the UUID
     * @throws IndexOutOfBoundsException if the raw UUID is not a raw UUID string
     */
    public static UUID fromSimple(final String raw) throws IndexOutOfBoundsException {
        try {
            return UUID.fromString(raw); //Is it even simple?
        } catch (IllegalArgumentException ex) {
            StringBuilder idBuff = new StringBuilder(raw);
            idBuff.insert(20, '-');
            idBuff.insert(16, '-');
            idBuff.insert(12, '-');
            idBuff.insert(8, '-');

            return UUID.fromString(idBuff.toString());
        }
    }

    /**
     * Get the simple version of the UUID
     *
     * @param id the uuid
     * @return the UUID simple version
     */
    public static String toSimple(final UUID id) {
        return id.toString().replaceAll("-", "");
    }

    /**
     * Validates a minecraft username
     *
     * @param name the name
     * @throws NullPointerException if the name is null or empty
     * @throws IllegalArgumentException if the name is not valid
     */
    public static void validateName(final String name) throws NullPointerException, IllegalArgumentException {
        ObjectUtils.assertNullOrEmpty(name, "Name cannot be null/empty");
        int length = name.length();

        if (length > 16) throw new IllegalArgumentException("Name cannot be longer than 16 characters");
        if (length < 3) throw new IllegalArgumentException("Name cannot be lower than 3 characters");

        int position = 0;
        for (char character : name.toCharArray()) {
            position++;

            String ch = String.valueOf(character);
            if (ch.matches("[^A-z-0-9-_]")) {
                throw new IllegalArgumentException("Name cannot contain special characters (other than numbers, \"-\" and/or \"_\") Found: " + character + " at position: " + position);
            }
        }
    }

    /**
     * Fetch an UUID
     *
     * @param name the minecraft name
     * @param type the UUID type
     * @return the fetched UUID
     */
    @Nullable
    public static UUID fetchUUID(final String name, final UUIDType type) {
        try {
            validateName(name);
        } catch (NullPointerException | IllegalArgumentException ex) {
            return null;
        }

        UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
        if (type.equals(UUIDType.OFFLINE)) {
            return offline;
        }

        UUID stored = getCache(offline);
        if (stored != null) return stored;

        URL ashcon = URLUtilities.getOptional(String.format(ASHCON, name)).orElse(null);
        if (ashcon != null) {
            String response = URLUtilities.get(ashcon);
            Gson gson = new GsonBuilder().create();
            JsonElement element = gson.fromJson(response, JsonElement.class);
            if (element != null && element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("uuid") && object.get("uuid").isJsonPrimitive() && object.getAsJsonPrimitive("uuid").isString()) {
                    String rawUUID = object.get("uuid").getAsString();
                    try {
                        UUID fetched = UUID.fromString(rawUUID);
                        doCache(offline, fetched);

                        return fetched;
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        return tryFallback(name, offline, 0);
    }

    private static UUID tryFallback(final String name, final UUID offline, final int intent) {
        if (intent > 1) return null;

        URL fallback = URLUtilities.getOptional((intent == 0 ? String.format(MOJANG, name) : String.format(MINETOOLS, name))).orElse(null);
        if (fallback != null) {
            String response = URLUtilities.get(fallback, HeadEntry.valueOf("", ""));
            Gson gson = new GsonBuilder().create();
            JsonElement element = gson.fromJson(response, JsonElement.class);
            if (element != null && element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("id") && object.get("id").isJsonPrimitive() && object.getAsJsonPrimitive("id").isString()) {
                    String rawUUID = object.get("id").getAsString();
                    try {
                        UUID fetched = fromSimple(rawUUID);
                        doCache(offline, fetched);

                        return fetched;
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        return tryFallback(name, offline, (intent + 1));
    }

    private static UUID getCache(final UUID offline) {
        Path fetchCache = source.workingDirectory().resolve("cache").resolve("uuid").resolve("fetch.json");
        PathUtilities.createPath(fetchCache);

        Gson gson = new GsonBuilder().create();
        JsonElement element = null;
        JsonElement cloneElement = null;
        try {
            element = gson.fromJson(PathUtilities.pathString(fetchCache), JsonElement.class);
            cloneElement = gson.fromJson(PathUtilities.pathString(fetchCache), JsonElement.class);
        } catch (JsonSyntaxException ignored) {}

        if (!(element instanceof JsonObject)) {
            element = new JsonObject();
        }
        if (!(cloneElement instanceof JsonObject)) {
            cloneElement = new JsonObject();
        }

        JsonObject object = element.getAsJsonObject();
        JsonObject clone = cloneElement.getAsJsonObject();

        List<String> keys = clone.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());

        boolean write = false;
        for (String key : keys) {
            JsonArray array = clone.getAsJsonArray(key);
            Iterator<JsonElement> elements = array.iterator();

            boolean modifications = false;
            while (elements.hasNext()) {
                JsonElement children = elements.next();
                JsonObject cacheItem = children.getAsJsonObject();
                String uuid = cacheItem.get("uuid").getAsString();

                if (uuid.equals(toSimple(offline))) {
                    long expiration = cacheItem.get("expiration").getAsLong();
                    long now = System.currentTimeMillis();

                    if (expiration >= now) {
                        return fromSimple(key);
                    } else {
                        elements.remove();
                        modifications = true;
                    }
                }
            }

            if (modifications) {
                object.add(key, array);
                write = true;
            }
        }

        if (write) {
            String raw = gson.toJson(object);
            try {
                Files.write(fetchCache, raw.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return null;
    }

    private static void doCache(final UUID offline, final UUID online) {
        Path fetchCache = source.workingDirectory().resolve("cache").resolve("uuid").resolve("fetch.json");
        PathUtilities.createPath(fetchCache);

        Gson gson = new GsonBuilder().create();
        JsonElement element = null;
        try {
            element = gson.fromJson(PathUtilities.read(fetchCache), JsonElement.class);
        } catch (JsonSyntaxException ignored) {}
        if (!(element instanceof JsonObject)) {
            element = new JsonObject();
        }

        JsonObject object = element.getAsJsonObject();
        JsonArray array = new JsonArray();
        if (object.has(toSimple(online))) {
            array = object.getAsJsonArray(toSimple(online));
        }

        JsonObject cacheItem = new JsonObject();
        cacheItem.addProperty("uuid", toSimple(offline));
        cacheItem.addProperty("expiration", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(72));

        boolean write = true;
        JsonElement existingEntry = null;
        for (JsonElement children : array) {
            JsonObject childItem = children.getAsJsonObject();
            String uuid = childItem.get("uuid").getAsString();

            if (uuid.equals(toSimple(offline))) {
                long expiration = childItem.get("expiration").getAsLong();
                long now = System.currentTimeMillis();

                if (expiration < now) {
                    existingEntry = children;
                } else{
                    write = false;
                }
            }
        }

        if (write) {
            if (existingEntry != null) {
                array.remove(existingEntry);
            }

            array.add(cacheItem);
            object.add(toSimple(online), array);

            String raw = gson.toJson(object);
            try {
                Files.write(fetchCache, raw.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
