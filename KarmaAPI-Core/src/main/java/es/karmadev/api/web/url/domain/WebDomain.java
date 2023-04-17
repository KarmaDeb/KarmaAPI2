package es.karmadev.api.web.url.domain;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

/**
 * Web domain
 */
@Accessors(fluent = true)
@Value(staticConstructor = "of")
@AllArgsConstructor(staticName = "of")
public class WebDomain {

    @NonNull
    String protocol;
    @NonNull
    SubDomain sub;
    @NonNull
    String root;
    @Nullable
    String tld;

    /**
     * Build a string URL using the web
     * domain data
     *
     * @return the URL string
     */
    public String build() {
        String sub = this.sub.toString();
        if (sub.isEmpty()) {
            return protocol + "://" + root + "." + tld;
        } else {
            return protocol + "://" + sub + "." + root + "." + tld;
        }
    }
}
