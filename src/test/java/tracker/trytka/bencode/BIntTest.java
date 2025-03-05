package tracker.trytka.bencode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

public class BIntTest {
    @ParameterizedTest
    @ValueSource(ints = { 2137, 42, 100, -1, 0 })
    void testBInt(int test) throws IOException {
        var encode = "i" + test + "e";
        var decoder = new Decoder(ByteBuffer.wrap(encode.getBytes()));
        var bint = BInt.parseBInt(decoder);
        assertArrayEquals(encode.getBytes(), bint.encode());
        BInt[] arr = { bint, bint, bint, bint };
        Arrays.sort(arr);
    }
}
