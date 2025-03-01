package pg.napinacze.bencode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Decoder {
    final public static Charset encoding = StandardCharsets.UTF_8;
    private ByteBuffer buffer;
    static int indent = 0;

    public Decoder(java.nio.ByteBuffer in) {
        this.buffer = in;
    }

    public byte peek() {
        return buffer.get(buffer.position());
    }

    public byte read() {
        return buffer.get();
    }

    public void read(byte[] buf) {
        buffer.get(buf);
    }

    long parseUintUntil(int until) throws IOException {
        String buf = "";
        byte peeked;
        while ((peeked = this.peek()) != until && peeked != -1) {
            if (!Character.isDigit(peeked)) {
                throw new NumberFormatException("parseIntUntil: invalid format: expected digit");
            }
            buf += (char) this.read();
        }
        if (peeked == -1) {
            throw new IOException("parseIntUntil: unexpected EOF");
        }
        return Long.parseLong(buf);
    }

    public static <T extends Map<String, Integer>> Map<String, Integer> stat(BValue<?> bval, Supplier<T> mapsup) {
        return stati(bval, mapsup.get());
    }

    private static <T extends Map<String, Integer>> Map<String, Integer> stati(BValue<?> bval,
            Map<String, Integer> count) {
        String className = bval.getClass().getName();
        count.put(className, count.getOrDefault(className, 0) + 1);
        if (bval instanceof BList blist) {
            for (var el : blist.getValue())
                stati(el, count);
        } else if (bval instanceof BDict bdict) {
            for (var key : bdict.getValue().keySet()) {
                stati(key, count);
                stati(bdict.getValue().get(key), count);
            }
        }
        return count;
    }

    public BValue<?> parse() throws IOException {
        switch (this.peek()) {
            case 'l':
                return BList.parseBList(this);
            case 'd':
                return BDict.parseBDict(this);
            case 'i':
                return BInt.parseBInt(this);
        }
        if (Character.isDigit(this.peek())) {
            return BString.parseBString(this);
        } else {
            throw new IllegalArgumentException("parse: invalid format");
        }
    }
}
