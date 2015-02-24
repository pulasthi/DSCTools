package org.saliya.dsctools.dist2graph;

public class Constants {
    static final String PROGRAM_NAME = "Dist2Graph";

    static final char CMD_OPTION_SHORT_F ='f';
    static final String CMD_OPTION_DESCRIPTION_F = "Binary distance file";

    public static final char CMD_OPTION_SHORT_N = 'n';
    public static final String CMD_OPTION_DESCRIPTION_N = "Number of points";

    public static final char CMD_OPTION_SHORT_O = 'o';
    public static final String CMD_OPTION_DESCRIPTION_O = "Output file";

    public static final char CMD_OPTION_SHORT_M = 'm';
    public static final String CMD_OPTION_DESCRIPTION_M = "Memory mapped?";

    public static final char CMD_OPTION_SHORT_B = 'b';
    public static final String CMD_OPTION_DESCRIPTION_B = "Big-endian?";

    static final String ERR_PROGRAM_ARGUMENTS_PARSING_FAILED =  "Argument parsing failed!";
    static final String ERR_INVALID_PROGRAM_ARGUMENTS =  "Invalid program arguments!";
    static final String ERR_EMPTY_FILE_NAME = "File name is null or empty!";

    public static String errWrongNumOfBytesSkipped(int requestedBytesToSkip, int numSkippedBytes){
        String msg = "Requested %1$d bytes to skip, but could skip only %2$d bytes";
        return String.format(msg, requestedBytesToSkip, numSkippedBytes);
    }

}
