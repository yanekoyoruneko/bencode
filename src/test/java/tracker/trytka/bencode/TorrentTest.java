package tracker.trytka.bencode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.TreeMap;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TorrentTest {
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
        Files.write(dump, metainfo.encode());
        assertArrayEquals(metainfo.encode(), Arrays.copyOfRange(raw, 0, raw.length - 1));
    }
}
