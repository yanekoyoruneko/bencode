package tracker.trytka.bencode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BListTest {
    @ParameterizedTest
    @ValueSource(strings = { "l4:spam4:eggse", "l3:foo3:bar5:hello6:worldee", "l5:apple3:piee" })
    void testBencodeList(String testInput) throws IOException {
        var decoder = new Decoder(ByteBuffer.wrap(testInput.getBytes()));
        var blist = BList.parseBList(decoder);
        assertArrayEquals(testInput.getBytes(), blist.encode());
    }
}
