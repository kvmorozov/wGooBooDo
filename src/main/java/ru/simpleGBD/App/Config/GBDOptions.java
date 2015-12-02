package ru.simpleGBD.App.Config;

import org.apache.commons.cli.*;

/**
 * Created by km on 01.12.2015.
 */
public class GBDOptions {

    private static final String OPTION_BOOKID_SHORT = "i";
    private static final String OPTION_BOOKID_LONG = "bookId";
    private static final String OPTION_OUTDIR_SHORT = "o";
    private static final String OPTION_OUTDIR_LONG = "out";
    private static final String OPTION_PROXY_FILE_SHORT = "p";
    private static final String OPTION_PROXY_FILE_LONG = "proxy";

    private static GBDOptions INSTANCE;
    private CommandLine commandLine;

    private GBDOptions(String[] commandLineArguments) {
        CommandLineParser cmdLineParser = new DefaultParser();
        Options options = new Options();

        Option option = new Option(OPTION_BOOKID_SHORT, OPTION_BOOKID_LONG, true, "Book Id");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("Google bookId ");
        options.addOption(option);

        option = new Option(OPTION_OUTDIR_SHORT, OPTION_OUTDIR_LONG, true, "Output dir");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("Output directory ");
        options.addOption(option);

        option = new Option(OPTION_PROXY_FILE_SHORT, OPTION_PROXY_FILE_LONG, true, "Proxy list file");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("Output directory ");
        options.addOption(option);

        try {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static GBDOptions getGBDOptions(String[] commandLineArguments) {
        return INSTANCE == null ? INSTANCE = new GBDOptions(commandLineArguments) : INSTANCE;
    }

    public static GBDOptions getGBDOptions() {
        if (INSTANCE == null || INSTANCE.commandLine == null)
            throw new RuntimeException("Куда-то делись опции!");

        return INSTANCE;
    }

    private String getStringOptionValue(String optionName) {
        return commandLine.hasOption(optionName) && commandLine.getOptionValues(optionName).length == 1
                ? commandLine.getOptionValues(optionName)[0]
                : null;
    }

    public String getBookId() {return getStringOptionValue(OPTION_BOOKID_SHORT);}
    public String getOutputDir() {return getStringOptionValue(OPTION_OUTDIR_SHORT);}
}
