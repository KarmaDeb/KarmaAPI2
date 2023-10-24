package es.karmadev.api.web.minecraft.response;

/**
 * Json response container
 */
@SuppressWarnings("unused") @Deprecated
public interface JsonContainer {

    /**
     * Parse the response to json
     *
     * @param pretty prettify the output
     * @return the json response
     */
    String toJson(final boolean pretty);
}
