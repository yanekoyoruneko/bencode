package tracker.trytka.bencode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BStringTest {
  @ParameterizedTest
  @CsvSource({
    "'obszar jest gruby'",
    "'another test string'",
    "'sample string 123'",
    "'hello world!'",
    "'test'"
  })
  void testBString(String test) throws IOException {
    var encode = test.length() + ":" + test;
    var decoder = new Decoder(ByteBuffer.wrap(encode.getBytes()));
    var bstr = BString.parseBString(decoder);
    assertArrayEquals(encode.getBytes(), bstr.encode());
  }
}
