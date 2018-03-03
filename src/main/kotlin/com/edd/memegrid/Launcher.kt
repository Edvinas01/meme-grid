package com.edd.memegrid

import com.edd.memegrid.web.Config
import com.edd.memegrid.web.DbConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.Option

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

    val memeCaching = Option
            .builder("C")
            .longOpt("meme-caching")
            .desc("Enable meme caching")
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
        addOption(dbUsername)
        addOption(dbPassword)
        addOption(dbUrl)
    }

    val cli = DefaultParser().parse(options, args)

    if (cli.hasOption(help.opt)) {
        HelpFormatter().apply {
            printHelp("meme-grid.jar", options)
        }
        return
    }

    val config = Config(
            port = cli.getOptionValue(port.opt)?.toIntOrNull() ?: 8080,
            enableTemplateCaching = cli.isFlagged(staticFileCaching),
            enableMemeCaching = cli.isFlagged(memeCaching),
            dbConfig = DbConfig(
                    username = cli.asString(dbUsername) ?: "memes",
                    password = cli.asString(dbPassword) ?: "memes",
                    url = cli.asString(dbUrl) ?: "localhost:5432/memes"
            )
    )

    Application.start(config)
}

private fun CommandLine.isFlagged(option: Option) =
        hasOption(option.opt)

/**
 * @return option value as string.
 */
private fun CommandLine.asString(option: Option) =
        getOptionValue(option.opt)?.toString()
