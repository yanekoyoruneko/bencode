package tracker.trytka.bencode;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Metainfo {
    public BString info_name, info_pieces, announce, createdBy, comment, encoding;
    public BInt length, info_pieceLength, creationDate, info_private, info_length;
    public BList info_files;
    private final String[] fields; // name of fields above

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

    private BValue<?> getFieldValue(Field field) {
            try {
                return (BValue<?>) field.get(this);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("unreachable");
            }
    }

    private void setFieldValue(Field field, BValue<?> value) {
        try {
            field.set(this, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("unreachable: fields names should match class fields");
        }
    }

    private static String toMetaName(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
    }

    private static void putMetaValue(BDict bdict, String key, BValue<?> value) {
            var path = key.split("_");
            if (path[0].equals("info")) {
                var info = bdict.get("info");
                if (info == null) {
                    bdict.put("info", new BDict());
                }
                ((BDict) bdict.get("info")).put(toMetaName(path[1]), value);
            } else {
                bdict.put(toMetaName(path[0]), value);
            }
    }


    private static BValue<?> getMetaValue(BDict bdict, String key) {
        var path = key.split("_");
        if (path[0].equals("info")) {
            return ((BDict)bdict.get("info")).get(toMetaName(path[1]));
        }
        return bdict.get(toMetaName(key));
    }

    public static Metainfo of(BDict bdict) throws IllegalArgumentException {
        var meta = new Metainfo();
        for (String key : meta.fields) {
            BValue<?> value;
            if ((value = getMetaValue(bdict, key)) == null) {
                continue;
            }
            Field field = meta.getField(key);
            try {
                meta.setFieldValue(field, value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("metainfo malformed: invalid type for '" + key + "'");
            }
        }
        return meta;
    }

    public BDict make() {
        var bdict = new BDict();
        for (String key : this.fields) {
            var value = getFieldValue(getField(key));
            if (value == null) {
                continue;
            }
            putMetaValue(bdict, key, value);
        }
        return bdict;
    }
}
