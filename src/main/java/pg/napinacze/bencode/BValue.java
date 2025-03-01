package pg.napinacze.bencode;

import java.io.IOException;

abstract class BValue<T> {
    protected T value;

    public BValue() {
    }

    public BValue(T value) {
        this.value = value;
    }

    abstract public String YAML();

    abstract public String toString();

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
