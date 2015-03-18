package org.saliya.dsctools.dist2graph;

import org.apache.commons.cli.*;

import java.util.Optional;

public class Utils {
    /**
     * Parse command line arguments
     * @param args Command line arguments
     * @param opts Command line options
     * @return An <code>Optional&lt;CommandLine&gt;</code> object
     */
    public static Optional<CommandLine> parseCommandLineArguments(String [] args, Options opts){
        CommandLineParser optParser = new GnuParser();
        try {
            return Optional.ofNullable(optParser.parse(opts, args));
        } catch (ParseException e) {
            throw new RuntimeException("Command line parsing failed ", e);
        }
    }
}
