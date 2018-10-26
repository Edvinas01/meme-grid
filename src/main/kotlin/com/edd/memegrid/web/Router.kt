package com.edd.memegrid.web

import com.edd.memegrid.memes.Meme
import com.edd.memegrid.memes.MemeManager
import com.edd.memegrid.util.BadMemeException
import com.edd.memegrid.util.BadPageException
import com.edd.memegrid.util.ImageValidator
import com.edd.memegrid.util.MEDIA_TYPE_HTML
import com.edd.memegrid.util.MEDIA_TYPE_JSON
import com.edd.memegrid.util.MemeNotFoundException
import com.edd.memegrid.util.STATUS_CODE_BAD_REQUEST
import com.edd.memegrid.util.STATUS_CODE_CREATED
import com.edd.memegrid.util.STATUS_CODE_NOT_FOUND
import com.edd.memegrid.util.STATUS_CODE_SERVER_ERROR
import com.edd.memegrid.util.STATUS_CODE_UNSUPPORTED_MEDIA_TYPE
import com.edd.memegrid.util.TemplateReader
import com.edd.memegrid.util.accept
import com.edd.memegrid.util.deleteJson
import com.edd.memegrid.util.getJson
import com.edd.memegrid.util.html
import com.edd.memegrid.util.intParam
import com.edd.memegrid.util.json
import com.edd.memegrid.util.jsonBody
import com.edd.memegrid.util.jsonError
import com.edd.memegrid.util.jsonException
import com.edd.memegrid.util.longParam
import com.edd.memegrid.util.postJson
import com.edd.memegrid.util.string
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Spark.exception
import spark.Spark.get

class Router(
        private val imageValidator: ImageValidator,
        private val memeManager: MemeManager,
        private val reader: TemplateReader,
        private val domain: String
) {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(Router::class.java)
    }

    fun start() {

        // Index page doesn't change.
        val index = reader.read("index", mapOf("domain" to domain))

        get("/") { req, res ->
            res.type(MEDIA_TYPE_HTML)

            if (req.html) {
                index
            } else {
                res.status(STATUS_CODE_UNSUPPORTED_MEDIA_TYPE)
                reader.read("415", mapOf("type" to req.accept))
            }
        }

        getJson("/api/memes") { _, _ ->
            memeManager.getMemes().json
        }

        getJson("/api/memes/page/:page") { req, _ ->
            memeManager.getMemes(req intParam "page").json
        }

        getJson("/api/memes/:id") { req, _ ->
            getMeme(req longParam "id").json
        }

        postJson("/api/memes") { req, res ->
            val json = req.jsonBody
            val url = (json string "url").trim()

            if (!imageValidator.isValid(url)) {
                throw BadMemeException("URL: $url, is invalid")
            }

            if (memeManager.getMeme(url) != null) {
                throw BadMemeException("Meme with URL: $url, already exists")
            }

            val saved = memeManager.saveMeme(
                    json string "title",
                    url
            )

            res.status(STATUS_CODE_CREATED)
            saved.json
        }

        deleteJson("/api/memes/:id") { req, _ ->
            getMeme(req longParam "id").let { meme ->
                memeManager.deleteMeme(meme)
                meme.json
            }
        }

        get("*") { req, res ->
            val path = req.pathInfo()

            res.status(STATUS_CODE_NOT_FOUND)

            if (req.json) {
                res.type(MEDIA_TYPE_JSON)
                jsonError("Path does not exist: $path")
            } else {
                res.type(MEDIA_TYPE_HTML)
                reader.read("404", mapOf("path" to path))
            }
        }

        MemeNotFoundException::class jsonException STATUS_CODE_NOT_FOUND
        BadMemeException::class jsonException STATUS_CODE_BAD_REQUEST
        BadPageException::class jsonException STATUS_CODE_BAD_REQUEST
        JSONException::class jsonException STATUS_CODE_BAD_REQUEST

        exception(Exception::class.java) { e, req, res ->
            LOG.error("Unhandled error", e)
            res.status(STATUS_CODE_SERVER_ERROR)

            val message = e.message ?: "Internal server error"
            if (req.json) {
                res.type(MEDIA_TYPE_JSON)
                res.body(jsonError(message))
            } else {
                res.type(MEDIA_TYPE_HTML)
                res.body(reader.read("500", mapOf("message" to message)))
            }
        }
    }

    /**
     * @return meme by id.
     */
    private fun getMeme(id: Long) = memeManager
            .getMeme(id)
            ?: throw MemeNotFoundException("Meme with id: $id, does not exist")

    /**
     * @return meme as json object.
     */
    private val Meme.json: JSONObject
        get() = JSONObject()
                .put("id", id)
                .put("title", title)
                .put("url", url)

    /**
     * @return list of memes as json array.
     */
    private val List<Meme>.json: JSONArray
        get() = JSONArray(map { it.json })
}
