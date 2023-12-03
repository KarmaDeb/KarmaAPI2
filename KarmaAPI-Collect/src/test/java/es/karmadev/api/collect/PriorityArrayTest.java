package es.karmadev.api.collect;

import es.karmadev.api.collect.priority.PriorityArray;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PriorityArrayTest {

    @Test
    public void testAddElement() {
        PriorityArray<String> array = new PriorityArray<>(String.class);
        array.add("Hello world!");

        assertEquals(array.size(), 1);
        assertEquals(array.get(0), "Hello world!");
    }

    @Test
    public void testAddRemoveElement() {
        int expectedSize = 1;
        PriorityArray<String> array = new PriorityArray<>(String.class);
        array.add("Hello world");
        if (array.remove("Hello world")) {
            expectedSize -= 1;
        }

        assertEquals(array.size(), expectedSize);
    }

    @Test
    public static void testGetPriority() {
        PriorityArray<String> array = new PriorityArray<>(String.class);
        array.add("Hello world!");
        array.add("A", 5);
        array.add("B", 10);
        array.add("C", 2);
        array.add("D", 0);

        assertEquals(array.getPriority("Hello world!"), 0d);
        assertEquals(array.getPriority("A"), 5d);
        assertEquals(array.getPriority("B"), 10d);
        assertEquals(array.getPriority("C"), 2d);
        assertEquals(array.getPriority("D"), 0d);
    }

    @Test
    public static void testSort() {
        PriorityArray<String> array = new PriorityArray<>(String.class);
        array.add("Hello world!");
        array.add("A", 5);
        array.add("B", 10);
        array.add("C", 2);
        array.add("D", 0);

        String[] sorted = array.sorted(new String[0]);
        String[] expected = new String[]{"B", "A", "C", "Hello world!", "D"};

        assertEquals(sorted, expected);
    }
}