package pg.napinacze.bencode;

import java.io.IOException;
import java.nio.BufferUnderflowException;

import static java.lang.Math.toIntExact;

public class BString extends BValue<byte[]> implements Comparable<BValue<byte[]>> {
    public BString() {
        super();
    }

    public BString(byte[] value) {
        super(value);
    }

    @Override
    public int compareTo(BValue<byte[]> other) {
        return new String(this.value).compareTo(new String(other.value));
    }

    @Override
    public String YAML() {
        if (this.value.length > 1024) {
            return "<" + this.value.length + ":BLOB>";
        }
        return new String(this.value);
    }

    @Override
    public String toString() {
        return this.value.length + ":" + new String(this.value);
    }

    public static BString parseBString(Decoder decoder) throws IOException {
        int length;
        try {
            length = toIntExact(decoder.parseUintUntil(':'));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("BString.parse: string size to big");
        }

        if (decoder.read() != ':') {
            throw new IllegalArgumentException("BString.parse: invalid format: expected ':'");
        }

        byte[] buf = new byte[length];
        try {
            decoder.read(buf);
        } catch (BufferUnderflowException e) {
            throw new IOException("BString.parse: unexpected EOF");
        }
        return new BString(buf);
    }
}
