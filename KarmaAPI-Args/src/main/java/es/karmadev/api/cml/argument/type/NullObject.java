package es.karmadev.api.cml.argument.type;

import lombok.AccessLevel;
import lombok.Getter;

public final class NullObject {

    @SuppressWarnings("InstantiationOfUtilityClass") @Getter(value = AccessLevel.PACKAGE)
    private final static NullObject instance = new NullObject();

    /**
     * Prevent null object creation
     */
    private NullObject() {}
}
