package es.karmadev.api.minecraft.text;

import es.karmadev.api.minecraft.text.component.AnimationComponent;
import es.karmadev.api.minecraft.text.component.TextComponent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Animated text component
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AnimatedText implements AnimationComponent {

    private final List<TextComponent> sequences = new CopyOnWriteArrayList<>();
    private final int repeats;
    private final long interval;

    private int currentIndex;
    private int repeatCount;
    private AnimationComponent currentAnimationSequence;

    /**
     * Insert a sequence into the animation
     *
     * @param index     the sequence index
     * @param component the sequence to add
     */
    @Override
    public void insertSequence(final int index, final TextComponent component) {
        if (index >= sequences.size()) {
            sequences.add(component);
            return;
        }

        TextComponent current = sequences.get(index);
        int iteratorIndex = index;
        while (current != null) {
            current = sequences.get(++iteratorIndex);
        }

        if (iteratorIndex != index) {
            for (int i = (iteratorIndex + 1); i > (index + 1); i--) {
                TextComponent previous = sequences.remove(i - 1);
                sequences.set(i, previous);
            }
        }

        sequences.set(index, component);
    }

    /**
     * Set a sequence of the animation
     *
     * @param index     the sequence index
     * @param component the sequence to add
     */
    @Override
    public void setSequence(final int index, final TextComponent component) {
        if (component == null) {
            removeSequence(index);
            return;
        }

        if (index >= sequences.size()) {
            sequences.add(component);
            return;
        }

        sequences.set(index, component);
    }

    /**
     * Add a sequence to the animation
     *
     * @param component the sequence to add
     */
    @Override
    public void addSequence(final TextComponent component) {
        sequences.add(component);
    }

    /**
     * Remove a sequence from the animation
     *
     * @param index the sequence index
     * @return the removed sequence or null
     */
    @Override
    public TextComponent removeSequence(final int index) {
        return sequences.remove(index);
    }

    /**
     * Remove a sequence
     *
     * @param component the component to remove
     * @return if the component was removed
     */
    @Override
    public boolean removeSequence(final TextComponent component) {
        if (component == null) return false;
        return sequences.remove(component);
    }

    /**
     * Remove all the sequences matching the content
     *
     * @param content the content
     * @return the removed sequences
     */
    @Override
    public TextComponent[] removeSequences(final String content) {
        if (content == null) return new TextComponent[0];
        List<TextComponent> removed = new ArrayList<>();

        Iterator<TextComponent> components = sequences.iterator();

        while (components.hasNext()) {
            TextComponent component = components.next();
            if (component.getRaw() == null) {
                components.remove();
                continue;
                /*
                As we removed it because its content is null, we didn't
                actually remove it because of content, so we won't add it
                to the list
                 */
            }

            if (component.getRaw().equals(content)) {
                components.remove();
                removed.add(component);
            }
        }

        return removed.toArray(new TextComponent[0]);
    }

    /**
     * Get the sequence at the specified index
     *
     * @param index the sequence index
     * @return the sequence or null
     */
    @Override
    public TextComponent getSequence(final int index) {
        return sequences.get(index);
    }

    /**
     * Get the amount of times to repeat the
     * text animation
     *
     * @return the amount of times to repeat
     */
    @Override
    public int getRepeats() {
        return repeats;
    }

    /**
     * Get the current sequence
     *
     * @return the current sequence
     */
    @Override
    public int getSequenceIndex() {
        return currentIndex;
    }

    /**
     * Get the maximum amount of sequences
     *
     * @return the maximum amount of
     * sequences
     */
    @Override
    public int getMaxSequenceIndex() {
        return sequences.size();
    }

    /**
     * Get the animation play interval
     * speed
     *
     * @return the play speed
     */
    @Override
    public long getInterval() {
        return interval;
    }

    /**
     * Get if the animation has finished
     *
     * @return if the animation finished
     */
    @Override
    public boolean isFinished() {
        return repeatCount == repeats;
    }

    /**
     * Get if the animated text component is similar to
     * the other component
     *
     * @param other the other component
     * @return if the components are similar
     */
    @Override
    public boolean isSimilar(final AnimationComponent other) {
        return false;
    }

    /**
     * Get the raw text
     *
     * @return the raw text
     */
    @Override
    public String getRaw() {
        if (currentAnimationSequence != null) {
            if (currentAnimationSequence.isFinished()) {
                currentAnimationSequence = null;
            } else {
                return currentAnimationSequence.getRaw();
            }
        }

        if ((currentIndex + 1) > sequences.size()) {
            currentIndex = 0;
            repeatCount = Math.min(repeats, ++repeatCount);

            if (repeats > -1 && repeatCount == repeats) {
                return null;
            }
        }

        TextComponent component = sequences.get(currentIndex);
        if (component instanceof AnimationComponent) {
            currentAnimationSequence = (AnimationComponent) component;
        }

        currentIndex++;
        return component.getRaw();
    }

    /**
     * Converse the component into a json
     * string
     *
     * @return the json component
     */
    @Override
    public String toJson() {
        return null;
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("AnimatedText{");

        int index = 0;
        for (TextComponent component : sequences) {
            if (!(component instanceof AnimationComponent)) {
                builder.append("Sequence{").append("\"").append(component.getRaw()).append("\"").append("}");
            } else {
                builder.append("Sequence{").append(component).append("}");
            }

            if (index++ < sequences.size() - 1) {
                builder.append(",");
            }
        }

        return builder.append("}").toString();
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link Object#equals(Object)}
     *     method, then calling the {@code hashCode} method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class {@code Object} does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java&trade; programming language.)
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        int sumCode = 0;

        for (TextComponent sequence : sequences) {
            sumCode += sequence.hashCode();
        }

        if (sumCode <= 0) return super.hashCode();
        return sumCode;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param object the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     */
    @SuppressWarnings("SlowListContainsAll")
    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (!(object instanceof AnimatedText)) return false;

        AnimatedText animatedText = (AnimatedText) object;
        return animatedText.sequences.containsAll(sequences)
                && interval == animatedText.interval
                && repeats == animatedText.repeats
                && repeatCount == animatedText.repeatCount
                && currentIndex == animatedText.currentIndex;
    }
}
