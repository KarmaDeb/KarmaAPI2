package es.karmadev.api.cml.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a class argument
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ClassArgument {

    /**
     * The argument name
     *
     * @return the argument name
     */
    String name();

    /**
     * The argument description
     *
     * @return the argument description
     */
    String description() default "[unknown-argument-description]";

    /**
     * The argument help
     *
     * @return the help information
     */
    String help() default "No help provided";

    /**
     * The argument switch status
     *
     * @return the switch status
     */
    boolean isSwitch() default false;
}
