package org.saliya.dsctools.whitendata;

public class Constants {
    static final String PROGRAM_NAME = "WhitenData";

    static final char CMD_OPTION_SHORT_F ='f';
    static final String CMD_OPTION_LONG_F = "dataFile";
    static final String CMD_OPTION_DESCRIPTION_F = "Tab delimited data file";

    public static final char CMD_OPTION_SHORT_N = 'n';
    public static final String CMD_OPTION_LONG_N = "numVec";
    public static final String CMD_OPTION_DESCRIPTION_N = "Number of vectors";

    public static final char CMD_OPTION_SHORT_L = 'l';
    public static final String CMD_OPTION_LONG_L = "vecLen";
    public static final String CMD_OPTION_DESCRIPTION_L = "Vector length";

    public static final char CMD_OPTION_SHORT_I = 'i';
    public static final String CMD_OPTION_LONG_I = "ignoreFirstRowAndCol";
    public static final String CMD_OPTION_DESCRIPTION_I = "Ignore first row and column of vector data";

    static final String ERR_PROGRAM_ARGUMENTS_PARSING_FAILED =  "Argument parsing failed!";
    static final String ERR_INVALID_PROGRAM_ARGUMENTS =  "Invalid program arguments!";
}
