package es.karmadev.api.logger.log.file.component;

/**
 * Que pair
 *
 * @param <A> the que pair type a
 * @param <B> the que pair type b
 * @param <C> the que pair type c
 */
public final class QuePair<A, B, C> {

    private final A typeA;
    private final B typeB;
    private final C typeC;

    /**
     * Create a new que pair
     *
     * @param typeA the pair type a
     * @param typeB the pair type b
     * @param typeC the pair type c
     */
    QuePair(final A typeA, final B typeB, final C typeC) {
        this.typeA = typeA;
        this.typeB = typeB;
        this.typeC = typeC;
    }

    /**
     * Get the first pair type
     *
     * @return the first pair type object
     */
    public A getFirst() {
        return typeA;
    }

    /**
     * Get the second pair type
     *
     * @return the second pair type object
     */
    public B getSecond() {
        return typeB;
    }

    /**
     * Get the third pair type
     *
     * @return the third pair type object
     */
    public C getThird() {
        return typeC;
    }

    /**
     * Build a new que pair
     *
     * @param typeA the que type a
     * @param typeB the que type b
     * @param typeC the que type c
     * @return the que pair
     * @param <A> the type A object
     * @param <B> the type B object
     * @param <C> the type C object
     */
    public static <A, B, C> QuePair<A, B, C> build(final A typeA, final B typeB, final C typeC) {
        return new QuePair<>(typeA, typeB, typeC);
    }
}
