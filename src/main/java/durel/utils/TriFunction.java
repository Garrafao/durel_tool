package durel.utils;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    void apply(T t, U u, V v);
}