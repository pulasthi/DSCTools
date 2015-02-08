package org.saliya.dsctools.whitendata;

import com.google.common.io.Files;
import mpi.MPIException;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;


public class Program {
    private static Options programOptions = new Options();
    static {
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_F),Constants.CMD_OPTION_LONG_F, true,
                                 Constants.CMD_OPTION_DESCRIPTION_F);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_N),Constants.CMD_OPTION_LONG_N, true,
                                 Constants.CMD_OPTION_DESCRIPTION_N);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_L),Constants.CMD_OPTION_LONG_L, true,
                                 Constants.CMD_OPTION_DESCRIPTION_L);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_I),Constants.CMD_OPTION_LONG_I, true,
                                 Constants.CMD_OPTION_DESCRIPTION_I);
    }
    public static void main(String[] args) {
        Optional<CommandLine>
                parserResult = parseCommandLineArguments(args, programOptions);
        if (!parserResult.isPresent()){
            System.out.println(Constants.ERR_PROGRAM_ARGUMENTS_PARSING_FAILED);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        CommandLine cmd = parserResult.get();
        if (!(cmd.hasOption(Constants.CMD_OPTION_SHORT_F) && cmd.hasOption(Constants.CMD_OPTION_SHORT_N) &&
                cmd.hasOption(Constants.CMD_OPTION_SHORT_L) && cmd.hasOption(Constants.CMD_OPTION_SHORT_I))) {
            System.out.println(Constants.ERR_INVALID_PROGRAM_ARGUMENTS);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        String dataFile = cmd.getOptionValue(Constants.CMD_OPTION_SHORT_F);
        int numVec = Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_N));
        int vecLen = Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_L));
        boolean ignore = Boolean.parseBoolean(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_I));

        ParallelOptions pOps = new ParallelOptions(args, numVec);
        MpiOps mpiOps = new MpiOps(numVec, vecLen, pOps);
        try {
            double[][] columnVectors = FileUtils.readVectorsInColumnOrder(dataFile, pOps.myNumVec, vecLen, pOps.globalVecStartIdx, ignore, ignore);

            ComponentStatistics[] summaries = Arrays.stream(columnVectors).parallel()
                                                    .map(c -> Arrays.stream(c).parallel()
                                                                    .collect(ComponentStatistics::new,
                                                                             ComponentStatistics::accept,
                                                                             ComponentStatistics::combine))
                                                    .toArray(ComponentStatistics[]::new);
            mpiOps.allReduce(summaries);

            // Whiten data
            IntStream.range(0, vecLen).parallel().forEach(i ->{
                double average = summaries[i].getAverage();
                double stdDev = summaries[i].getStandardDeviation();
                IntStream.range(0,pOps.myNumVec).parallel().forEach(j->columnVectors[i][j] = (columnVectors[i][j] - average)/stdDev);
            });

            Path path = Paths.get(dataFile);
            Optional<Path> parent = Optional.ofNullable(path.getParent());
            String name = "whiten_" + Files.getNameWithoutExtension(dataFile) + ".txt";
            Path whitenDataFile = parent.isPresent() ? parent.get().resolve(name) : Paths.get(name);

            if (pOps.rank == 0){
                FileUtils.writeVectorsToFile(whitenDataFile, columnVectors, pOps.myNumVec, vecLen);
                mpiOps.notify(pOps.rank + 1);
                mpiOps.receive(pOps.size - 1);
                System.out.println("Done.");
            } else {
                mpiOps.receive(pOps.rank - 1);
                FileUtils.writeVectorsToFile(whitenDataFile, columnVectors, pOps.myNumVec, vecLen);
                mpiOps.notify((pOps.rank+1)%pOps.size);
            }
            pOps.endParallelism();
        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred ", e);
        } catch (MPIException e) {
            throw new RuntimeException("MPI Error occurred", e);
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


    private static void print2DArray(double[][] vectors) {
        Arrays.stream(vectors).forEach(v -> {
            Arrays.stream(v).forEach(c -> System.out.print(c + "\t"));
            System.out.println();
        });
    }
}
