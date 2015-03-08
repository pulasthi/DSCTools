package org.saliya.dsctools.dist2graph;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Pairwise binary distance file to textual graph converter
 *
 * @author esaliya@gmail.com (Saliya Ekanayake)
 */
public class Program {
    private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static Options programOptions = new Options();

    static {
        programOptions
                .addOption(String.valueOf(Constants.CMD_OPTION_SHORT_F), true, Constants.CMD_OPTION_DESCRIPTION_F);
        programOptions
                .addOption(String.valueOf(Constants.CMD_OPTION_SHORT_N), true, Constants.CMD_OPTION_DESCRIPTION_N);
        programOptions
                .addOption(String.valueOf(Constants.CMD_OPTION_SHORT_O), true, Constants.CMD_OPTION_DESCRIPTION_O);
        programOptions
                .addOption(String.valueOf(Constants.CMD_OPTION_SHORT_B), true, Constants.CMD_OPTION_DESCRIPTION_B);
        programOptions
                .addOption(String.valueOf(Constants.CMD_OPTION_SHORT_M), true, Constants.CMD_OPTION_DESCRIPTION_M);
    }

    public static void main(String[] args) {
        Stopwatch mainTimer = Stopwatch.createStarted();
        Optional<CommandLine>
                parserResult = parseCommandLineArguments(args, programOptions);
        if (!parserResult.isPresent()){
            System.out.println(Constants.ERR_PROGRAM_ARGUMENTS_PARSING_FAILED);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        CommandLine cmd = parserResult.get();
        if (!(cmd.hasOption(Constants.CMD_OPTION_SHORT_F) && cmd.hasOption(Constants.CMD_OPTION_SHORT_N) &&
                cmd.hasOption(Constants.CMD_OPTION_SHORT_O) && cmd.hasOption(Constants.CMD_OPTION_SHORT_O))) {
            System.out.println(Constants.ERR_INVALID_PROGRAM_ARGUMENTS);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }



        int numPoints = Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_N));

        boolean isMemoryMapped = !cmd.hasOption(Constants.CMD_OPTION_SHORT_M) ||
                Boolean.parseBoolean(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_M));

        boolean isBigEndian = !cmd.hasOption(Constants.CMD_OPTION_SHORT_B) ||
                Boolean.parseBoolean(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_B));

        String distanceFile = cmd.getOptionValue(Constants.CMD_OPTION_SHORT_F);
        String outputFile = cmd.getOptionValue(Constants.CMD_OPTION_SHORT_O);

        if (Strings.isNullOrEmpty(distanceFile)) {
            throw new IllegalArgumentException("Distance file error - " + Constants.ERR_EMPTY_FILE_NAME);
        }
        if (Strings.isNullOrEmpty(outputFile)) {
            throw new IllegalArgumentException("Output file error - " + Constants.ERR_EMPTY_FILE_NAME);
        }

        System.out.println("=== Program Started on " + dateFormat.format(new Date()) + " ===");
        convertToGraph(numPoints, isMemoryMapped, isBigEndian, distanceFile, outputFile);
        mainTimer.stop();
        System.out.println("=== Program terminated successfully on " + dateFormat.format(new Date())  +" running for" + (mainTimer.elapsed(
                TimeUnit.MILLISECONDS) * 0.0001)  + " seconds ===");

    }

    /**
     * Converts the binary distance file to a textual graph adjacency list. The format is, <br>
     <pre>{@code
     <number of nodes in G>
     <ID1>  <Deg1>
       <Nbr_ID11> <W11> <T11>
       <Nbr_ID12> <W12> <T12>
       ... ...
     <ID2>  <Deg2>
       <Nbr_ID21> <W21> <T21>
       <Nbr_ID22> <W22> <T22>
       ... ...}<br></pre>
     * where,<br>
     * <pre>{@code
     * <ID1> is the id of the first node, which is an integer to identify the first node
     * <Deg1> is the degree of the first node
     * The the next <Deg1> lines are the neighbors of the first node ID1
     *   <Nbr_ID11> is the ID of the first neighbor of node ID1
     *   <W11> is the weight (any real number) of this edge
     *   <T11> is the type the edge. If there is no type associated with an edge, this can simply be zero}<br></pre>
     *
     * @param numPoints total number of points
     * @param isMemoryMapped (recommended) indicates whether to use memory mapped files
     * @param isBigEndian indicates the endianness of the binary file
     * @param distanceFile path to binary pairwise distance file
     * @param outputFile path to output textual graph file
     */
    public static void convertToGraph(int numPoints, boolean isMemoryMapped, boolean isBigEndian, String distanceFile,
                                       String outputFile) {
        Path filePath = Paths.get(outputFile);
        Path metaFilePath = Paths.get(filePath.getParent().toString(), "meta.txt");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                                                  true);
             PrintWriter metaWriter = new PrintWriter(Files.newBufferedWriter(metaFilePath, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                                                  true);) {
            DistanceReader
                    distanceReader = DistanceReader.readRowRange(distanceFile, 0, numPoints, numPoints, isBigEndian?
                    ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN, isMemoryMapped);
            writer.println(numPoints); // <number of nodes in G>
            int [] idxMask = new int[numPoints];
            // initially assume disconnected nodes <= 1% of total nodes
            List<Integer> disconnectedNodes = new ArrayList<>(numPoints / 100);
            long edgeCount = 0; // total edges
            for (int i = 0; i < numPoints; ++i){
                int deg = 0;
                int noEdgeCount = 0; // missing distances -> no edge between node i and the corresponding other node
                // scan phase
                for (int j = 0; j < numPoints; ++j){
                    if (i == j) continue; // ignore self edges
                    short d = distanceReader.getDistance(i, j);
                    if (d == -1) {
                        ++noEdgeCount;
                        continue;
                    }
                    idxMask[deg] = j;
                    ++deg;
                }
                metaWriter.println(i + " " + deg + " " + noEdgeCount);
                writer.println(i + " " + deg); // <ID_i> <Deg_i>
                edgeCount+=deg;
                if (deg == 0) disconnectedNodes.add(i);
                // read phase
                /*for (int j = 0; j < deg; ++j){
                    int idx = idxMask[j];
                    short d = distanceReader.getDistance(i, idx); // at this point d MUST be >= 0
                    writer.println(" " + idx + " " + (d*1.0/Short.MAX_VALUE) + " 0"); // <Nbr_ID_ij> <W_ij> <T_ij>
                }*/
                if (i>0 && i%100 == 0) System.out.println("  Converted " + i + " nodes so far ...");
            }
            writer.close();
            metaWriter.close();
            System.out.println("  Conversion completed.");
            System.out.println("    Total nodes: " + numPoints);
            System.out.println("    Disconnected nodes:  " + disconnectedNodes.size());
            System.out.println("    Total edges: "  + edgeCount + " or " + (edgeCount/2) + " if undirected");
        } catch (IOException e) {
            System.err.format("Failed writing graph file: %s%n", e);
        }
    }

    /**
     * Parse command line arguments
     * @param args Command line arguments
     * @param opts Command line options
     * @return An <code>Optional&lt;CommandLine&gt;</code> object
     */
    private static Optional<CommandLine> parseCommandLineArguments(String [] args, Options opts){
        CommandLineParser optParser = new GnuParser();
        try {
            return Optional.ofNullable(optParser.parse(opts, args));
        } catch (ParseException e) {
            throw new RuntimeException("Command line parsing failed ", e);
        }
    }

}
