package es.karmadev.api.database.model.json.query;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GlobalTypeValue<T> {

    private final T value;
    private final Class<? extends T> type;

    public static <T> GlobalTypeValue<T> of(final T value, final Class<? extends T> type) {
        return new GlobalTypeValue<T>(value, type);
    }
}
