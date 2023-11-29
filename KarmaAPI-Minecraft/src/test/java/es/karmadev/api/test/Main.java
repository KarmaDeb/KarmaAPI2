package es.karmadev.api.test;

import es.karmadev.api.minecraft.text.TextMessageType;

import es.karmadev.api.minecraft.text.component.Component;
import es.karmadev.api.minecraft.text.component.ComponentBuilder;

public class Main {

    public static void main(String[] args) {
        Component[] components = ComponentBuilder.builder().sequenceStart(TextMessageType.TITLE)
                .components(ComponentBuilder.builder()
                        .title("Title").build(TextMessageType.TIMES))
                .sequenceEnd()
                .sequenceStart(TextMessageType.SUBTITLE).components(
                        Component.builder().subtitle("Subtitle").build(TextMessageType.TIMES)
                ).sequenceEnd().build();

        for (Component component : components) {
            System.out.println(component.toJson(true));
        }
    }
}
