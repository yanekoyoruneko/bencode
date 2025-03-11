package tracker.trytka.bencode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MetainfoTest {
  @ParameterizedTest
  @ValueSource(strings = {"/rurouni.torrent", "/tears.torrent"})
  void testMetainfo(String filepath) throws IOException, URISyntaxException {
    Path path = Paths.get(getClass().getResource(filepath).toURI());
    byte[] raw = Files.readAllBytes(path);
    var decoder = new Decoder(ByteBuffer.wrap(raw));
    var bdict = decoder.parse();
    var meta = Metainfo.of((BDict) bdict);
    System.out.println("INFO HASH VALUE:  " + meta.info_hash_str());
    System.out.println("TORRENT URL: " + meta.info_hash_url());
    if (meta.isMultipleFileMode()) {
      System.out.println("FILES:");
      for (var file : meta.getFiles().entrySet()) {
        System.out.println(file);
      }
    }
    byte[] parsed = Metainfo.decodeInfoHashURL(meta.info_hash_url());
    assertArrayEquals(parsed, meta.info_hash());
    System.out.println(meta);
  }
}
