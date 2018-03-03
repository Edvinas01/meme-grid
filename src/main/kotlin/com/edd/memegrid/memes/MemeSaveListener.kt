package com.edd.memegrid.memes

interface MemeSaveListener {

    /**
     * Handle saved meme event.
     *
     * @param meme meme which was just saved.
     */
    fun onMemeSaved(meme: Meme)
}
