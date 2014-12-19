package org.saliya.dsctools.vectorpairwise;

public class Constants {
    static final String PROGRAM_NAME = "VectorPairwise";

    static final char CMD_OPTION_SHORT_f ='f';
    static final String CMD_OPTION_LONG_f = "dataFile";
    static final String CMD_OPTION_DESCRIPTION_f = "Tab delimited data file";

    public static final char CMD_OPTION_SHORT_n = 'n';
    public static final String CMD_OPTION_LONG_n = "numVec";
    public static final String CMD_OPTION_DESCRIPTION_n = "Number of vectors";

    public static final char CMD_OPTION_SHORT_l = 'l';
    public static final String CMD_OPTION_LONG_l = "vecLen";
    public static final String CMD_OPTION_DESCRIPTION_l = "Vector length";

    public static final char CMD_OPTION_SHORT_N = 'N';
    public static final String CMD_OPTION_LONG_N = "normalize";
    public static final String CMD_OPTION_DESCRIPTION_N = "Normalize distances by the max distance";

    public static final char CMD_OPTION_SHORT_w = 'w';
    public static final String CMD_OPTION_LONG_w = "whiten";
    public static final String CMD_OPTION_DESCRIPTION_w = "Whiten vectors to have zero mean and unit standard deviation";

    static final String ERR_PROGRAM_ARGUMENTS_PARSING_FAILED =  "Argument parsing failed!";
    static final String ERR_INVALID_PROGRAM_ARGUMENTS =  "Invalid program arguments!";
}
