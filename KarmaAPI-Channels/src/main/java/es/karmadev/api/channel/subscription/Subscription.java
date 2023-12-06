package es.karmadev.api.channel.subscription;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a subscription
 * to an event
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscription {

    /**
     * Get the subscription priority
     *
     * @return the priority of this
     * subscription
     */
    int priority() default 0;

    /**
     * Get if the subscription ignores
     * the cancelled events
     *
     * @return if the subscription ignores
     * the cancelled events
     */
    boolean ignoreCancelled() default false;
}
