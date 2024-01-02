package es.karmadev.api.object.solver;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@link Resolvable} resolvers
 */
public final class Resolvers {

    private final static Set<Resolver<?>> resolvers = ConcurrentHashMap.newKeySet();

    /**
     * Create a new resolver
     *
     * @param type the resolver type
     * @return the resolver
     * @param <T> the resolver parameter type
     */
    public static <T> Resolver<T> createResolver(final Class<T> type) {
        if (type == null) throw new NullPointerException();

        return new Resolver<T>() {
            private final Set<Function<T, ?>> providers = new HashSet<>();
            private final Set<Class<?>> resolvers = new HashSet<>();

            @Override
            public boolean isType(final Class<?> parameterType) {
                return type.isAssignableFrom(parameterType) || parameterType.isAssignableFrom(type) ||
                        type.equals(parameterType);
            }

            @Override
            public T tryResolveParameter(final Object parameter) {
                if (parameter == null) return null;
                if (!isType(parameter.getClass())) return null;

                return type.cast(parameter);
            }

            /**
             * Set the resolver solver
             *
             * @param type     the type that the resolver resolves to
             * @param instance the instance returned by the
             *                 resolve method
             * @return the resolver
             */
            @Override
            public <A> Resolver<T> addSolving(final Class<A> type, final Function<T, A> instance) {
                if (instance == null) return this;
                resolvers.add(type);
                providers.add(instance);

                return this;
            }

            @Override
            public boolean resolvesTo(final Class<?> type) {
                return resolvers.contains(type);
            }

            @Override
            public <A> @Nullable A resolve(final T parameter, final Class<A> type) {
                if (!resolvers.contains(type)) return null;
                for (Function<T, ?> function : providers) {
                    Object value = function.apply(parameter);
                    if (value == null) continue;

                    Class<?> returnType = value.getClass();
                    if (returnType.isAssignableFrom(type) || type.isAssignableFrom(returnType)
                        || type.equals(returnType)) {

                        return type.cast(value);
                    }
                }

                return null;
            }

            @Override
            public void register() {
                Resolvers.resolvers.add(this);
            }
        };
    }

    /**
     * Resolves an element
     *
     * @param parameter the resolver parameter
     * @param type the resolved type
     * @return the resolved element
     * @param <T> the resolver parameter type
     * @param <A> the resolved type
     */
    @SuppressWarnings("unchecked")
    public static <T, A> A resolve(final T parameter, final Class<A> type) {
        for (Resolver<?> resolver : resolvers) {
            if (!resolver.isType(parameter.getClass())) continue;

            Resolver<T> resolverType = (Resolver<T>) resolver;
            if (resolver.resolvesTo(type)) {
                return resolverType.resolve(parameter, type);
            }
        }

        return null;
    }
}
