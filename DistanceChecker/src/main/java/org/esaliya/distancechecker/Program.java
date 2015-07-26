package org.esaliya.distancechecker;

import edu.indiana.soic.spidal.common.BinaryReader;
import edu.indiana.soic.spidal.common.Range;

import java.nio.ByteOrder;

public class Program {
    public static void main(String[] args) {
        String distanceFile = args[0];
        int size = Integer.parseInt(args[1]);
        double threshold = Double.parseDouble(args[2]);
        int entriesToOutput = Integer.parseInt(args[3]);

        int blockSize = 1000;
        int parts = size / blockSize;
        int remainder = size % blockSize;
        if (remainder > 0){
            parts += 1;
        }


        int count = 0;
        for (int p = 0; p < parts; ++p) {
            int startIdxInclusive = p * blockSize;
            int endIdxInclusive = Math.min(
                (((p + 1) * blockSize) - 1), size - 1);

            BinaryReader reader = BinaryReader.readRowRange(
                distanceFile, new Range(startIdxInclusive, endIdxInclusive), size, ByteOrder.BIG_ENDIAN,
                true, true);
            int pointCount =  (remainder > 0 ? remainder : blockSize) * size;
            for (int i = 0; i < pointCount && count < entriesToOutput; ++i) {
                double d = reader.getValue(i);
                int row = (i / size) + (p*blockSize);
                int col = i % size;
                if (d <= threshold && row != col) {
                    System.out.println(
                        "d:" + d + " threshold:" + threshold + " row:" +
                        row + " col:" + col);
                    ++count;
                }
            }
        }
    }
}
