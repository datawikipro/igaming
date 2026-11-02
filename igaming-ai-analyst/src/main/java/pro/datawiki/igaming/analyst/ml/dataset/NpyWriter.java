package pro.datawiki.igaming.analyst.ml.dataset;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * High-performance binary writer for NumPy .npy format (Version 1.0).
 * Produces standard little-endian float32 matrices (<f4) compatible with PyTorch and NumPy.
 */
@Slf4j
public class NpyWriter {

    private static final byte[] MAGIC = new byte[]{(byte) 0x93, 'N', 'U', 'M', 'P', 'Y'};
    private static final byte MAJOR_VERSION = 1;
    private static final byte MINOR_VERSION = 0;

    /**
     * Writes a 2D float array (List of float[]) to a .npy file.
     *
     * @param file Target file
     * @param rows List of float vectors (each vector must have the same length)
     * @throws IOException on I/O error
     */
    public static void writeFloatMatrix(File file, List<float[]> rows) throws IOException {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Cannot write empty matrix to .npy");
        }
        int numRows = rows.size();
        int numCols = rows.get(0).length;

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), 64 * 1024)) {
            // 1. Magic + Version (8 bytes)
            bos.write(MAGIC);
            bos.write(MAJOR_VERSION);
            bos.write(MINOR_VERSION);

            // 2. Build header dict
            String dict = String.format("{'descr': '<f4', 'fortran_order': False, 'shape': (%d, %d), }", numRows, numCols);
            int preambleLen = 10; // 6 (magic) + 2 (version) + 2 (header length short)
            int unpaddedLen = preambleLen + dict.length();
            int padLen = (64 - (unpaddedLen % 64)) % 64;
            if (padLen == 0) {
                padLen = 64;
            }
            // Ensure ending with '\n'
            StringBuilder sb = new StringBuilder(dict);
            for (int i = 0; i < padLen - 1; i++) {
                sb.append(' ');
            }
            sb.append('\n');

            byte[] headerBytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
            int headerLen = headerBytes.length;

            // 3. Write header length (2 bytes, little-endian)
            bos.write((byte) (headerLen & 0xFF));
            bos.write((byte) ((headerLen >> 8) & 0xFF));

            // 4. Write header
            bos.write(headerBytes);

            // 5. Write raw IEEE-754 little-endian 32-bit floats
            byte[] buffer = new byte[numCols * 4];
            for (float[] row : rows) {
                if (row.length != numCols) {
                    throw new IllegalStateException("Inconsistent row dimension: expected " + numCols + ", got " + row.length);
                }
                int offset = 0;
                for (int c = 0; c < numCols; c++) {
                    int bits = Float.floatToIntBits(row[c]);
                    buffer[offset++] = (byte) (bits & 0xFF);
                    buffer[offset++] = (byte) ((bits >> 8) & 0xFF);
                    buffer[offset++] = (byte) ((bits >> 16) & 0xFF);
                    buffer[offset++] = (byte) ((bits >> 24) & 0xFF);
                }
                bos.write(buffer, 0, offset);
            }
            bos.flush();
        }
        log.info("[NpyWriter] Successfully wrote matrix ({}, {}) to {}", numRows, numCols, file.getAbsolutePath());
    }
}
