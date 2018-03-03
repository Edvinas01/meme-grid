package com.edd.memegrid.web

data class DbConfig(
        val username: String,
        val password: String,
        val url: String
) {

    override fun toString() = "DbConfig(Secret)"
}
