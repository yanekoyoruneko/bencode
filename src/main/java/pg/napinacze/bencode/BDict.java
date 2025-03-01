package pg.napinacze.bencode;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

public class BDict extends BValue<SortedMap<BString, BValue<?>>> {
    public BDict() {
        this.value = new TreeMap<>();
    }

    @Override
    public String YAML() {
        return this.YAMLindent(0);
    }

    public String YAMLindent(int indent) {
        var yaml = new StringBuilder();
        for (var key : this.value.keySet()) {
            for (int i = 0; i < indent; i++) {
                yaml.append(" ");
            }
            var bvalue = this.value.get(key);
            if (bvalue instanceof BInt || bvalue instanceof BString) {
                yaml.append(key.YAML() + ": " + bvalue.YAML() + "\n");
            } else if (bvalue instanceof BList blist) {
                yaml.append(key.YAML() + ":\n" + blist.YAMLindent(indent));
            } else if (bvalue instanceof BDict bdict) {
                yaml.append(key.YAML() + ":\n" + bdict.YAMLindent(indent + 4));
            } else {
                assert true : "unreachable";
            }
        }
        return yaml.toString();
    }

    @Override
    public String toString() {
        var repr = new StringBuilder("d");
        // this is written like this just for fun dont judge me
        this.value.keySet().forEach(key -> repr.append(key).append(this.value.get(key)));
        return repr + "e";
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
