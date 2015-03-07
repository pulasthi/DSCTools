package org.saliya.dsctools.dist2graph;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Binary distance reader
 *
 * @author esaliya@gmail.com (Saliya Ekanayake)
 */
public abstract class DistanceReader {
    public short getDistance(int globalRow, int globalCol){
        throw new UnsupportedOperationException();
    }
    public static DistanceReader readRowRange(String fname, int startRow, int numRows, int globalColCount, ByteOrder
            endianness, boolean mmap) throws IOException {
        if (mmap) {
            try (FileChannel fc = (FileChannel) Files.newByteChannel(Paths.get(fname), StandardOpenOption.READ)) {
                long pos = ((long) startRow) * globalColCount * 2; // 2 for short values, which are 2 bytes long
                MappedByteBuffer mappedBytes = fc.map(FileChannel.MapMode.READ_ONLY, pos,
                                                      ((long)numRows) * globalColCount * 2); // 2 for short values, which are 2 bytes long
                mappedBytes.order(endianness);
                return new DistanceReader(){
                    @Override
                    public short getDistance(int globalRow, int globalCol) {
                        int pos = (globalRow - startRow) * globalColCount + globalCol; // element position - not the byte position
                        return mappedBytes.getShort(pos*2); // pos*2 is the byte position
                    }
                };
            }
        } else {
            try (FileInputStream fis = new FileInputStream(fname)) {
                DataInput di = endianness == ByteOrder.BIG_ENDIAN ? new DataInputStream(
                        fis) : new LittleEndianDataInputStream(fis);

                int numBytesToSkip = startRow * globalColCount * Short.BYTES;
                int skippedBytes = di.skipBytes(numBytesToSkip);
                if (skippedBytes != numBytesToSkip)
                    throw new IOException(Constants.errWrongNumOfBytesSkipped(numBytesToSkip, skippedBytes));

                short[][] buffer = new short[numRows][];
                for (int i = 0; i < numRows; ++i) {
                    buffer[i] = new short[globalColCount];
                    for (int j = 0; j < globalColCount; ++j) {
                        buffer[i][j] = di.readShort();
                    }
                }
                return new DistanceReader() {
                    @Override
                    public short getDistance(int globalRow, int globalCol) {
                        int localRow = globalRow - startRow;
                        return buffer[localRow][globalCol];
                    }
                };
            }
        }
    }
}
