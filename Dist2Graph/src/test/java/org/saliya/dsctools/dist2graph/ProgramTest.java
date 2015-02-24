package org.saliya.dsctools.dist2graph;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        String testGraphFile = "test_8x8_graph.txt";

        String binaryDistanceOutputFile = "output_8x8.bin";
        String graphOutputFileName = "output_8x8_graph.txt";
        int numPoints = generateDistanceFile(testDistanceFile, binaryDistanceOutputFile);
        Program.convertToGraph(numPoints,true,true,binaryDistanceOutputFile,graphOutputFileName);
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
    public int generateDistanceFile(String textDistanceFile, String binaryDistanceFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getFile(textDistanceFile)));
                FileOutputStream fos = new FileOutputStream(binaryDistanceFile)){
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
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
            dos.flush();
            dos.close();
            return rows;
        }
    }

    private InputStream getFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }

}
