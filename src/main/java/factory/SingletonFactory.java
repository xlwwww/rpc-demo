package factory;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {
    private static ConcurrentHashMap<String, Object> singletonMaps = new ConcurrentHashMap<>();

    public static <T> T getInstance(Class<T> clazz) {
        String s = clazz.toString();
        if (singletonMaps.containsKey(s)) {
            return (T) singletonMaps.get(s);
        }
        try {
            singletonMaps.put(s, clazz.newInstance());
            return (T) singletonMaps.get(s);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
