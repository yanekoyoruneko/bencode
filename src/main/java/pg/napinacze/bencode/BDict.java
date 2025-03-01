package pg.napinacze.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

public class BDict extends BValue<SortedMap<BString, BValue<?>>> {
    public BDict() {
        this.value = new TreeMap<>();
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
            } else {
                assert true : "unreachable";
            }
        }
        return yaml.toString();
    }

    @Override
    public byte[] toBytes() throws IOException {
        var buf = new ByteArrayOutputStream();
        buf.write('d');
        for (var key : this.value.keySet()) {
            buf.write(key.toBytes());
            buf.write(this.value.get(key).toBytes());
        }
        buf.write('e');
        return buf.toByteArray();
    }

    public static BDict parseBDict(Decoder decoder) throws IOException {
        if (decoder.read() != 'd') {
            throw new IllegalArgumentException("BDict: invalid format: expected 'd'");
        }
        var bdict = new BDict();
        byte peeked;
        while ((peeked = decoder.peek()) != 'e' && peeked != -1) {
            var key = BString.parseBString(decoder);
            bdict.value.put(key, decoder.parse());
        }
        if (peeked == -1) {
            throw new IOException("BDict: unexpected EOF");
        }
        if (decoder.read() != 'e') {
            throw new IOException("(unreachable) BDict: invalid format: expected 'e'");
        }
        return bdict;
    }
}
