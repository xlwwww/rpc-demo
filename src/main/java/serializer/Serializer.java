package serializer;

public interface Serializer {
    <T> byte[] serialize(T object);

    <T> Object deserialize(byte[] bytes, Class<T> clazz);
}
