package org.saliya.dsctools.vectorpairwise;

import mpi.Intracomm;
import mpi.MPI;
import mpi.MPIException;
import org.apache.commons.cli.*;
import org.saliya.dsctools.common.Common;

import java.util.Optional;

public class Program {
    private static Options programOptions = new Options();
    static {
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_f),Constants.CMD_OPTION_LONG_f, true,
                                 Constants.CMD_OPTION_DESCRIPTION_f);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_n),Constants.CMD_OPTION_LONG_n, true,
                                 Constants.CMD_OPTION_DESCRIPTION_n);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_l),Constants.CMD_OPTION_LONG_l, true,
                                 Constants.CMD_OPTION_DESCRIPTION_l);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_N),Constants.CMD_OPTION_LONG_N, true,
                                 Constants.CMD_OPTION_DESCRIPTION_N);
        programOptions.addOption(String.valueOf(Constants.CMD_OPTION_SHORT_w),Constants.CMD_OPTION_LONG_w, true,
                                 Constants.CMD_OPTION_DESCRIPTION_w);
    }

    private static Intracomm comm;
    private static int rank;
    private static int size;

    public static void main(String[] args) {
        Optional<CommandLine>
                parserResult = Common.parseCommandLineArguments(args, programOptions);
        if (!parserResult.isPresent()){
            System.out.println(Constants.ERR_PROGRAM_ARGUMENTS_PARSING_FAILED);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        CommandLine cmd = parserResult.get();
        if (!(cmd.hasOption(Constants.CMD_OPTION_SHORT_f) && cmd.hasOption(Constants.CMD_OPTION_SHORT_n) &&
                cmd.hasOption(Constants.CMD_OPTION_SHORT_l)&& cmd.hasOption(Constants.CMD_OPTION_SHORT_N) &&
                cmd.hasOption(Constants.CMD_OPTION_SHORT_w))) {
            System.out.println(Constants.ERR_INVALID_PROGRAM_ARGUMENTS);
            new HelpFormatter().printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        String dataFile = cmd.getOptionValue(Constants.CMD_OPTION_SHORT_f);
        int numVec = Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_n));
        int vecLen = Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_l));
        boolean normalize = Boolean.parseBoolean(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_N));
        boolean whiten = Boolean.parseBoolean(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_w));

        initializeParallelism(args);
        decomposeData()

    }

    private static void initializeParallelism(String[] args) {
        try {
            MPI.Init(args);
            comm = MPI.COMM_WORLD;
            rank = comm.getRank();
            size = comm.getSize();
        } catch (MPIException e) {
            throw new RuntimeException(e);
        }
    }


}
