package pg.napinacze.bencode;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Decoder {
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
