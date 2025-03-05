package tracker.trytka.bencode;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


public class MetainfoTest {
    //        "d"

    @ParameterizedTest
    @ValueSource(strings = { "d4:infod4:name4:abcdee", "d8:announce4:abcd4:infod12:piece lengthi134eee" })
    void testMetainfo(String testInput) throws IOException {
        var decoder = new Decoder(ByteBuffer.wrap(testInput.getBytes()));
        var bdict = BDict.parseBDict(decoder);
        var meta = Metainfo.of(bdict);
        // System.out.println(((BDict)bdict.get("info")).getValue().get(new
        // BString("piece length")));
        System.out.println(meta);
    }
}
