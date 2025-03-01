package pg.napinacze.bencode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.junit.jupiter.params.provider.CsvSource;

public class BencodeTest {
    @ParameterizedTest
    @ValueSource(ints = { 2137, 42, 100, -1, 0 })
    void testBInt(int test) throws IOException {
        var encode = "i" + test + "e";
        var decoder = new Decoder(ByteBuffer.wrap(encode.getBytes()));
        var bint = BInt.parseBInt(decoder);
        assertEquals(encode, bint.toString());
        BInt[] arr = { bint, bint, bint, bint };
        Arrays.sort(arr);
    }

    @ParameterizedTest
    @CsvSource({ "'obszar jest gruby'", "'another test string'", "'sample string 123'", "'hello world!'", "'test'" })
    void testBString(String test) throws IOException {
        var encode = test.length() + ":" + test;
        var decoder = new Decoder(ByteBuffer.wrap(encode.getBytes()));
        var bstr = BString.parseBString(decoder);
        assertEquals(encode, bstr.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "d3:keyi1234ee", "d5:valuei56789ee", "d3:keyi1234e5:valuei56789ee" })
    void testBDict(String testInput) throws IOException {
        var decoder = new Decoder(ByteBuffer.wrap(testInput.getBytes()));
        var bdict = BDict.parseBDict(decoder);
        assertEquals(testInput, bdict.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "l4:spam4:eggse", "l3:foo3:bar5:hello6:worldee", "l5:apple3:piee" })
    void testBencodeList(String testInput) throws IOException {
        var decoder = new Decoder(ByteBuffer.wrap(testInput.getBytes()));
        var blist = BList.parseBList(decoder);
        assertEquals(testInput, blist.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/rurouni.torrent", "/tears.torrent" })
    void testTorrent(String filepath) throws IOException, URISyntaxException {
        Path path = Paths.get(getClass().getResource(filepath).toURI());
        byte[] raw = Files.readAllBytes(path);
        var decoder = new Decoder(ByteBuffer.wrap(raw));
        var metainfo = decoder.parse();
        System.out.println(path);
        System.out.println(metainfo.YAML());
        assertEquals(metainfo.toString(), new String(raw).trim());
    }
}
