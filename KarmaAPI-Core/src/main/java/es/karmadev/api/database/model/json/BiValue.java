package es.karmadev.api.database.model.json;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
@Getter
public class BiValue<T> {

    @NonNull
    T value;

    @NonNull
    T subValue;
}
