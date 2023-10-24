package es.karmadev.api.function;

@FunctionalInterface
public interface TriConsumer<A, B, C> {

    /**
     * Performs the operation on the
     * given arguments
     *
     * @param type1 the input argument 1
     * @param type2 the input argument 2
     * @param type3 the input argument 3
     */
    void accept(final A type1, final B type2, final C type3);

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after) {
        if (after == null) return (a, b, c) -> {};
        return (A a, B b, C c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }
}
