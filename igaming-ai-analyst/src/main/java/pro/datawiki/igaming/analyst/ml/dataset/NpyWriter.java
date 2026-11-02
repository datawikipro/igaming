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

        float[] flat = new float[numRows * numCols];
        for (int r = 0; r < numRows; r++) {
            float[] row = rows.get(r);
            if (row.length != numCols) {
                throw new IllegalStateException("Inconsistent row dimension: expected " + numCols + ", got " + row.length);
            }
            System.arraycopy(row, 0, flat, r * numCols, numCols);
        }
        writeNpyFloat2D(file, flat, numRows, numCols);
    }

    /**
     * Writes a flattened 2D float array directly to a standard .npy file (Version 1.0, <f4).
     *
     * @param file Target file
     * @param flatArray Flattened float matrix elements (row-major order)
     * @param numRows Number of rows
     * @param numCols Number of columns
     * @throws IOException on I/O error
     */
    public static void writeNpyFloat2D(File file, float[] flatArray, int numRows, int numCols) throws IOException {
        if (flatArray == null || numRows <= 0 || numCols <= 0 || flatArray.length != (long) numRows * numCols) {
            throw new IllegalArgumentException("Invalid matrix dimensions or flat array size");
        }

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
            int totalFloats = numRows * numCols;
            int chunkSize = Math.min(16384, totalFloats);
            byte[] buffer = new byte[chunkSize * 4];
            int bufIdx = 0;

            for (int i = 0; i < totalFloats; i++) {
                int bits = Float.floatToIntBits(flatArray[i]);
                buffer[bufIdx++] = (byte) (bits & 0xFF);
                buffer[bufIdx++] = (byte) ((bits >> 8) & 0xFF);
                buffer[bufIdx++] = (byte) ((bits >> 16) & 0xFF);
                buffer[bufIdx++] = (byte) ((bits >> 24) & 0xFF);

                if (bufIdx == buffer.length) {
                    bos.write(buffer, 0, bufIdx);
                    bufIdx = 0;
                }
            }
            if (bufIdx > 0) {
                bos.write(buffer, 0, bufIdx);
            }
            bos.flush();
        }
        log.info("[NpyWriter] Successfully wrote matrix ({}, {}) to {}", numRows, numCols, file.getAbsolutePath());
    }
}
