package tracker.trytka.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

public class BDict extends BValue<SortedMap<BString, BValue<?>>> {
    public BDict() {
        this.value = new TreeMap<>();
    }

    public BDict put(String key, Long bint) {
        this.value.put(new BString(key), new BInt(bint));
        return this;
    }

    public BDict put(String key, byte[] bstr) {
        this.value.put(new BString(key), new BString(bstr));
        return this;
    }

    public BDict put(String key, String bstr) {
        this.value.put(new BString(key), new BString(bstr));
        return this;
    }

    public BDict put(String key, BValue<?> bval) {
        this.value.put(new BString(key), bval);
        return this;
    }

    public BValue<?> get(String key) {
        return this.value.get(new BString(key));
    }

    @Override
    public String toString() {
        return this.toString(0);
    }

    public String toString(int indent) {
        var yaml = new StringBuilder();
        for (var key : this.value.keySet()) {
            for (int i = 0; i < indent; i++) {
                yaml.append(" ");
            }
            var bvalue = this.value.get(key);
            if (bvalue instanceof BInt || bvalue instanceof BString) {
                yaml.append(key.toString() + ": " + bvalue.toString() + "\n");
            } else if (bvalue instanceof BList blist) {
                yaml.append(key.toString() + ":\n" + blist.toString(indent));
            } else if (bvalue instanceof BDict bdict) {
                yaml.append(key.toString() + ":\n" + bdict.toString(indent + 4));
            }
        }
        return yaml.toString();
    }

    @Override
    public void encode(ByteArrayOutputStream out) {
        out.write((byte) 'd');
        for (var key : this.value.keySet()) {
            key.encode(out);
            this.value.get(key).encode(out);
        }
        out.write((byte) 'e');
    }

    public static BDict parseBDict(Decoder decoder) throws IOException {
        if (decoder.read() != 'd') {
            throw new IllegalArgumentException("malformed: expected 'd'");
        }
        var bdict = new BDict();
        byte peeked;
        while ((peeked = decoder.peek()) != 'e' && peeked != -1) {
            var key = BString.parseBString(decoder);
            bdict.value.put(key, decoder.parse());
        }
        if (peeked == -1) {
            throw new IOException("unexpected EOF");
        }
        if (decoder.read() != 'e') {
            throw new IllegalStateException("(unreachable) expected 'e'");
        }
        return bdict;
    }
}
