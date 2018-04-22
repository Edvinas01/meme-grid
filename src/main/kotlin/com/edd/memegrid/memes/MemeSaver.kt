package com.edd.memegrid.memes

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MemeSaver(
        private val database: Database,
        private val memeSaveListeners: List<MemeSaveListener>
) {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(MemeSaver::class.java)
    }

    init {
        transaction(database) {
            create(StoredMemes)
        }
    }

    /**
     * Persist a new meme to the database.
     *
     * @return persisted meme.
     */
    fun saveMeme(title: String, url: String) = transaction(database) {
        LOG.debug("Saving new meme, title: {}, url: {}", title, url)

        val id = StoredMemes.insertAndGetId {
            it[StoredMemes.title] = title
            it[StoredMemes.url] = url
        }.value

        StoredMemes.select {
            StoredMemes.id eq id
        }.first().let { row ->
            val saved = Meme(
                    row[StoredMemes.id].value,
                    row[StoredMemes.title],
                    row[StoredMemes.url]
            )

            memeSaveListeners.forEach { listener ->
                listener.onMemeSaved(saved)
            }
            saved
        }
    }
}
