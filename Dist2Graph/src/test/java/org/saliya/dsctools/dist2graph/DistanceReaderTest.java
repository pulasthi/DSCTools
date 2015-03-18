package org.saliya.dsctools.dist2graph;

import com.google.common.io.LittleEndianDataOutputStream;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class DistanceReaderTest {
    @Test
    public void getDistance() throws IOException {
        String binaryDistanceOutputFile = "output_large.bin";
        boolean isBigEndian = false;
        boolean isMemoryMapped = true;
        System.out.println("Generating binary distance file ...");
        int size = generateDistanceFile(binaryDistanceOutputFile, isBigEndian);
        System.out.println("Done.");
        System.out.println("Verifying binary distance reader ...");
        verifyDistanceReader(binaryDistanceOutputFile, size, isBigEndian, isMemoryMapped);
        System.out.println("Done.");
    }

    public void verifyDistanceReader(String binaryDistanceFile, int size, boolean isBigEndian, boolean isMemoryMapped)
            throws IOException {
        DistanceReader reader = DistanceReader.readRowRange(binaryDistanceFile, 0, size, size, isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN, isMemoryMapped);
        for (int i = 0; i < size; ++i){
            for (int j = 0; j < size; ++j){
                short d = reader.getDistance(i,j);
                int expected = i % 2 == 0 ? j : (size - (j + 1));
                try {
                    assert d == expected;
                } catch (AssertionError e) {
                    System.out.println("i=" + i + " j=" + j + " d=" + d + " expected " + expected);
                    throw e;
                }
            }
        }
    }

    public int generateDistanceFile(String binaryDistanceFile, boolean isBigEndian) throws
            IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(binaryDistanceFile))){
            DataOutput dos = isBigEndian ? new DataOutputStream(bos) : new LittleEndianDataOutputStream(bos);

            int size = 32768; // this will cause two memory maps
            ByteBuffer forwardBuffer = ByteBuffer.allocate(size*2).order(
                    isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            ByteBuffer reversedBuffer = ByteBuffer.allocate(size*2).order(
                    isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

            ShortBuffer forwardBufferShort = forwardBuffer.asShortBuffer();
            ShortBuffer reversedBufferShort = reversedBuffer.asShortBuffer();
            for (int i = 0; i < size; ++i){
                forwardBufferShort.put((short)i);
                reversedBufferShort.put((short)(size-(i+1)));
            }

            byte [] f = forwardBuffer.array();
            byte [] b = reversedBuffer.array();

            for (int i = 0; i < size; i += 2){
                dos.write(f);
                dos.write(b);
            }

            return size;
        }
    }
}
