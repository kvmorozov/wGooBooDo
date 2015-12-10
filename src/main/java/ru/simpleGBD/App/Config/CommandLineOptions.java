package ru.simpleGBD.App.Config;

import org.apache.commons.cli.*;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

/**
 * Created by km on 06.12.2015.
 */
public class CommandLineOptions implements IGBDOptions {

    private static final String OPTION_BOOKID_SHORT = "i";
    private static final String OPTION_BOOKID_LONG = "bookId";
    private static final String OPTION_OUTDIR_SHORT = "o";
    private static final String OPTION_OUTDIR_LONG = "out";
    private static final String OPTION_PROXY_FILE_SHORT = "p";
    private static final String OPTION_PROXY_FILE_LONG = "proxy";
    private static final String OPTION_WIDTH_SHORT = "w";
    private static final String OPTION_WIDTH_LONG = "width";
    private static final String OPTION_IMG_RELOAD_SHORT = "r";
    private static final String OPTION_IMG_RELOAD_LONG = "reload";

    private CommandLine commandLine;

    public CommandLineOptions(String[] commandLineArguments) {
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

        option = new Option(OPTION_WIDTH_SHORT, OPTION_WIDTH_LONG, true, "Width");
        option.setArgs(1);
        option.setOptionalArg(true);
        option.setArgName("Width ");
        options.addOption(option);

        option = new Option(OPTION_IMG_RELOAD_SHORT, OPTION_IMG_RELOAD_LONG, true, "Reload images");
        option.setArgs(0);
        option.setOptionalArg(true);
        option.setArgName("Reload images ");
        options.addOption(option);

        try {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getStringOptionValue(String optionName) {
        return commandLine.hasOption(optionName) && commandLine.getOptionValues(optionName).length == 1
                ? commandLine.getOptionValues(optionName)[0]
                : null;
    }

    private int getIntOptionValue(String optionName) {
        return commandLine.hasOption(optionName) && commandLine.getOptionValues(optionName).length == 1
                ? Integer.parseInt(commandLine.getOptionValues(optionName)[0])
                : ImageExtractor.DEFAULT_PAGE_WIDTH;
    }

    private boolean getBoolOptionValue(String optionName) {
        return commandLine.hasOption(optionName);
    }

    @Override
    public String getBookId() {
        return getStringOptionValue(OPTION_BOOKID_SHORT);
    }

    @Override
    public String getOutputDir() {
        return getStringOptionValue(OPTION_OUTDIR_SHORT);
    }

    @Override
    public String getProxyListFile() {
        return getStringOptionValue(OPTION_PROXY_FILE_SHORT);
    }

    @Override
    public int getImageWidth() {
        return getIntOptionValue(OPTION_WIDTH_SHORT);
    }

    @Override
    public boolean reloadImages() {
        return getBoolOptionValue(OPTION_IMG_RELOAD_SHORT);
    }
}
