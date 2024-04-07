package bridge.asm;

public interface Linked<V> {
    V getDelegate();
    <T extends V> T setDelegate(T value);


    V getParent();
    <T extends V> T setParent(T value);
}
