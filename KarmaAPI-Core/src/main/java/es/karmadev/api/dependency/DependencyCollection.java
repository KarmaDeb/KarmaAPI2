package es.karmadev.api.dependency;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a collection of dependencies
 */
public class DependencyCollection {

    private final List<Dependency> dependencies = new ArrayList<>();

    private DependencyCollection(final List<Dependency> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    /**
     * Process the dependency collection
     *
     * @param consumer the dependency consumer
     */
    public void process(final Consumer<Dependency> consumer) {
        dependencies.forEach(consumer);
    }

    /**
     * Wrap a collection of dependencies into a
     * dependency collection instance. Why?
     * The dependency collection will sort the dependencies
     * based on priority and requires
     *
     * @param dependencies the dependencies
     * @return the dependency collection
     */
    public static DependencyCollection wrap(final Collection<Dependency> dependencies) {
        List<Dependency> list = new ArrayList<>(dependencies);
        list.sort(Comparator.comparing(Dependency::getPriority));

        return new DependencyCollection(list);
    }
}
