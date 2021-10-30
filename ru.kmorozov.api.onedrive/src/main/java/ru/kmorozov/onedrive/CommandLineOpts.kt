package ru.kmorozov.onedrive

import com.google.api.client.util.Sets
import org.apache.commons.cli.*
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class CommandLineOpts {
    private var isInitialised: Boolean = false

    // Mandatory arguments
    var direction: Direction? = null
        private set
    var localPath: String? = null
        private set
    var remotePath: String? = null
        private set

    // Optional arguments
    private var help: Boolean = false
    private var useHash: Boolean = false
    var threads = 5
        private set
    var tries = 3
        private set
    private var version: Boolean = false
    var isRecursive: Boolean = false
        private set
    var maxSizeKb: Int = 0
        private set
    var keyFile = Paths.get("onedrive.key")
        private set
    var isDryRun: Boolean = false
        private set
    var logFile: String? = null
        private set
    var splitAfter = 5
        private set
    private var ignored: MutableSet<String>? = null
    var isAuthorise: Boolean = false
        private set

    fun help(): Boolean {
        return help
    }

    fun useHash(): Boolean {
        return useHash
    }

    fun version(): Boolean {
        return version
    }

    fun getIgnored(): Set<String>? {
        return ignored
    }

    enum class Direction {
        UP,
        DOWN
    }

    companion object {

        private val log = LogManager.getLogger(Main::class.java.name)

        private val optionsToParse = buildOptions()
        val commandLineOpts = CommandLineOpts()

        @Throws(ParseException::class)
        @JvmStatic
        fun initialise(args: Array<String>) {

            val parser = DefaultParser()
            val line = parser.parse(optionsToParse, args)

            for (opt in line.options) {
                log.debug("Parsing command line option -${if (null != opt.longOpt) '-' + opt.longOpt else opt.opt}, value = ${opt.value} ")
            }

            commandLineOpts.help = line.hasOption("help")
            commandLineOpts.useHash = line.hasOption("hash-compare")
            commandLineOpts.version = line.hasOption("version")
            commandLineOpts.isRecursive = line.hasOption("recursive")
            commandLineOpts.isDryRun = line.hasOption("dry-run")
            commandLineOpts.isAuthorise = line.hasOption("authorise")

            if (line.hasOption("local")) {
                commandLineOpts.localPath = line.getOptionValue("local")
            }

            if (line.hasOption("remote")) {
                commandLineOpts.remotePath = line.getOptionValue("remote")
            }

            if (line.hasOption("direction")) {
                val chosen = line.getOptionValue("direction").toLowerCase()
                if ("up" != chosen && "down" != chosen) {
                    throw ParseException("Direction must be one of up or down")
                }
                commandLineOpts.direction = Direction.valueOf(chosen.toUpperCase())
            }

            if (line.hasOption("threads")) {
                commandLineOpts.threads = Integer.parseInt(line.getOptionValue("threads"))
            }

            if (line.hasOption("tries")) {
                commandLineOpts.tries = Integer.parseInt(line.getOptionValue("tries"))
            }

            if (line.hasOption("max-size")) {
                commandLineOpts.maxSizeKb = Integer.parseInt(line.getOptionValue("max-size"))
            }

            if (line.hasOption("keyfile")) {
                commandLineOpts.keyFile = Paths.get(line.getOptionValue("keyfile"))
            }

            if (line.hasOption("logfile")) {
                commandLineOpts.logFile = line.getOptionValue("logfile")
            }

            if (line.hasOption("split-after")) {
                commandLineOpts.splitAfter = Integer.parseInt(line.getOptionValue("split-after"))

                if (60 < commandLineOpts.splitAfter) {
                    throw ParseException("maximum permissible value for split-after is 60")
                }
            }

            if (line.hasOption("ignore")) {
                val ignoreFile = Paths.get(line.getOptionValue("ignore"))
                if (!Files.exists(ignoreFile)) {
                    throw ParseException("specified ignore file does not exist")
                }

                try {
                    commandLineOpts.ignored = Sets.newHashSet()
                    commandLineOpts.ignored!!.addAll(Files.readAllLines(ignoreFile, Charset.defaultCharset()))
                } catch (e: IOException) {
                    throw ParseException(e.message)
                }

            }

            commandLineOpts.isInitialised = true
        }

        private fun buildOptions(): Options {
            val authorise = Option.builder("a")
                    .longOpt("authorise")
                    .desc("generate authorisation url")
                    .build()

            val hash = Option.builder("c")
                    .longOpt("hash-compare")
                    .desc("always compare files by hash")
                    .build()

            val direction = Option.builder()
                    .longOpt("direction")
                    .hasArg()
                    .argName("up|down")
                    .desc("direction of synchronisation.")
                    .build()

            val help = Option.builder("h")
                    .longOpt("help")
                    .desc("print this message")
                    .build()

            val ignore = Option.builder("i")
                    .longOpt("ignore")
                    .hasArg()
                    .argName("ignore_file")
                    .desc("ignore entry file")
                    .build()

            val keyFile = Option.builder("k")
                    .longOpt("keyfile")
                    .hasArg()
                    .argName("file")
                    .desc("key file to use")
                    .build()

            val logLevel = Option.builder("L")
                    .longOpt("log-level")
                    .hasArg()
                    .argName("level (1-7)")
                    .desc("controls the verbosity of logging")
                    .build()

            val localPath = Option.builder()
                    .longOpt("local")
                    .hasArg()
                    .argName("path")
                    .desc("the local path")
                    .build()

            val logFile = Option.builder()
                    .longOpt("logfile")
                    .hasArg()
                    .argName("file")
                    .desc("log to file")
                    .build()

            val maxSize = Option.builder("M")
                    .longOpt("max-size")
                    .hasArg()
                    .argName("size_in_KB")
                    .desc("only process files smaller than <size> KB")
                    .build()

            val dryRun = Option.builder("n")
                    .longOpt("dry-run")
                    .desc("only do a dry run without making changes")
                    .build()

            val recursive = Option.builder("r")
                    .longOpt("recursive")
                    .desc("recurse into directories")
                    .build()

            val remotePath = Option.builder()
                    .longOpt("remote")
                    .hasArg()
                    .argName("path")
                    .desc("the remote path on OneDrive")
                    .build()

            val splitAfter = Option.builder("s")
                    .longOpt("split-after")
                    .hasArg()
                    .argName("size_in_MB")
                    .desc("use multi-part upload for big files")
                    .build()

            val threads = Option.builder("t")
                    .longOpt("threads")
                    .hasArg()
                    .argName("count")
                    .desc("number of threads to use")
                    .build()

            val version = Option.builder("v")
                    .longOpt("version")
                    .desc("print the version information and exit")
                    .build()

            val retries = Option.builder("y")
                    .longOpt("tries")
                    .hasArg()
                    .argName("count")
                    .desc("try each service request <count> times")
                    .build()

            return Options()
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
                    .addOption(retries)
        }

        fun printHelp() {
            val formatter = HelpFormatter()
            formatter.printHelp("onedrive-java-syncer", optionsToParse)
        }
    }
}
