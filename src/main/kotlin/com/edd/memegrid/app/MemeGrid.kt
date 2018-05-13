package com.edd.memegrid.app

import com.edd.memegrid.memes.MemeManager
import com.edd.memegrid.util.ImageValidator
import com.edd.memegrid.util.TemplateReader
import com.edd.memegrid.web.Router
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import spark.Spark.port
import spark.Spark.staticFiles

object MemeGrid {

    private val LOG = LoggerFactory.getLogger(MemeGrid::class.java)

    /**
     * Start the meme grid.
     */
    fun start(config: Config) {
        LOG.debug("Starting application using config: {}", config)

        val database = connectDatabase(config.dbConfig)

        val memeManager = MemeManager(config.maxMemes, database)

        val contentReader = TemplateReader(
                enableCaching = config.enableTemplateCaching,
                templateDir = config.templateDir
        )

        staticFiles.expireTime(config.staticFileExpireTimeSeconds)
        staticFiles.location(config.staticFileDir)

        // Note that port mapping must go before route creation.
        port(config.port)

        Router(
                ImageValidator(config.validatorTimeoutMillis),
                memeManager,
                contentReader,
                config.domain
        ).start()

        LOG.debug("Application started on port: {}", config.port)
    }

    /**
     * Connect to PostgresSQL database.
     */
    private fun connectDatabase(config: DbConfig) = config.run {
        val url = "jdbc:postgresql://$url"

        LOG.debug("Connecting to DB: {}", url)
        val database = Database.connect(
                user = username,
                password = password,
                url = url,
                driver = "org.postgresql.Driver"
        )

        LOG.debug("Connected to DB: {}, version: {}", database.vendor, database.version)
        database
    }
}
