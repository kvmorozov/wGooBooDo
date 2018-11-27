package ru.kmorozov.gbd.core.config;

import org.apache.commons.cli.*;
import ru.kmorozov.gbd.core.config.options.CtxOptions;
import ru.kmorozov.gbd.core.loader.LocalFSStorage;

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
    private static final String OPTION_SECURE_MODE_SHORT = "s";
    private static final String OPTION_SECURE_MODE_LONG = "secure";
    private static final String OPTION_PDF_MODE_SHORT = "x";
    private static final String OPTION_PDF_MODE_LONG = "pdf";
    private static final String OPTION_CTX_MODE_SHORT = "c";
    private static final String OPTION_CTX_MODE_LONG = "ctx";

    private CommandLine commandLine;

    public CommandLineOptions(final String[] commandLineArguments) {
        final CommandLineParser cmdLineParser = new DefaultParser();
        final Options options = new Options();

        Option option = new Option(OPTION_BOOKID_SHORT, OPTION_BOOKID_LONG, true, "Book Id");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("BookId ");
        options.addOption(option);

        option = new Option(OPTION_OUTDIR_SHORT, OPTION_OUTDIR_LONG, true, "output dir");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("output directory ");
        options.addOption(option);

        option = new Option(OPTION_PROXY_FILE_SHORT, OPTION_PROXY_FILE_LONG, true, "Proxy list file");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("proxy options ");
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

        option = new Option(OPTION_SECURE_MODE_SHORT, OPTION_SECURE_MODE_LONG, true, "Secure mode");
        option.setArgs(0);
        option.setOptionalArg(true);
        option.setArgName("Secure mode ");
        options.addOption(option);

        option = new Option(OPTION_PDF_MODE_SHORT, OPTION_PDF_MODE_LONG, true, "Pdf mode");
        option.setArgs(1);
        option.setOptionalArg(true);
        option.setArgName("Pdf mode ");
        options.addOption(option);

        option = new Option(OPTION_CTX_MODE_SHORT, OPTION_CTX_MODE_LONG, true, "CTX mode");
        option.setArgs(2);
        option.setOptionalArg(true);
        option.setArgName("CTX mode ");
        options.addOption(option);

        try {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
    }

    private String getStringOptionValue(final String optionName) {
        return commandLine.hasOption(optionName) && null != commandLine.getOptionValues(optionName) && 1 == commandLine.getOptionValues(optionName).length
                ? commandLine.getOptionValues(optionName)[0] : null;
    }

    private String[] getStringOptionValues(final String optionName) {
        return commandLine.hasOption(optionName) && null != commandLine.getOptionValues(optionName)
                ? commandLine.getOptionValues(optionName) : null;
    }

    private int getIntOptionValue(final String optionName) {
        return commandLine.hasOption(optionName) && 1 == commandLine.getOptionValues(optionName).length ? Integer.parseInt(commandLine.getOptionValues(optionName)[0]) : 0;
    }

    private boolean getBoolOptionValue(final String optionName) {
        return commandLine.hasOption(optionName);
    }

    @Override
    public String getBookId() {
        return getStringOptionValue(OPTION_BOOKID_SHORT);
    }

    @Override
    public IStorage getStorage() {
        return new LocalFSStorage(getStringOptionValue(OPTION_OUTDIR_SHORT));
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

    @Override
    public boolean secureMode() {
        return getBoolOptionValue(OPTION_SECURE_MODE_SHORT);
    }

    @Override
    public String pdfOptions() {
        return getStringOptionValue(OPTION_PDF_MODE_SHORT);
    }

    @Override
    public CtxOptions ctxOptions() {
        String[] ctxOpts = getStringOptionValues(OPTION_CTX_MODE_SHORT);
        return ctxOpts == null || ctxOpts.length != 2 ? CtxOptions.DEFAULT_CTX_OPTIONS : new CtxOptions(ctxOpts[0], ctxOpts[1]);
    }
}
