package pg.napinacze.bencode;

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

    @Override
    public String toString() {
        return toString(0);
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
            } else {
                assert true : "unreachable";
            }
        }
        return yaml.toString();
    }

    @Override
    public byte[] toBytes() throws IOException {
        var buf = new ByteArrayOutputStream();
        buf.write('l');
        for (var el : this.value) {
            buf.write(el.toBytes());
        }
        buf.write('e');
        return buf.toByteArray();
    }

    public static BList parseBList(Decoder decoder) throws IOException {
        if (decoder.read() != 'l') {
            throw new IllegalArgumentException("BList: invalid format: expected 'l'");
        }
        var blist = new BList();
        byte peeked;
        while ((peeked = decoder.peek()) != 'e' && peeked != -1) {
            blist.value.add(decoder.parse());
        }
        if (peeked == -1) {
            throw new IOException("BList: unexpected EOF");
        }
        if (decoder.read() != 'e') {
            throw new IOException("(unreachable): BList: invalid format: expected 'e'");
        }
        return blist;
    }
}
