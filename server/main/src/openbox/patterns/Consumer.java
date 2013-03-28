package openbox.patterns;

@SuppressWarnings({"UnusedDeclaration"})
public interface Consumer<T,E extends Exception> {

    void put(T value) throws E;
}