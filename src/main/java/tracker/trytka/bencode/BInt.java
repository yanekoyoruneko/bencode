package tracker.trytka.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BInt extends BValue<Long> implements Comparable<BValue<Long>> {
    public BInt() {
    }

    public BInt(long value) {
        this.value = value;
    }

    public BInt(int value) {
        this.value = (long)value;
    }

    @Override
    public int compareTo(BValue<Long> other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public void encode(ByteArrayOutputStream out) {
        var long_bytes = ("i" + this.value + "e").getBytes(Decoder.encoding);
        out.write(long_bytes, 0, long_bytes.length);
    }

    public static BInt parseBInt(Decoder decoder) throws IOException {
        if (decoder.read() != 'i') {
            throw new IllegalArgumentException("malformed: expected 'i'");
        }
        byte sign = decoder.peek();
        if (sign == '-')
            decoder.read();
        BInt bint = new BInt(decoder.parseUintUntil('e'));
        if (sign == '-')
            bint.value = -bint.value;
        if (decoder.read() != 'e') {
            throw new IllegalArgumentException("malformed: expected 'e'");
        }
        return bint;
    }
}
