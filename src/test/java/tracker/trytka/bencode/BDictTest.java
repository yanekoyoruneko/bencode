package tracker.trytka.bencode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BDictTest {
  @ParameterizedTest
  @ValueSource(strings = {"d8:announce4:abcde", "d5:valuei56789ee", "d3:keyi1234e5:valuei56789ee"})
  void testBDict(String testInput) throws IOException {
    var decoder = new Decoder(ByteBuffer.wrap(testInput.getBytes()));
    var bdict = BDict.parseBDict(decoder);
    assertArrayEquals(testInput.getBytes(), bdict.encode());
  }
}
