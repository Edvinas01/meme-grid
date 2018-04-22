package com.edd.memegrid.memes

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class MemeFetcher(
        private val memeCaching: Boolean,
        private val maxMemes: Int,
        private val database: Database
) : MemeSaveListener {

    private val cachedMemes = mutableListOf<Meme>()

    /**
     * @return list of latest memes from database.
     */
    fun getMemes(): List<Meme> {
        if (cachedMemes.isNotEmpty()) {
            return cachedMemes
        }

        return transaction(database) {
            val memes = StoredMemes
                    .selectAll()
                    .limit(maxMemes)
                    .orderBy(StoredMemes.id, isAsc = false).map {

                Meme(
                        it[StoredMemes.id].value,
                        it[StoredMemes.title],
                        it[StoredMemes.url]
                )
            }

            if (memeCaching) {
                synchronized(cachedMemes) {
                    cachedMemes.clear()
                    cachedMemes.addAll(memes)
                }
            }
            memes
        }
    }

    override fun onMemeSaved(meme: Meme) {
        cachedMemes.clear()
    }
}
