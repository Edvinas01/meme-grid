package com.edd.memegrid.app

import java.util.concurrent.TimeUnit

data class Config(
        val port: Int,
        val staticFileExpireTimeSeconds: Long = TimeUnit.DAYS.toSeconds(1),
        val staticFileDir: String = "/public/static",
        val enableTemplateCaching: Boolean = false,
        val enableMemeCaching: Boolean = false,
        val templateDir: String = "/public/templates",
        val maxMemes: Int = 100,
        val dbConfig: DbConfig
)
