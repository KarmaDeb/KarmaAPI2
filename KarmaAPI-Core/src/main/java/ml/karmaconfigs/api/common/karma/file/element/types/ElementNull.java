package ml.karmaconfigs.api.common.karma.file.element.types;

import ml.karmaconfigs.api.common.karma.file.element.types.primitive.PrimitiveType;

/**
 * @deprecated KarmaMain support has been dropped
 */
@Deprecated
public interface ElementNull extends PrimitiveType<Object> {

    /**
     * Get the primitive type
     *
     * @return the primitive type
     */
    @Override
    default ObjectType type() {
        return ObjectType.NULL;
    }
}
