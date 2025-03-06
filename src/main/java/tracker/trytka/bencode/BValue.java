package tracker.trytka.bencode;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public abstract class BValue<T> {
  protected T value;

  @Override
  public boolean equals(Object other) {
    if (other instanceof BValue otherval) return this.value.equals(otherval.value);
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.value);
  }

  public BValue() {}

  public BValue(T value) {
    this.value = value;
  }

  public abstract String toString();

  public final byte[] encode() {
    var out = new ByteArrayOutputStream();
    encode(out);
    return out.toByteArray();
  }

  protected abstract void encode(ByteArrayOutputStream out);

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }
}
