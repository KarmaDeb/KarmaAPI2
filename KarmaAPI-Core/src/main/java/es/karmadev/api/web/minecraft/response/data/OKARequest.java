package es.karmadev.api.web.minecraft.response.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import es.karmadev.api.web.minecraft.UUIDType;
import es.karmadev.api.web.minecraft.response.JsonContainer;
import es.karmadev.api.web.minecraft.response.data.component.CapeComponent;
import es.karmadev.api.web.minecraft.response.data.component.SkinComponent;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.UUID;

/**
 * Online KarmaAPI request
 */
@Builder
public class OKARequest implements JsonContainer {

    @Nullable @Getter
    URI uri;

    @Getter
    long id;

    @Nullable @Getter
    String nick;

    @Nullable @Getter
    String creation;

    @Nullable
    UUID offline, online;

    @Getter @NonNull @Setter
    SkinComponent skin;

    @Getter @NonNull
    CapeComponent cape;

    @NonNull
    String json;

    /**
     * Parse the response to json
     *
     * @param pretty prettify the output
     * @return the json response
     */
    @Override
    public String toJson(final boolean pretty) {
        if (pretty) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create(); //Not compatible
                JsonElement element = gson.fromJson(json, JsonElement.class);

                return gson.toJson(element);
            } catch (Throwable ignored) {}
        }

        return json;
    }

    /**
     * Get the UUID of the oka request
     *
     * @param type the uuid type
     * @return the uuid
     */
    public UUID getUUID(final UUIDType type) {
        switch (type) {
            case ONLINE:
                return online;
            case OFFLINE:
            default:
                return offline;
        }
    }

    /**
     * Build an empty request
     *
     * @return the empty request
     */
    public static OKARequest empty() {
        return empty("{}");
    }

    /**
     * Build an empty request
     *
     * @param json the json request
     * @return the empty request
     */
    public static OKARequest empty(final String json) {
        return OKARequest.builder().skin(SkinComponent.empty()).cape(CapeComponent.empty()).json((json != null ? json : "{}")).build();
    }
}
