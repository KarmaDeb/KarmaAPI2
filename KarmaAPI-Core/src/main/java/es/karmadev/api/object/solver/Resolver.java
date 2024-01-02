package es.karmadev.api.object.solver;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents a resolver
 */
public interface Resolver<T> {

    /**
     * Get if the resolver parameter
     * is of the provided one
     *
     * @param parameterType the parameter type
     * @return if the resolver is of the provided
     * parameter type
     */
    boolean isType(final Class<?> parameterType);

    /**
     * Tries to resolve the parameter object
     * to make it of the type of the
     * resolver
     *
     * @param parameter the parameter
     * @return the resolved parameter
     */
    @Nullable
    T tryResolveParameter(final Object parameter);

    /**
     * Set the resolver solver
     *
     * @param type the type that the resolver resolves to
     * @param instance the instance returned by the
     *                 resolve method
     * @param <A> the resolved type
     * @return the resolver
     */
    <A> Resolver<T> addSolving(Class<A> type, Function<T, A> instance);

    /**
     * Get if the resolver resolves to the
     * specified element
     *
     * @param type the element
     * @return if the resolver resolves
     */
    boolean resolvesTo(final Class<?> type);

    /**
     * Resolve the element
     *
     * @param parameter the parameter
     * @param type the resolved type
     * @return the resolved element
     * @param <A> the resolved type
     */
    @Nullable
    <A> A resolve(final T parameter, final Class<A> type);

    /**
     * Register the resolver
     */
    void register();
}
