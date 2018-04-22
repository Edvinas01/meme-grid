package com.edd.memegrid.app

data class DbConfig(
        val username: String,
        val password: String,
        val url: String
) {

    override fun toString() = "${DbConfig::class.simpleName}(Secret)"
}
