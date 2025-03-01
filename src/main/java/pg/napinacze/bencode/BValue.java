package pg.napinacze.bencode;

abstract class BValue<T> {
    protected T value;

    @Override
    public boolean equals(Object other) {
        if (other instanceof BValue otherval)
            return this.value.equals(otherval.value);
        return false;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.value);
    }

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
