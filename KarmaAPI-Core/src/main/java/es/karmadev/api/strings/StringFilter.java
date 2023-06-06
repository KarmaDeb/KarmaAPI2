package es.karmadev.api.strings;

/**
 *  String filter
 */
public interface StringFilter {


    /**
     * Tests whether or not the specified abstract sequence should be
     * included in a string list.
     *
     * @param  sequence  The abstract sequence to be tested
     * @return  {@code true} if and only if {@code sequence}
     *          should be included
     */
    boolean accept(final CharSequence sequence);
}
