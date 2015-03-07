package org.saliya.dsctools.dist2graph;

import com.google.common.base.Strings;
import com.google.common.io.LittleEndianDataOutputStream;
import javafx.scene.shape.Path;
import org.junit.Test;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tests for {@link org.saliya.dsctools.dist2graph.Program}.
 *
 * @author esaliya@gmail.com (Saliya Ekanayake)
 */
public class ProgramTest {
    @Test
    public void convertToGraph() throws IOException {
        String testDistanceFile = "test_8x8.txt";
        String binaryDistanceOutputFile = "output_8x8.bin";
        String graphOutputFileName = "output_8x8_graph.txt";
        boolean isBigEndian = false;
        boolean isMemoryMapped = true;
        System.out.println("Generating binary distance file ...");
        int numPoints = generateDistanceFile(testDistanceFile, binaryDistanceOutputFile, isBigEndian);
        System.out.println("Done.");
        System.out.println("Converting binary distance file to textual graph adjacency list ...");
        Program.convertToGraph(numPoints,isMemoryMapped,isBigEndian,binaryDistanceOutputFile,graphOutputFileName);
        System.out.println("Done.");
        System.out.println("Verifying graph against binary distance file ...");
        verifyGraph(numPoints, binaryDistanceOutputFile, graphOutputFileName);
        System.out.println("Done.");

    }

    private void verifyGraph(int numPoints, String binaryDistanceFile, String textGraphFile) throws IOException {
        try(BufferedReader br = Files.newBufferedReader(Paths.get(textGraphFile))){
            DistanceReader distanceReader = DistanceReader.readRowRange(binaryDistanceFile, 0, numPoints, numPoints,
                                                                        ByteOrder.LITTLE_ENDIAN, true);
            int graphNodes = Integer.parseInt(br.readLine());
            assert graphNodes == numPoints;

            Pattern pattern = Pattern.compile("[ ]");
            int [] idxMask = new int[numPoints];
            // initially assume disconnected nodes <= 1% of total nodes
            for (int i = 0; i < numPoints; ++i){
                int deg = 0;
                // scan phase
                for (int j = 0; j < numPoints; ++j){
                    short d = distanceReader.getDistance(i, j);
                    if (d == -1) continue;
                    idxMask[deg] = j;
                    ++deg;
                }
                String line = br.readLine();
                assert !Strings.isNullOrEmpty(line);
                String [] splits = pattern.split(line.trim());
                assert splits.length == 2;
                assert Integer.parseInt(splits[0]) == i;
                assert Integer.parseInt(splits[1]) == deg;

                // read phase
                for (int j = 0; j < deg; ++j){
                    line = br.readLine();
                    assert !Strings.isNullOrEmpty(line);
                    splits = pattern.split(line.trim());
                    assert splits.length == 3;

                    int idx = idxMask[j];
                    assert Integer.parseInt(splits[0]) == idx;

                    short d = distanceReader.getDistance(i, idx); // at this point d MUST be >= 0
                    assert Double.parseDouble(splits[1]) - (d*1.0/Short.MAX_VALUE) < Double.MIN_NORMAL;
                    assert Integer.parseInt(splits[2]) == 0;
                }
            }
        }
    }

    /**
     * Will produce a 8x8 distance matrix of the following format<br>
     <pre>{@code
    -1  x  -1  -1  x  -1  -1  -1
    -1 -1   x   x  x   x  -1  -1
    -1  x  -1  -1 -1   x  -1  -1
    -1 -1   x  -1 -1  -1  -1   x
     x -1  -1   x -1   x  -1  -1
    -1  x  -1   x  x  -1   x  -1
    -1 -1  -1  -1 -1  -1  -1  -1
    -1  x   x  -1 -1   x  -1   x
    }<br></pre>
     where {@code x} indicates real distances (in short format) and {@code -1} indicates missing distances
     Distances are always in the range of [0.0,1.0] and are squeezed into short by multiplying by {@code Short.MAX_VALUE}
     */
    public int generateDistanceFile(String textDistanceFile, String binaryDistanceFile, boolean isBigEndian) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getFile(textDistanceFile)));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(binaryDistanceFile))){
            DataOutput dos = isBigEndian ? new DataOutputStream(bos) : new LittleEndianDataOutputStream(bos);
            int rows = Integer.parseInt(br.readLine());
            int cols = Integer.parseInt(br.readLine());
            Pattern pattern = Pattern.compile("[\t ]");
            String line;
            int count = 0;
            while ((line = br.readLine()) != null){
                String [] splits = pattern.split(line.trim());
                assert splits.length == cols;
                for (String split : splits) {
                    if (split.equals("-1")) dos.writeShort((short) -1);
                    if (split.equals("x")) dos.writeShort((short) (Math.random() * Short.MAX_VALUE));
                }
                ++count;
            }
            assert count == rows;

            bos.flush();
            bos.close();
            return rows;
        }
    }

    private InputStream getFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }

}
