package es.karmadev.api.collect.container;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SimpleContainerTest {

    @Test
    public void testNewContainer() {
        SimpleContainer<String> container = new SimpleContainer<>(20, String.class);
        assertEquals(container.getMaxCapacity(), 20);
    }

    @Test
    public void testAddItem() {
        SimpleContainer<String> container = new SimpleContainer<>(1, String.class);
        container.addItem("Hello world!");

        assertEquals(container.getItems(new String[0])[0], "Hello world!");
    }
}