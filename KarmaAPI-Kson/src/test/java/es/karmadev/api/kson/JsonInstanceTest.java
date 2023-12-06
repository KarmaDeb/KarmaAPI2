package es.karmadev.api.kson;

import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.kson.object.JsonNull;
import org.junit.Test;

public class JsonInstanceTest {

    @Test
    public void testNewObject() {
        JsonObject object = JsonObject.newObject();
        object.put("test", JsonNative.forSequence("Hello world!"));
        object.put("test2", JsonNative.forSequence("How are you?"));

        JsonObject child = JsonObject.newObject();
        child.put("key1", JsonNull.get());
        child.put("key2", JsonNative.forNumber(Long.MAX_VALUE));
        child.put("key3", JsonNative.forBoolean(false));

        JsonArray array = JsonArray.newArray();
        JsonArray subArray = JsonArray.newArray();
        array.add("This is a element");
        array.add("This is another element");
        subArray.add(Byte.MAX_VALUE);
        subArray.add(Short.MAX_VALUE);
        subArray.add(Integer.MAX_VALUE);
        subArray.add(Long.MAX_VALUE);
        subArray.add(Float.MAX_VALUE);
        subArray.add(Double.MAX_VALUE);
        array.add(subArray);

        object.put("child", child);
        object.put("list", array);

        String value = object.toString(false);
        JsonInstance instance = JsonReader.read(value);

        System.out.println(instance);
    }
}