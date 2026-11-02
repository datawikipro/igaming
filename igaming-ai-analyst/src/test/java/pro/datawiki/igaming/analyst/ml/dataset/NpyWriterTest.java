package pro.datawiki.igaming.analyst.ml.dataset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NpyWriterTest {

    @Test
    void testWriteFloatMatrix(@TempDir Path tempDir) throws IOException {
        File target = tempDir.resolve("test_matrix.npy").toFile();

        List<float[]> rows = new ArrayList<>();
        rows.add(new float[]{1.0f, 2.5f, -3.25f});
        rows.add(new float[]{0.0f, 100.0f, 0.5f});

        NpyWriter.writeFloatMatrix(target, rows);

        assertTrue(target.exists());
        assertTrue(target.length() > 0);

        try (FileInputStream fis = new FileInputStream(target)) {
            byte[] magic = new byte[6];
            assertEquals(6, fis.read(magic));
            assertEquals((byte) 0x93, magic[0]);
            assertEquals('N', magic[1]);
            assertEquals('U', magic[2]);
            assertEquals('M', magic[3]);
            assertEquals('P', magic[4]);
            assertEquals('Y', magic[5]);

            int major = fis.read();
            int minor = fis.read();
            assertEquals(1, major);
            assertEquals(0, minor);

            int b1 = fis.read();
            int b2 = fis.read();
            int headerLen = b1 | (b2 << 8);
            assertTrue(headerLen > 0);

            // Total preamble + header must be aligned to 64
            assertEquals(0, (10 + headerLen) % 64);
        }
    }
}
