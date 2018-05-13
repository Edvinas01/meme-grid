package com.edd.memegrid

import com.edd.memegrid.app.Config
import com.edd.memegrid.app.DbConfig
import com.edd.memegrid.app.MemeGrid
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val LOG : Logger = LoggerFactory.getLogger("launcher")

fun main(args: Array<String>) {
    val help = Option
            .builder("h")
            .longOpt("help")
            .desc("Print help")
            .build()

    val port = Option
            .builder("p")
                .hasArg()
                .argName("int")
            .longOpt("port")
            .desc("Port to bind to")
            .build()

    val staticFileCaching = Option
            .builder("c")
            .longOpt("caching")
            .desc("Enable static file caching")
            .build()

    val domain = Option
            .builder("d")
                .hasArg()
                .argName("string")
            .longOpt("domain")
            .desc("Website domain")
            .build()

    val dbUsername = Option
            .builder("U")
                .hasArg()
                .argName("string")
            .longOpt("username")
            .desc("Database username")
            .build()

    val dbPassword = Option
            .builder("w")
                .hasArg()
                .argName("string")
            .longOpt("password")
            .desc("Database password")
            .build()

    val dbUrl = Option
            .builder("u")
                .hasArg()
                .argName("string")
            .longOpt("url")
            .desc("Database url")
            .build()

    val options = Options().apply {
        addOption(help)
        addOption(port)
        addOption(staticFileCaching)
        addOption(domain)
        addOption(dbUsername)
        addOption(dbPassword)
        addOption(dbUrl)
    }

    val cli = try {
        DefaultParser().parse(options, args)
    } catch (e: ParseException) {
        LOG.debug("Could not parse arguments", e)
        printHelp(options)
        return
    }

    if (cli.hasOption(help.opt)) {
        printHelp(options)
        return
    }

    val portValue = cli
            .getOptionValue(port.opt)
            ?.toIntOrNull()
            ?: 8080

    val config = Config(
            port = portValue,
            enableTemplateCaching = cli flagged staticFileCaching,
            domain = cli string domain ?: "http://localhost:$portValue",
            dbConfig = DbConfig(
                    username = cli string dbUsername ?: "memes",
                    password = cli string dbPassword ?: "memes",
                    url = cli string dbUrl ?: "localhost:5432/memes"
            )
    )

    MemeGrid.start(config)
}

/**
 * @return true if option is flagged in the command line or false otherwise.
 */
private infix fun CommandLine.flagged(option: Option) =
        hasOption(option.opt)

/**
 * @return option value as string.
 */
private infix fun CommandLine.string(option: Option) =
        getOptionValue(option.opt)?.toString()

/**
 * Print help for provided options.
 */
private fun printHelp(options: Options) {
    HelpFormatter().apply {
        printHelp("meme-grid.jar", options)
    }
}
