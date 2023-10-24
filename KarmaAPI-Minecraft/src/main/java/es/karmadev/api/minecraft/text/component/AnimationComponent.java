package es.karmadev.api.minecraft.text.component;

import es.karmadev.api.minecraft.text.Component;

/**
 * Animated text component
 */
public interface AnimationComponent extends TextComponent {

    /**
     * Insert a sequence into the animation
     *
     * @param index the sequence index
     * @param component the sequence to add
     */
    void insertSequence(final int index, final TextComponent component);

    /**
     * Insert a sequence into the animation
     *
     * @param index the sequence index
     * @param raw the text to add
     */
    default void insertSequence(final int index, final String raw) {
        insertSequence(index, Component.simple().text(raw).build());
    }

    /**
     * Set a sequence of the animation
     *
     * @param index the sequence index
     * @param component the sequence to add
     */
    void setSequence(final int index, final TextComponent component);

    /**
     * Set a sequence of the animation
     *
     * @param index the sequence index
     * @param raw the text to add
     */
    default void setSequence(final int index, final String raw) {
        setSequence(index, Component.simple().text(raw).build());
    }

    /**
     * Add a sequence to the animation
     *
     * @param component the sequence to add
     */
    void addSequence(final TextComponent component);

    /**
     * Add a sequence to the animation
     *
     * @param raw the text to add
     */
    default void addSequence(final String raw) {
        addSequence(Component.simple().text(raw).build());
    }

    /**
     * Remove a sequence from the animation
     *
     * @param index the sequence index
     * @return the removed sequence or null
     */
    TextComponent removeSequence(final int index);

    /**
     * Remove a sequence
     *
     * @param component the component to remove
     * @return if the component was removed
     */
    boolean removeSequence(final TextComponent component);

    /**
     * Remove all the sequences matching the content
     *
     * @param content the content
     * @return the removed sequences
     */
    TextComponent[] removeSequences(final String content);

    /**
     * Get the sequence at the specified index
     *
     * @param index the sequence index
     * @return the sequence or null
     */
    TextComponent getSequence(final int index);

    /**
     * Get the current sequence
     *
     * @return the current sequence
     */
    default TextComponent getCurrentSequence() {
        return getSequence(getSequenceIndex());
    }

    /**
     * Get the amount of times to repeat the
     * text animation
     *
     * @return the amount of times to repeat
     */
    int getRepeats();

    /**
     * Get the current sequence
     *
     * @return the current sequence
     */
    int getSequenceIndex();

    /**
     * Get the maximum amount of sequences
     *
     * @return the maximum amount of
     * sequences
     */
    int getMaxSequenceIndex();

    /**
     * Get the animation play interval
     * speed
     *
     * @return the play speed
     */
    long getInterval();

    /**
     * Get if the animation has finished
     *
     * @return if the animation finished
     */
    boolean isFinished();

    /**
     * Get if the animated text component is similar to
     * the other component
     *
     * @param other the other component
     * @return if the components are similar
     */
    boolean isSimilar(final AnimationComponent other);
}
