package tracker.trytka.bencode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Metainfo {
  public BString announce, createdBy, comment, encoding, info_name, info_pieces, info_md5sum;
  public BInt length, creationDate, info_pieceLength, info_private, info_length;
  public BList announceList, info_files;
  private final String[] fields; // name of fields above

  public Metainfo() {
    this.fields =
        Arrays.stream(this.getClass().getDeclaredFields())
            .filter(field -> !field.getName().equals("fields"))
            .map(Field::getName)
            .toArray(String[]::new);
  }

  public boolean isMultipleFileMode() {
    return info_files != null;
  }

  public boolean isSingleFileMode() {
    return info_length != null && info_name != null;
  }

  public Map<String, Long> getFiles() {
    if (!isMultipleFileMode()) return null;
    this.validateFileMode(); // assume types
    var files = new HashMap<String, Long>();
    for (var f : info_files.getValue()) {
      var entry = ((BDict) f);
      var finalpath = new StringBuilder();
      ArrayList<BValue<?>> path = entry.getBList("path").getValue();
      for (int i = 0; i < path.size() - 1; i++) {
        finalpath.append(((BString) path.get(i)).getStringValue() + "/");
      }
      finalpath.append(((BString) path.get(path.size() - 1)).getStringValue());
      Long length = entry.getBInt("length").getValue();
      files.put(finalpath.toString(), length);
    }
    return files;
  }

  public byte[] info_hash() {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      return digest.digest(buildBDict().get("info").encode());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return buildBDict().toString();
  }

  public byte[] encode() {
    return buildBDict().encode();
  }

  private Field getField(String field) {
    try {
      return this.getClass().getDeclaredField(field);
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("UNREACHABLE: Key should be Metainfo field: " + field);
    }
  }

  private BValue<?> getFieldValue(Field field) {
    try {
      return (BValue<?>) field.get(this);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(
          "UNREACHABLE: Key should be Metainfo Field: " + field.getName());
    }
  }

  private void setFieldValue(Field field, BValue<?> value) {
    try {
      field.set(this, value);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("UNREACHABLE: Fields names should match class fields");
    }
  }

  private static String toMetaName(String name) {
    if (name == "announceList") return "announce-list"; // exception
    return name.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
  }

  private static void putMetaValue(BDict bdict, String key, BValue<?> value) {
    var path = key.split("_");
    if (path[0].equals("info")) {
      var info = bdict.getBDict("info");
      if (info == null) {
        bdict.put("info", new BDict());
      }
      bdict.getBDict("info").put(toMetaName(path[1]), value);
    } else {
      bdict.put(toMetaName(path[0]), value);
    }
  }

  private static BValue<?> getMetaValue(BDict bdict, String key) {
    var path = key.split("_");
    if (path[0].equals("info")) {
      return bdict.getBDict("info").get(toMetaName(path[1]));
    }
    return bdict.get(toMetaName(key));
  }

  public void validateFileMode() throws IllegalArgumentException {
    if (this.isMultipleFileMode()) {
      if (info_length != null) {
        throw new IllegalArgumentException("Must be either single or multi file mode");
      }
      for (var el : this.info_files.getValue()) {
        if (!(el instanceof BDict)) {
          throw new IllegalArgumentException("info/files should be dictionary");
        }
        var fileBDict = (BDict) el;
        var keys = Set.of(new BString("length"), new BString("path"));
        if (!fileBDict.getValue().keySet().equals(keys)) {
          throw new IllegalArgumentException("info/files/ dictionary contains invalid keys");
        }
        var path = fileBDict.get("path");
        if (!(path instanceof BList)) {
          throw new IllegalArgumentException("info/files/-/path should be list");
        }
        for (var subpath : ((BList) path).getValue()) {
          if (!(subpath instanceof BString)) {
            throw new IllegalArgumentException("info/files/-/path/* should be string");
          }
        }
      }
    } else if (isSingleFileMode()) {
      if (info_files != null) {
        throw new IllegalArgumentException("Must be either single or multi file mode");
      }
    } else {
      throw new IllegalArgumentException("Missing file information (name/length/files)");
    }
  }

  public static Metainfo ofStream(InputStream stream) throws IOException {
    var parsed = new Decoder(ByteBuffer.wrap(stream.readAllBytes())).parse();
    if (!(parsed instanceof BDict)) {
      throw new IllegalArgumentException("Malformed input: expected metainfo dictionary");
    }
    return Metainfo.of((BDict) parsed);
  }

  public static Metainfo of(BDict bdict) throws IllegalArgumentException {
    var meta = new Metainfo();
    if (!(bdict.get("info") instanceof BDict)) {
      throw new IllegalArgumentException("Metainfo malformed: info should be dictionary");
    }
    int countSetInfo = 0;
    for (String key : meta.fields) {
      BValue<?> value;
      if ((value = getMetaValue(bdict, key)) == null) {
        continue;
      }
      Field field = meta.getField(key);
      try {
        meta.setFieldValue(field, value);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Metainfo malformed: invalid type for '" + key + "'");
      }
      if (key.split("_")[0].equals("info")) {
        countSetInfo++;
      }
    }
    if (bdict.getBDict("info").getValue().keySet().size() != countSetInfo) {
      throw new IllegalArgumentException("Unrecognized keys inside info dictionary");
    }
    meta.validateFileMode();
    return meta;
  }

  public BDict buildBDict() {
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
