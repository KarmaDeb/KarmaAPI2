package es.karmadev.api.minecraft.uuid;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.KsonException;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.web.request.HeadEntry;
import es.karmadev.api.web.url.URLUtilities;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
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
            JsonInstance element = JsonReader.read(response);
            if (element.isObjectType()) {
                JsonObject object = element.asObject();
                if (object.hasChild("uuid") && object.getChild("uuid").isNativeType() && object.getChild("uuid").asNative().isString()) {
                    String rawUUID = object.getChild("uuid").asString();
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
            JsonInstance element = JsonReader.read(response);
            if (element.isObjectType()) {
                JsonObject object = element.asObject();
                if (object.hasChild("id") && object.getChild("id").isNativeType() && object.getChild("id").asNative().isString()) {
                    String rawUUID = object.getChild("id").asString();
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

        JsonInstance element = null;
        try {
            element = JsonReader.read(PathUtilities.read(fetchCache));
        } catch (KsonException ignored) {}

        if (!(element instanceof JsonObject)) {
            element = JsonObject.newObject("", "");
        }

        JsonObject object = element.asObject();
        JsonObject clone = object.clone("", "", '.')
                .asObject();

        List<String> keys = new ArrayList<>(clone.getKeys(false));

        boolean write = false;
        for (String key : keys) {
            JsonArray array = clone.getChild(key).asArray();
            boolean modifications = false;

            List<JsonInstance> toRemove = new ArrayList<>();
            for (JsonInstance children : array) {
                JsonObject cacheItem = children.asObject();
                String uuid = cacheItem.getChild("uuid").asString();

                if (uuid.equals(toSimple(offline))) {
                    long expiration = cacheItem.getChild("expiration").asLong();
                    long now = System.currentTimeMillis();

                    if (expiration >= now) {
                        return fromSimple(key);
                    } else {
                        //elements.remove();
                        toRemove.add(element);
                        modifications = true;
                    }
                }
            }

            toRemove.forEach(array::remove);

            if (modifications) {
                object.put(key, array);
                write = true;
            }
        }

        if (write) {
            String raw = object.toString();
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

        JsonInstance element = null;
        try {
            element = JsonReader.read(PathUtilities.read(fetchCache));
        } catch (KsonException ignored) {}
        if (!(element instanceof JsonObject)) {
            element = JsonObject.newObject("", "");
        }

        JsonObject object = element.asObject();
        JsonArray array = JsonArray.newArray("", toSimple(online));
        if (object.hasChild(toSimple(online))) {
            array = object.getChild(toSimple(online)).asArray();
        }

        JsonObject cacheItem = JsonObject.newObject("", "");
        cacheItem.put("uuid", toSimple(offline));
        cacheItem.put("expiration", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(72));

        boolean write = true;
        JsonInstance existingEntry = null;
        for (JsonInstance children : array) {
            JsonObject childItem = children.asObject();
            String uuid = childItem.getChild("uuid").asString();

            if (uuid.equals(toSimple(offline))) {
                long expiration = childItem.getChild("expiration").asLong();
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
            object.put(toSimple(online), array);

            String raw = object.toString();
            try {
                Files.write(fetchCache, raw.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
