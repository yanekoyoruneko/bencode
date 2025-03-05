package tracker.trytka.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BList extends BValue<ArrayList<BValue<?>>> {
    public BList() {
        this.value = new ArrayList<>();
    }

    public BList(ArrayList<BValue<?>> value) {
        super(value);
    }

    public BList add(Long bint) {
        this.value.add(new BInt(bint));
        return this;
    }

    public BList add(byte[] bstr) {
        this.value.add(new BString(bstr));
        return this;
    }

    public BList add(String bstr) {
        this.value.add(new BString(bstr));
        return this;
    }

    public BList add(String key, BValue<?> bval) {
        this.value.add(bval);
        return this;
    }

    @Override
    public String toString() {
        return toString();
    }

    public String toString(int indent) {
        var yaml = new StringBuffer();
        for (var el : this.value) {
            for (int i = 0; i < indent; i++) {
                yaml.append(" ");
            }
            yaml.append("- ");
            if (el instanceof BInt || el instanceof BString) {
                yaml.append(el.toString() + "\n");
            } else if (el instanceof BList blist) {
                yaml.append("\n" + blist.toString(indent));
            } else if (el instanceof BDict bdict) {
                yaml.append("\n" + bdict.toString(indent + 4));
            }
        }
        return yaml.toString();
    }

    @Override
    public void encode(ByteArrayOutputStream out) {
        out.write((byte) 'l');
        for (var el : this.value) {
            el.encode(out);
        }
        out.write((byte) 'e');
    }

    public static BList parseBList(Decoder decoder) throws IOException {
        if (decoder.read() != 'l') {
            throw new IllegalArgumentException("malformed: expected 'l'");
        }
        var blist = new BList();
        byte peeked;
        while ((peeked = decoder.peek()) != 'e' && peeked != -1) {
            blist.value.add(decoder.parse());
        }
        if (peeked == -1) {
            throw new IOException("unexpected EOF");
        }
        if (decoder.read() != 'e') {
            throw new IllegalStateException("(unreachable) expected 'e'");
        }
        return blist;
    }
}
