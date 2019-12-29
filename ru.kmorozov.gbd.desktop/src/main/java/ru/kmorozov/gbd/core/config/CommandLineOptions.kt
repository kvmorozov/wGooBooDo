package ru.kmorozov.gbd.core.config

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import ru.kmorozov.gbd.core.config.options.AuthOptions
import ru.kmorozov.gbd.core.config.options.CtxOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage

/**
 * Created by km on 06.12.2015.
 */
class CommandLineOptions(commandLineArguments: Array<String>) : IGBDOptions {

    private val commandLine: CommandLine

    override val bookId: String
        get() = getStringOptionValue(OPTION_BOOKID_SHORT)

    override val storage: IStorage
        get() = LocalFSStorage.getStorage(getStringOptionValue(OPTION_OUTDIR_SHORT))

    override val proxyListFile: String
        get() = getStringOptionValue(OPTION_PROXY_FILE_SHORT)

    override val imageWidth: Int
        get() = getIntOptionValue(OPTION_WIDTH_SHORT)

    init {
        val cmdLineParser = DefaultParser()
        val options = Options()

        var option = Option(OPTION_BOOKID_SHORT, OPTION_BOOKID_LONG, true, "Book Id")
        option.args = 1
        option.setOptionalArg(false)
        option.argName = "BookId "
        options.addOption(option)

        option = Option(OPTION_OUTDIR_SHORT, OPTION_OUTDIR_LONG, true, "output dir")
        option.args = 1
        option.setOptionalArg(false)
        option.argName = "output directory "
        options.addOption(option)

        option = Option(OPTION_PROXY_FILE_SHORT, OPTION_PROXY_FILE_LONG, true, "Proxy list file")
        option.args = 1
        option.setOptionalArg(false)
        option.argName = "proxy options "
        options.addOption(option)

        option = Option(OPTION_WIDTH_SHORT, OPTION_WIDTH_LONG, true, "Width")
        option.args = 1
        option.setOptionalArg(true)
        option.argName = "Width "
        options.addOption(option)

        option = Option(OPTION_IMG_RELOAD_SHORT, OPTION_IMG_RELOAD_LONG, true, "Reload images")
        option.args = 0
        option.setOptionalArg(true)
        option.argName = "Reload images "
        options.addOption(option)

        option = Option(OPTION_SECURE_MODE_SHORT, OPTION_SECURE_MODE_LONG, true, "Secure mode")
        option.args = 0
        option.setOptionalArg(true)
        option.argName = "Secure mode "
        options.addOption(option)

        option = Option(OPTION_DEBUG_MODE_SHORT, OPTION_DEBUG_MODE_LONG, true, "Debug mode")
        option.args = 0
        option.setOptionalArg(true)
        option.argName = "Debug mode "
        options.addOption(option)

        option = Option(OPTION_PDF_MODE_SHORT, OPTION_PDF_MODE_LONG, true, "Pdf mode")
        option.args = 1
        option.setOptionalArg(true)
        option.argName = "Pdf mode "
        options.addOption(option)

        option = Option(OPTION_SCAN_MODE_SHORT, OPTION_SCAN_MODE_LONG, true, "Scan mode")
        option.args = 0
        option.setOptionalArg(true)
        option.argName = "Scan mode "
        options.addOption(option)

        option = Option(OPTION_CTX_MODE_SHORT, OPTION_CTX_MODE_LONG, true, "CTX mode")
        option.args = 2
        option.setOptionalArg(true)
        option.argName = "CTX mode "
        options.addOption(option)

        option = Option(OPTION_AUTH_MODE_SHORT, OPTION_AUTH_MODE_LONG, true, "authentication params")
        option.args = 2
        option.setOptionalArg(true)
        option.argName = "authentication params "
        options.addOption(option)

        commandLine = cmdLineParser.parse(options, commandLineArguments)
    }

    private fun getStringOptionValue(optionName: String): String {
        return if (commandLine.hasOption(optionName) && null != commandLine.getOptionValues(optionName) && 1 == commandLine.getOptionValues(optionName).size)
            commandLine.getOptionValues(optionName)[0]
        else
            ""
    }

    private fun getStringOptionValues(optionName: String): Array<String>? {
        return if (commandLine.hasOption(optionName) && null != commandLine.getOptionValues(optionName))
            commandLine.getOptionValues(optionName)
        else
            null
    }

    private fun getIntOptionValue(optionName: String): Int {
        return if (commandLine.hasOption(optionName) && 1 == commandLine.getOptionValues(optionName).size) Integer.parseInt(commandLine.getOptionValues(optionName)[0]) else 0
    }

    private fun getBoolOptionValue(optionName: String): Boolean {
        return commandLine.hasOption(optionName)
    }

    override val reloadImages: Boolean
        get() = getBoolOptionValue(OPTION_IMG_RELOAD_SHORT)

    override val secureMode: Boolean
        get() = getBoolOptionValue(OPTION_SECURE_MODE_SHORT)

    override val debugEnabled: Boolean
        get() = getBoolOptionValue(OPTION_DEBUG_MODE_SHORT)

    override val scanEnabled: Boolean
        get() = getBoolOptionValue(OPTION_SCAN_MODE_SHORT)

    override fun pdfOptions(): String {
        return getStringOptionValue(OPTION_PDF_MODE_SHORT)
    }

    override fun ctxOptions(): CtxOptions {
        val ctxOpts = getStringOptionValues(OPTION_CTX_MODE_SHORT)
        return if (ctxOpts == null || ctxOpts.size != 2) CtxOptions.DEFAULT_CTX_OPTIONS else CtxOptions(ctxOpts[0], ctxOpts[1])
    }

    override fun authOptions(): AuthOptions? {
        val authOpts = getStringOptionValues(OPTION_AUTH_MODE_SHORT)
        return if (authOpts == null || authOpts.size != 2) null else AuthOptions(authOpts[0], authOpts[1])
    }

    companion object {

        private const val OPTION_BOOKID_SHORT = "i"
        private const val OPTION_BOOKID_LONG = "bookId"
        private const val OPTION_OUTDIR_SHORT = "o"
        private const val OPTION_OUTDIR_LONG = "out"
        private const val OPTION_PROXY_FILE_SHORT = "p"
        private const val OPTION_PROXY_FILE_LONG = "proxy"
        private const val OPTION_WIDTH_SHORT = "w"
        private const val OPTION_WIDTH_LONG = "width"
        private const val OPTION_IMG_RELOAD_SHORT = "r"
        private const val OPTION_IMG_RELOAD_LONG = "reloadImages"
        private const val OPTION_SECURE_MODE_SHORT = "s"
        private const val OPTION_SECURE_MODE_LONG = "secure"
        private const val OPTION_PDF_MODE_SHORT = "x"
        private const val OPTION_PDF_MODE_LONG = "pdf"
        private const val OPTION_CTX_MODE_SHORT = "c"
        private const val OPTION_CTX_MODE_LONG = "ctx"
        private const val OPTION_AUTH_MODE_SHORT = "a"
        private const val OPTION_AUTH_MODE_LONG = "auth"
        private const val OPTION_DEBUG_MODE_SHORT = "d"
        private const val OPTION_DEBUG_MODE_LONG = "debug"
        private const val OPTION_SCAN_MODE_SHORT = "S"
        private const val OPTION_SCAN_MODE_LONG = "Scan"
    }
}
