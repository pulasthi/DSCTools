package org.saliya.dsctools.dist2graph;

/**
 * Constants
 *
 * @author esaliya@gmail.com (Saliya Ekanayake)
 */
public class Constants {

    public static final String PROGRAM_NAME = "org.saliya.dsctools.dist2graph.Program";
    public static final String MODE_GRAPH = "graph"; // Convert distance file to graph represenation
    public static final String MODE_FIX = "fix"; // Fix missing distances to Short.MAX_VALUE

    public static final char CMD_OPTION_SHORT_F ='f';
    public static final String CMD_OPTION_DESCRIPTION_F = "Binary distance file";

    public static final char CMD_OPTION_SHORT_N = 'n';
    public static final String CMD_OPTION_DESCRIPTION_N = "Number of points";

    public static final char CMD_OPTION_SHORT_O = 'o';
    public static final String CMD_OPTION_DESCRIPTION_O = "Output file";

    public static final char CMD_OPTION_SHORT_M = 'm';
    public static final String CMD_OPTION_DESCRIPTION_M = "Memory mapped? Must be true with " + MODE_FIX + " mode";

    public static final char CMD_OPTION_SHORT_B = 'b';
    public static final String CMD_OPTION_DESCRIPTION_B = "Big-endian?";

    public static final char CMD_OPTION_SHORT_MODE = 'M';
    public static final String CMD_OPTION_DESCRIPTION_MODE = "Select mode [" + MODE_GRAPH + " | " + MODE_FIX + "]";

    public static final String ERR_PROGRAM_ARGUMENTS_PARSING_FAILED =  "Argument parsing failed!";
    public static final String ERR_INVALID_PROGRAM_ARGUMENTS =  "Invalid program arguments!";
    public static final String ERR_EMPTY_FILE_NAME = "File name is null or empty!";

    public static String errWrongNumOfBytesSkipped(int requestedBytesToSkip, int numSkippedBytes){
        String msg = "Requested %1$d bytes to skip, but could skip only %2$d bytes";
        return String.format(msg, requestedBytesToSkip, numSkippedBytes);
    }

}
