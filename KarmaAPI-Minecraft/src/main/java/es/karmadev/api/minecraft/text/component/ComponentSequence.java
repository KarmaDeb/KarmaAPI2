package es.karmadev.api.minecraft.text.component;

import es.karmadev.api.minecraft.text.component.message.SimpleMessageComponent;
import es.karmadev.api.minecraft.text.component.message.chat.ChatMessageComponent;

/**
 * Animated text component
 */
public interface ComponentSequence extends Component {

    /**
     * Insert a sequence into the animation
     *
     * @param index the sequence index
     * @param component the sequence to add
     */
    void insertSequence(final int index, final Component component);

    /**
     * Insert a sequence into the animation
     *
     * @param index the sequence index
     * @param raw the text to add
     */
    default void insertSequence(final int index, final String raw) {
        insertSequence(index, new SimpleMessageComponent(raw, getType()));
    }

    /**
     * Insert a sequence into the animation
     *
     * @param index the sequence index
     * @param component the sequence to add
     * @param repeats the amount to repeat the
     *                content
     */
    default void insertSequence(final int index, final Component component, final int repeats) {
        for (int i = 0; i < Math.max(1, repeats); i++) {
            insertSequence(index + i, component);
        }
    }

    /**
     * Insert a sequence into the animation
     *
     * @param index the sequence index
     * @param raw the text to add
     * @param repeats the amount to repeat the
     *                content
     */
    default void insertSequence(final int index, final String raw, final int repeats) {
        Component component = new SimpleMessageComponent(raw, getType());

        for (int i = 0; i < Math.max(1, repeats); i++) {
            insertSequence(index + i, component);
        }
    }

    /**
     * Set a sequence of the animation
     *
     * @param index the sequence index
     * @param component the sequence to add
     */
    void setSequence(final int index, final Component component);

    /**
     * Set a sequence of the animation
     *
     * @param index the sequence index
     * @param raw the text to add
     */
    default void setSequence(final int index, final String raw) {
        setSequence(index, new SimpleMessageComponent(raw, getType()));
    }

    /**
     * Set a sequence of the animation
     *
     * @param index the sequence index
     * @param component the sequence to add
     * @param repeats the amount to repeat the
     *                content
     */
    default void setSequence(final int index, final Component component, final int repeats) {
        for (int i = 0; i < Math.max(1, repeats); i++) {
            setSequence(index + i, component);
        }
    }

    /**
     * Set a sequence of the animation
     *
     * @param index the sequence index
     * @param raw the text to add
     * @param repeats the amount to repeat the
     *                content
     */
    default void setSequence(final int index, final String raw, final int repeats) {
        Component component = new SimpleMessageComponent(raw, getType());

        for (int i = 0; i < Math.max(1, repeats); i++) {
            setSequence(index + i, component);
        }
    }

    /**
     * Add a sequence to the animation
     *
     * @param component the sequence to add
     */
    void addSequence(final Component component);

    /**
     * Add a sequence to the animation
     *
     * @param raw the text to add
     */
    default void addSequence(final String raw) {
        addSequence(new SimpleMessageComponent(raw, getType()));
    }

    /**
     * Add a sequence to the animation
     *
     * @param component the sequence to add
     * @param repeats the amount to repeat the
     *                content
     */
    default void addSequence(final Component component, final int repeats) {
        for (int i = 0; i < Math.max(1, repeats); i++) {
            addSequence(component);
        }
    }

    /**
     * Add a sequence to the animation
     *
     * @param raw the text to add
     * @param repeats the amount to repeat the
     *                content
     */
    default void addSequence(final String raw, final int repeats) {
        Component component = new SimpleMessageComponent(raw, getType());
        for (int i = 0; i < Math.max(1, repeats); i++) {
            addSequence(component);
        }
    }

    /**
     * Remove a sequence from the animation
     *
     * @param index the sequence index
     * @return the removed sequence or null
     */
    Component removeSequence(final int index);

    /**
     * Remove a sequence
     *
     * @param component the component to remove
     * @return if the component was removed
     */
    boolean removeSequence(final Component component);

    /**
     * Remove all the sequences matching the content
     *
     * @param content the content
     * @return the removed sequences
     */
    Component[] removeSequences(final String content);

    /**
     * Get the sequence at the specified index
     *
     * @param index the sequence index
     * @return the sequence or null
     */
    Component getSequence(final int index);

    /**
     * Get the next sequence
     *
     * @return the next sequence
     */
    Component next();

    /**
     * Get the current sequence
     *
     * @return the current sequence
     */
    default Component getCurrentSequence() {
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
    boolean isSimilar(final ComponentSequence other);
}
