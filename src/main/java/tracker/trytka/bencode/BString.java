package tracker.trytka.bencode;

import static java.lang.Math.toIntExact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Objects;

public class BString extends BValue<byte[]> implements Comparable<BValue<byte[]>> {
  public BString() {
    super();
  }

  public BString(String string) {
    super(string.getBytes(Decoder.encoding));
  }

  public BString(byte[] value) {
    super(value);
  }

  public String getStringValue() {
    return new String(this.value, Decoder.encoding);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(this.value));
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return Arrays.equals(this.value, ((BString) other).getValue());
  }

  @Override
  public int compareTo(BValue<byte[]> other) {
    return new String(this.value).compareTo(new String(other.value));
  }

  @Override
  public String toString() {
    if (this.value.length > 1024) {
      return "<" + this.value.length + ":BLOB>";
    }
    return new String(this.value);
  }

  @Override
  public void encode(ByteArrayOutputStream out) {
    var long_bytes = Long.toString(this.value.length).getBytes(Decoder.encoding);
    out.write(long_bytes, 0, long_bytes.length);
    out.write((byte) ':');
    out.write(this.value, 0, this.value.length);
  }

  public static BString parseBString(Decoder decoder) throws IOException {
    int length;
    try {
      length = toIntExact(decoder.parseUintUntil(':'));
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("Tagged string size exceeds 32 bits");
    }

    if (decoder.read() != ':') {
      throw new IllegalArgumentException("Malformed input: Expected ':'");
    }

    byte[] buf = new byte[length];
    try {
      decoder.read(buf);
    } catch (BufferUnderflowException e) {
      throw new IOException("Unexpected EOF");
    }
    return new BString(buf);
  }
}
