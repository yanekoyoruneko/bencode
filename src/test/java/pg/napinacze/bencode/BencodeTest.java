package pg.napinacze.bencode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
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
        assertArrayEquals(encode.getBytes(), bint.toBytes());
        BInt[] arr = { bint, bint, bint, bint };
        Arrays.sort(arr);
    }

    @ParameterizedTest
    @CsvSource({ "'obszar jest gruby'", "'another test string'", "'sample string 123'", "'hello world!'", "'test'" })
    void testBString(String test) throws IOException {
        var encode = test.length() + ":" + test;
        var decoder = new Decoder(ByteBuffer.wrap(encode.getBytes()));
        var bstr = BString.parseBString(decoder);
        assertArrayEquals(encode.getBytes(), bstr.toBytes());
    }

    @ParameterizedTest
    @ValueSource(strings = { "d3:keyi1234ee", "d5:valuei56789ee", "d3:keyi1234e5:valuei56789ee" })
    void testBDict(String testInput) throws IOException {
        var decoder = new Decoder(ByteBuffer.wrap(testInput.getBytes()));
        var bdict = BDict.parseBDict(decoder);
        assertArrayEquals(testInput.getBytes(), bdict.toBytes());
    }

    @ParameterizedTest
    @ValueSource(strings = { "l4:spam4:eggse", "l3:foo3:bar5:hello6:worldee", "l5:apple3:piee" })
    void testBencodeList(String testInput) throws IOException {
        var decoder = new Decoder(ByteBuffer.wrap(testInput.getBytes()));
        var blist = BList.parseBList(decoder);
        assertArrayEquals(testInput.getBytes(), blist.toBytes());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/rurouni.torrent", "/tears.torrent" })
    void testTorrent(String filepath) throws IOException, URISyntaxException {
        Path path = Paths.get(getClass().getResource(filepath).toURI());
        byte[] raw = Files.readAllBytes(path);
        var decoder = new Decoder(ByteBuffer.wrap(raw));
        var metainfo = decoder.parse();
        Path dump = path.resolveSibling(filepath.substring(1) + "-dump");
        System.out.println(path);
        System.out.println(Decoder.stat(metainfo, TreeMap::new));
        System.out.println(metainfo.toString());
        Files.write(dump, metainfo.toBytes());
        assertArrayEquals(metainfo.toBytes(), Arrays.copyOfRange(raw, 0, raw.length - 1));
    }
}
