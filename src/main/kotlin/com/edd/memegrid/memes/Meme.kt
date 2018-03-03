package com.edd.memegrid.memes

import org.jetbrains.exposed.dao.IntIdTable

data class Meme(
        val title: String,
        val url: String
) : IntIdTable()
