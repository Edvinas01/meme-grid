package com.edd.memegrid.app

import java.util.concurrent.TimeUnit

data class Config(
        val port: Int,
        val staticFileExpireTimeSeconds: Long = TimeUnit.DAYS.toSeconds(1),
        val validatorTimeoutMillis: Int = TimeUnit.SECONDS.toMillis(5).toInt(),
        val staticFileDir: String = "/public/static",
        val templateDir: String = "/public/templates",
        val enableTemplateCaching: Boolean = false,
        val maxMemes: Int = 100,
        val domain: String,
        val dbConfig: DbConfig
)
