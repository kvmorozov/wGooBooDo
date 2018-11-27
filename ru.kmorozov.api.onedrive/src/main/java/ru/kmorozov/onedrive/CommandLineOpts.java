package ru.kmorozov.onedrive;

import com.google.api.client.util.Sets;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class CommandLineOpts {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    private static final Options optionsToParse = CommandLineOpts.buildOptions();
    private static final CommandLineOpts opts = new CommandLineOpts();
    private boolean isInitialised;

    // Mandatory arguments
    private CommandLineOpts.Direction direction;
    private String localPath;
    private String remotePath;

    // Optional arguments
    private boolean help;
    private boolean useHash;
    private int threads = 5;
    private int tries = 3;
    private boolean version;
    private boolean recursive;
    private int maxSizeKb;
    private Path keyFile = Paths.get("onedrive.key");
    private boolean dryRun;
    private String logFile;
    private int splitAfter = 5;
    private Set<String> ignored;
    private boolean authorise;

    public static CommandLineOpts getCommandLineOpts() {
        return CommandLineOpts.opts;
    }

    public static void initialise(String[] args) throws ParseException {

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(CommandLineOpts.optionsToParse, args);

        for (Option opt : line.getOptions()) {
            CommandLineOpts.log.debug(String.format("Parsing command line option -%s, value = %s ",
                    null != opt.getLongOpt() ? '-' + opt.getLongOpt() : opt.getOpt(),
                    opt.getValue()));
        }

        CommandLineOpts.opts.help = line.hasOption("help");
        CommandLineOpts.opts.useHash = line.hasOption("hash-compare");
        CommandLineOpts.opts.version = line.hasOption("version");
        CommandLineOpts.opts.recursive = line.hasOption("recursive");
        CommandLineOpts.opts.dryRun = line.hasOption("dry-run");
        CommandLineOpts.opts.authorise = line.hasOption("authorise");

        if (line.hasOption("local")) {
            CommandLineOpts.opts.localPath = line.getOptionValue("local");
        }

        if (line.hasOption("remote")) {
            CommandLineOpts.opts.remotePath = line.getOptionValue("remote");
        }

        if (line.hasOption("direction")) {
            String chosen = line.getOptionValue("direction").toLowerCase();
            if (!"up".equals(chosen) && !"down".equals(chosen)) {
                throw new ParseException("Direction must be one of up or down");
            }
            CommandLineOpts.opts.direction = CommandLineOpts.Direction.valueOf(chosen.toUpperCase());
        }

        if (line.hasOption("threads")) {
            CommandLineOpts.opts.threads = Integer.parseInt(line.getOptionValue("threads"));
        }

        if (line.hasOption("tries")) {
            CommandLineOpts.opts.tries = Integer.parseInt(line.getOptionValue("tries"));
        }

        if (line.hasOption("max-size")) {
            CommandLineOpts.opts.maxSizeKb = Integer.parseInt(line.getOptionValue("max-size"));
        }

        if (line.hasOption("keyfile")) {
            CommandLineOpts.opts.keyFile = Paths.get(line.getOptionValue("keyfile"));
        }

        if (line.hasOption("logfile")) {
            CommandLineOpts.opts.logFile = line.getOptionValue("logfile");
        }

        if (line.hasOption("split-after")) {
            CommandLineOpts.opts.splitAfter = Integer.parseInt(line.getOptionValue("split-after"));

            if (60 < CommandLineOpts.opts.splitAfter) {
                throw new ParseException("maximum permissible value for split-after is 60");
            }
        }

        if (line.hasOption("ignore")) {
            Path ignoreFile = Paths.get(line.getOptionValue("ignore"));
            if (!Files.exists(ignoreFile)) {
                throw new ParseException("specified ignore file does not exist");
            }

            try {
                CommandLineOpts.opts.ignored = Sets.newHashSet();
                CommandLineOpts.opts.ignored.addAll(Files.readAllLines(ignoreFile, Charset.defaultCharset()));
            } catch (IOException e) {
                throw new ParseException(e.getMessage());
            }
        }

        CommandLineOpts.opts.isInitialised = true;
    }

    private static Options buildOptions() {
        Option authorise = Option.builder("a")
                .longOpt("authorise")
                .desc("generate authorisation url")
                .build();

        Option hash = Option.builder("c")
                .longOpt("hash-compare")
                .desc("always compare files by hash")
                .build();

        Option direction = Option.builder()
                .longOpt("direction")
                .hasArg()
                .argName("up|down")
                .desc("direction of synchronisation.")
                .build();

        Option help = Option.builder("h")
                .longOpt("help")
                .desc("print this message")
                .build();

        Option ignore = Option.builder("i")
                .longOpt("ignore")
                .hasArg()
                .argName("ignore_file")
                .desc("ignore entry file")
                .build();

        Option keyFile = Option.builder("k")
                .longOpt("keyfile")
                .hasArg()
                .argName("file")
                .desc("key file to use")
                .build();

        Option logLevel = Option.builder("L")
                .longOpt("log-level")
                .hasArg()
                .argName("level (1-7)")
                .desc("controls the verbosity of logging")
                .build();

        Option localPath = Option.builder()
                .longOpt("local")
                .hasArg()
                .argName("path")
                .desc("the local path")
                .build();

        Option logFile = Option.builder()
                .longOpt("logfile")
                .hasArg()
                .argName("file")
                .desc("log to file")
                .build();

        Option maxSize = Option.builder("M")
                .longOpt("max-size")
                .hasArg()
                .argName("size_in_KB")
                .desc("only process files smaller than <size> KB")
                .build();

        Option dryRun = Option.builder("n")
                .longOpt("dry-run")
                .desc("only do a dry run without making changes")
                .build();

        Option recursive = Option.builder("r")
                .longOpt("recursive")
                .desc("recurse into directories")
                .build();

        Option remotePath = Option.builder()
                .longOpt("remote")
                .hasArg()
                .argName("path")
                .desc("the remote path on OneDrive")
                .build();

        Option splitAfter = Option.builder("s")
                .longOpt("split-after")
                .hasArg()
                .argName("size_in_MB")
                .desc("use multi-part upload for big files")
                .build();

        Option threads = Option.builder("t")
                .longOpt("threads")
                .hasArg()
                .argName("count")
                .desc("number of threads to use")
                .build();

        Option version = Option.builder("v")
                .longOpt("version")
                .desc("print the version information and exit")
                .build();

        Option retries = Option.builder("y")
                .longOpt("tries")
                .hasArg()
                .argName("count")
                .desc("try each service request <count> times")
                .build();

        return new Options()
                .addOption(authorise)
                .addOption(hash)
                .addOption(direction)
                .addOption(help)
                .addOption(ignore)
                .addOption(keyFile)
                .addOption(logLevel)
                .addOption(localPath)
                .addOption(logFile)
                .addOption(maxSize)
                .addOption(dryRun)
                .addOption(recursive)
                .addOption(remotePath)
                .addOption(splitAfter)
                .addOption(threads)
                .addOption(version)
                .addOption(retries);
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("onedrive-java-syncer", CommandLineOpts.optionsToParse);
    }

    public CommandLineOpts.Direction getDirection() {
        return this.direction;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public String getRemotePath() {
        return this.remotePath;
    }

    public boolean help() {
        return this.help;
    }

    public int getThreads() {
        return this.threads;
    }

    public boolean useHash() {
        return this.useHash;
    }

    public int getTries() {
        return this.tries;
    }

    public boolean version() {
        return this.version;
    }

    public boolean isRecursive() {
        return this.recursive;
    }

    public int getMaxSizeKb() {
        return this.maxSizeKb;
    }

    public Path getKeyFile() {
        return this.keyFile;
    }

    public boolean isDryRun() {
        return this.dryRun;
    }

    public String getLogFile() {
        return this.logFile;
    }

    public int getSplitAfter() {
        return this.splitAfter;
    }

    public Set<String> getIgnored() {
        return this.ignored;
    }

    public boolean isAuthorise() {
        return this.authorise;
    }

    public enum Direction {
        UP,
        DOWN
    }
}
