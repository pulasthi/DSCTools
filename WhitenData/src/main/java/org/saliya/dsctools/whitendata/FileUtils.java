package org.saliya.dsctools.whitendata;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class FileUtils {
    public static double [][] readVectorsInColumnOrder(String file, int myNumVec, int vecLen, int globalStartIdx)
            throws IOException {
        double [][] columnVectors = new double[vecLen][myNumVec]; // column major storage of vectors
        Path path = Paths.get(file);
        try (BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset())) {
            Pattern pattern = Pattern.compile("[\t]");
            Optional<String> line;
            int idx = 0;
            while ((line = Optional.ofNullable(reader.readLine())).isPresent() && idx < globalStartIdx+myNumVec){
                if (idx < globalStartIdx) {++idx;continue;}
                String [] splits = pattern.split(line.get().trim());
                if (splits.length != vecLen){
                    throw new RuntimeException("Vector length for line " + idx + " mismatch with given vector length " + vecLen);
                }
                final int idxTmp = idx;
                IntStream.range(0,vecLen).parallel().forEach(i->columnVectors[i][idxTmp-globalStartIdx] = Double.parseDouble(splits[i]));
                ++idx;
            }
        }
        return columnVectors;
    }
}
