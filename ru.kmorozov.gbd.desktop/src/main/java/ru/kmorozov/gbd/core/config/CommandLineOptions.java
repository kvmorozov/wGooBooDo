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

    public CommandLineOptions(String[] commandLineArguments) {
        CommandLineParser cmdLineParser = new DefaultParser();
        Options options = new Options();

        Option option = new Option(CommandLineOptions.OPTION_BOOKID_SHORT, CommandLineOptions.OPTION_BOOKID_LONG, true, "Book Id");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("BookId ");
        options.addOption(option);

        option = new Option(CommandLineOptions.OPTION_OUTDIR_SHORT, CommandLineOptions.OPTION_OUTDIR_LONG, true, "output dir");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("output directory ");
        options.addOption(option);

        option = new Option(CommandLineOptions.OPTION_PROXY_FILE_SHORT, CommandLineOptions.OPTION_PROXY_FILE_LONG, true, "Proxy list file");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("proxy options ");
        options.addOption(option);

        option = new Option(CommandLineOptions.OPTION_WIDTH_SHORT, CommandLineOptions.OPTION_WIDTH_LONG, true, "Width");
        option.setArgs(1);
        option.setOptionalArg(true);
        option.setArgName("Width ");
        options.addOption(option);

        option = new Option(CommandLineOptions.OPTION_IMG_RELOAD_SHORT, CommandLineOptions.OPTION_IMG_RELOAD_LONG, true, "Reload images");
        option.setArgs(0);
        option.setOptionalArg(true);
        option.setArgName("Reload images ");
        options.addOption(option);

        option = new Option(CommandLineOptions.OPTION_SECURE_MODE_SHORT, CommandLineOptions.OPTION_SECURE_MODE_LONG, true, "Secure mode");
        option.setArgs(0);
        option.setOptionalArg(true);
        option.setArgName("Secure mode ");
        options.addOption(option);

        option = new Option(CommandLineOptions.OPTION_PDF_MODE_SHORT, CommandLineOptions.OPTION_PDF_MODE_LONG, true, "Pdf mode");
        option.setArgs(1);
        option.setOptionalArg(true);
        option.setArgName("Pdf mode ");
        options.addOption(option);

        option = new Option(CommandLineOptions.OPTION_CTX_MODE_SHORT, CommandLineOptions.OPTION_CTX_MODE_LONG, true, "CTX mode");
        option.setArgs(2);
        option.setOptionalArg(true);
        option.setArgName("CTX mode ");
        options.addOption(option);

        try {
            this.commandLine = cmdLineParser.parse(options, commandLineArguments);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getStringOptionValue(String optionName) {
        return this.commandLine.hasOption(optionName) && null != this.commandLine.getOptionValues(optionName) && 1 == this.commandLine.getOptionValues(optionName).length
                ? this.commandLine.getOptionValues(optionName)[0] : null;
    }

    private String[] getStringOptionValues(String optionName) {
        return this.commandLine.hasOption(optionName) && null != this.commandLine.getOptionValues(optionName)
                ? this.commandLine.getOptionValues(optionName) : null;
    }

    private int getIntOptionValue(String optionName) {
        return this.commandLine.hasOption(optionName) && 1 == this.commandLine.getOptionValues(optionName).length ? Integer.parseInt(this.commandLine.getOptionValues(optionName)[0]) : 0;
    }

    private boolean getBoolOptionValue(String optionName) {
        return this.commandLine.hasOption(optionName);
    }

    @Override
    public String getBookId() {
        return this.getStringOptionValue(CommandLineOptions.OPTION_BOOKID_SHORT);
    }

    @Override
    public IStorage getStorage() {
        return new LocalFSStorage(this.getStringOptionValue(CommandLineOptions.OPTION_OUTDIR_SHORT));
    }

    @Override
    public String getProxyListFile() {
        return this.getStringOptionValue(CommandLineOptions.OPTION_PROXY_FILE_SHORT);
    }

    @Override
    public int getImageWidth() {
        return this.getIntOptionValue(CommandLineOptions.OPTION_WIDTH_SHORT);
    }

    @Override
    public boolean reloadImages() {
        return this.getBoolOptionValue(CommandLineOptions.OPTION_IMG_RELOAD_SHORT);
    }

    @Override
    public boolean secureMode() {
        return this.getBoolOptionValue(CommandLineOptions.OPTION_SECURE_MODE_SHORT);
    }

    @Override
    public String pdfOptions() {
        return this.getStringOptionValue(CommandLineOptions.OPTION_PDF_MODE_SHORT);
    }

    @Override
    public CtxOptions ctxOptions() {
        final String[] ctxOpts = this.getStringOptionValues(CommandLineOptions.OPTION_CTX_MODE_SHORT);
        return ctxOpts == null || ctxOpts.length != 2 ? CtxOptions.DEFAULT_CTX_OPTIONS : new CtxOptions(ctxOpts[0], ctxOpts[1]);
    }
}
