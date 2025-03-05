package tracker.trytka.bencode;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Metainfo {
    private BString info_name, info_pieces, announce, createdBy, comment, encoding;
    private BInt length, info_pieceLength, creationDate;
    private final String[] fields;

    public Metainfo() {
        this.fields = Arrays.stream(this.getClass().getDeclaredFields())
                            .filter(field -> !field.getName().equals("fields"))
                            .map(Field::getName)
                            .toArray(String[]::new);
    }


    @Override
    public String toString() {
        return make().toString();
    }

    public byte[] encode() {
        return make().encode();
    }

    private Field getField(String field) {
        try {
            return this.getClass().getDeclaredField(field);
        } catch (NoSuchFieldException e) {
                throw new IllegalStateException("unreachable: key has to be Metainfo field: ");
        }
    }

    private static BValue<?> fromBDict(BDict bdict, String key) {
        BValue<?> value = bdict;
        for (var path : key.split("_")) {
            if ((value = ((BDict) value).get(path)) == null) {
                return null;
            }
            if (!(value instanceof BDict)) {
                return value;
            }
        }
        return value;
    }

    public static Metainfo of(BDict bdict) throws IllegalArgumentException {
        var meta = new Metainfo();
        for (String key : meta.fields) {
            BValue<?> value;
            if ((value = fromBDict(bdict, key)) == null) {
                continue;
            }
            Field field = meta.getField(key);
            try {
                field.set(meta, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "unreachable: fields strings should match class fields");
            }
        }
        return meta;
    }

    public static void putAt(BDict bdict, String key, BValue<?> value) {
            BValue<?> dest = bdict;
            var path = key.split("_");
            for (var sub : Arrays.copyOfRange(path, 0, path.length - 1)) {
                if (((BDict)dest).get(sub) == null) {
                    var subBDict = new BDict();
                    ((BDict)dest).put(sub, subBDict);
                    dest = subBDict;
                } else {
                    // subpath must be bdict
                    dest = ((BDict) dest).get(sub);
                }
            }
            ((BDict)dest).put(path[path.length - 1], value);
    }

    public BDict make() {
        var meta = new BDict();
        for (String key : fields) {
            Field field = getField(key);
            BValue<?> value;
            try {
                if ((value = (BValue<?>)field.get(this)) == null) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("unreachable");
            }
            putAt(meta, key, value);
        }
        return meta;
    }
}
