package com.edd.memegrid.memes

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MemeManager(
        private val maxMemes: Int,
        private val database: Database
) {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(MemeManager::class.java)
    }

    init {
        transaction(database) {
            SchemaUtils.create(StoredMemes)
        }
    }

    /**
     * @return meme by unique id.
     */
    fun getMeme(id: Long) = transaction(database) {
        StoredMemes
                .select { StoredMemes.id eq id }
                .singleOrNull()
                ?.let(::map)
    }

    /**
     * @return meme by unique URL.
     */
    fun getMeme(url: String) = transaction(database) {
        StoredMemes
                .select { StoredMemes.url eq url }
                .singleOrNull()
                ?.let(::map)
    }

    /**
     * @return list of latest memes from database.
     */
    fun getMemes(page: Int = 0) = transaction(database) {
        StoredMemes
                .selectAll()
                .limit(maxMemes, page * maxMemes)
                .orderBy(StoredMemes.id, isAsc = false)
                .map(::map)
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

        StoredMemes
                .select { StoredMemes.id eq id }
                .first()
                .let(::map)
    }

    /**
     * Delete meme from DB.
     */
    fun deleteMeme(meme: Meme) = transaction(database) {
        LOG.debug("Deleting meme with id: {}", meme.id)
        StoredMemes.deleteWhere { StoredMemes.id eq meme.id }
    }

    /**
     * Map internal DB meme row into DAO.
     */
    private fun map(row: ResultRow) = Meme(
            row[StoredMemes.id].value,
            row[StoredMemes.title],
            row[StoredMemes.url]
    )
}
