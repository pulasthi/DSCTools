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
                long size =((long) numRows) * globalColCount * 2; // 2 for short values, which are 2 bytes long

                int m = Integer.MAX_VALUE - 1; // m = 2n for some n where n denotes the number of shorts
                int mapCount = (int) Math.ceil((double)size / m);
                MappedByteBuffer [] maps = new MappedByteBuffer[mapCount];
                for (int i = 0; i < mapCount; ++i) {
                    maps[i] = fc.map(FileChannel.MapMode.READ_ONLY, pos+(((long)i)*m), i < mapCount - 1 ? m : size%m);
                    maps[i].order(endianness);
                }

                return new DistanceReader(){
                    @Override
                    public short getDistance(int globalRow, int globalCol) {
                        long pos = ((globalRow - startRow) * ((long)globalColCount) + globalCol)*2; // byte position relative to start row
                        int mapIdx = (int)(pos/m);
                        return maps[mapIdx].getShort((int)(pos - (m*((long)mapIdx))));
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
