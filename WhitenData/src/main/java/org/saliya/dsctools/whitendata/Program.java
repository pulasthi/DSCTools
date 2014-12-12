package org.saliya.dsctools.whitendata;

import com.google.common.base.Optional;
import mpi.MPIException;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;


public class Program {
    private static Options programOptions = new Options();
    static {
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_F),Constants.CMD_OPTION_LONG_F, true,
                                 Constants.CMD_OPTION_DESCRIPTION_F);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_N),Constants.CMD_OPTION_LONG_N, true,
                                 Constants.CMD_OPTION_DESCRIPTION_N);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_L),Constants.CMD_OPTION_LONG_L, true,
                                 Constants.CMD_OPTION_DESCRIPTION_L);
    }
    public static void main(String[] args) {
        Optional<org.apache.commons.cli.CommandLine>
                parserResult = parseCommandLineArguments(args, programOptions);
        if (!parserResult.isPresent()){
            System.out.println(Constants.ERR_PROGRAM_ARGUMENTS_PARSING_FAILED);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        CommandLine cmd = parserResult.get();
        if (!(cmd.hasOption(Constants.CMD_OPTION_LONG_F) && cmd.hasOption(Constants.CMD_OPTION_SHORT_N) &&
                cmd.hasOption(Constants.CMD_OPTION_SHORT_L))) {
            System.out.println(Constants.ERR_INVALID_PROGRAM_ARGUMENTS);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        String dataFile = cmd.getOptionValue(Constants.CMD_OPTION_LONG_F);
        int numVec = Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_N));
        int vecLen = Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_L));

        ParallelOptions pops = new ParallelOptions(args, numVec);
        try {
            double[][] columnVectors = FileUtils.readVectorsInColumnOrder(dataFile, pops.myNumVec, vecLen, pops.globalVecStartIdx);
            Object[] summaries = Arrays.stream(columnVectors).parallel().map(c -> Arrays.stream(c).parallel().summaryStatistics()).toArray();
            ((DoubleSummaryStatistics)summaries[0]).
        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred ", e);
        }
    }

    /**
     * Parse command line arguments
     * @param args Command line arguments
     * @param opts Command line options
     * @return An <code>Optional&lt;CommandLine&gt;</code> object
     */
    private static com.google.common.base.Optional<org.apache.commons.cli.CommandLine> parseCommandLineArguments(String [] args, Options opts){

        CommandLineParser optParser = new GnuParser();

        try {
            return com.google.common.base.Optional.fromNullable(optParser.parse(opts, args));
        } catch (ParseException e) {
            System.out.println(e);
        }
        return com.google.common.base.Optional.fromNullable(null);
    }
}
