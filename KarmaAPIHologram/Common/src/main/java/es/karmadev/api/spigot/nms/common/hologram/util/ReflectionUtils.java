package es.karmadev.api.spigot.nms.common.hologram.util;

import es.karmadev.api.core.ExceptionCollector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings("unused")
public class ReflectionUtils {
    private static Method getStackTraceElementMethod;

    private static Method getStackTraceDepthMethod;

    private static boolean stackTraceErrorPrinted;

    @SuppressWarnings("unchecked")
    public static void putInPrivateStaticMap(Class<?> clazz, String fieldName, Object key, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Map<Object, Object> map = (Map<Object, Object>) field.get(null);
        map.put(key, value);
    }

    public static void setPrivateField(Class<?> clazz, Object handle, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(handle, value);
    }

    public static Object getPrivateField(Class<?> clazz, Object handle, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(handle);
    }

    public static StackTraceElement getStackTraceElement(int index) {
        try {
            if (getStackTraceElementMethod == null) {
                getStackTraceElementMethod = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
                getStackTraceElementMethod.setAccessible(true);
            }
            if (getStackTraceDepthMethod == null) {
                getStackTraceDepthMethod = Throwable.class.getDeclaredMethod("getStackTraceDepth");
                getStackTraceDepthMethod.setAccessible(true);
            }
            Throwable dummyThrowable = new Throwable();
            int depth = (Integer) getStackTraceDepthMethod.invoke(dummyThrowable, new Object[0]);
            if (index < depth)
                return (StackTraceElement)getStackTraceElementMethod.invoke(new Throwable(), new Object[] {index});
            return null;
        } catch (Throwable t) {
            if (!stackTraceErrorPrinted) {
                ExceptionCollector.catchException(ReflectionUtils.class, t);
                stackTraceErrorPrinted = true;
            }
            return null;
        }
    }
}
