package es.karmadev.api.minecraft.text.component;

import es.karmadev.api.minecraft.text.TextMessageType;
import es.karmadev.api.minecraft.text.component.message.AnimatedComponent;
import es.karmadev.api.minecraft.text.component.message.SimpleMessageComponent;
import es.karmadev.api.minecraft.text.component.title.Times;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a component builder
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SeqBuilder {

    private final TextMessageType sequenceType;
    private final ComponentBuilder builder;

    private final List<ComponentData> componentList = new ArrayList<>();

    @Setter@Accessors(fluent = true)
    private int repeats = 0;

    @Setter@Accessors(fluent = true)
    private Times interval = Times.exact(20);

    /**
     * Add a component to the sequence
     *
     * @param raw the raw component content
     * @return the sequence builder
     */
    public SeqBuilder component(final String raw) {
        Component component = new SimpleMessageComponent(raw, sequenceType);
        return component(component, 0);
    }

    /**
     * Add a component to the sequence
     *
     * @param raw the raw component content
     * @param repeats the amount of times to repeat
     *                the component
     * @return the sequence builder
     */
    public SeqBuilder component(final String raw, final int repeats) {
        Component component = new SimpleMessageComponent(raw, sequenceType);
        return component(component, repeats);
    }

    /**
     * Add a component to the sequence
     *
     * @param component the component
     * @return the sequence builder
     */
    public SeqBuilder component(final Component component) {
        return component(component, 0);
    }

    /**
     * Add a component to the sequence
     *
     * @param component the component
     * @param repeats the amount of times to repeat
     *                the component
     * @return the sequence builder
     */
    public SeqBuilder component(final Component component, final int repeats) {
        componentList.add(new ComponentData(component, repeats));
        return this;
    }

    /**
     * Add a component to the sequence
     *
     * @param raw the raw component content
     * @return the sequence builder
     */
    public SeqBuilder components(final String[] raw) {
        for (String str : raw) {
            Component component = new SimpleMessageComponent(str, sequenceType);
            component(component, 0);
        }

        return this;
    }

    /**
     * Add a component to the sequence
     *
     * @param raw the raw component content
     * @param repeats the amount of times to repeat
     *                the component
     * @return the sequence builder
     */
    public SeqBuilder components(final String[] raw, final int repeats) {
        for (String str : raw) {
            Component component = new SimpleMessageComponent(str, sequenceType);
            component(component, repeats);
        }

        return this;
    }

    /**
     * Add a component to the sequence
     *
     * @param components the components
     * @return the sequence builder
     */
    public SeqBuilder components(final Component[] components) {
        for (Component component : components) {
            component(component, 0);
        }

        return this;
    }

    /**
     * Add a component to the sequence
     *
     * @param components the component
     * @param repeats the amount of times to repeat
     *                the component
     * @return the sequence builder
     */
    public SeqBuilder components(final Component[] components, final int repeats) {
        for (Component component : components) {
            component(component, repeats);
        }

        return this;
    }

    /**
     * End the sequence
     *
     * @return the sequence builder
     */
    public ComponentBuilder sequenceEnd() {
        ComponentSequence sequence = new AnimatedComponent(repeats, interval, sequenceType);
        for (ComponentData data : componentList) {
            sequence.addSequence(data.component, data.repeats);
        }

        builder.sequenceComponents.add(sequence);
        return builder;
    }

    @AllArgsConstructor
    private static class ComponentData {

        private final Component component;
        private final int repeats;
    }
}
