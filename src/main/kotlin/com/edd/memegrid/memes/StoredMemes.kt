package com.edd.memegrid.memes

import org.jetbrains.exposed.dao.LongIdTable

object StoredMemes : LongIdTable("meme") {
    val title = varchar("title", 256)
    val url = varchar("url", 1024).uniqueIndex()
}
