package com.edd.memegrid.web

import com.edd.memegrid.memes.Meme
import com.edd.memegrid.memes.MemeFetcher
import com.edd.memegrid.memes.MemeSaver
import com.edd.memegrid.util.BadMemeException
import com.edd.memegrid.util.MEDIA_TYPE_HTML
import com.edd.memegrid.util.MEDIA_TYPE_JSON
import com.edd.memegrid.util.STATUS_CODE_BAD_REQUEST
import com.edd.memegrid.util.STATUS_CODE_CREATED
import com.edd.memegrid.util.STATUS_CODE_NOT_FOUND
import com.edd.memegrid.util.STATUS_CODE_SERVER_ERROR
import com.edd.memegrid.util.STATUS_CODE_UNSUPPORTED_MEDIA_TYPE
import com.edd.memegrid.util.TemplateReader
import com.edd.memegrid.util.accept
import com.edd.memegrid.util.html
import com.edd.memegrid.util.json
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import org.postgresql.util.PSQLException
import spark.Request
import spark.Response
import spark.Route
import spark.Spark.exception
import spark.Spark.get
import spark.Spark.post

class Router(
        private val memeFetcher: MemeFetcher,
        private val memeSaver: MemeSaver,
        private val reader: TemplateReader
) {

    fun start() {
        getHtml("/") { _, _ ->
            reader.read("index")
        }

        getJson("/api/memes") { _, _ ->
            JSONArray(memeFetcher.getMemes().map { meme ->
                meme.json
            }).toString()
        }

        postJson("/api/memes") { req, res ->
            val json = req.jsonBody

            val saved = memeSaver.saveMeme(Meme(
                    json.asString("title"),
                    json.asString("url")
            )).json

            res.status(STATUS_CODE_CREATED)
            saved
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

        jsonExceptionHandler(BadMemeException::class.java, STATUS_CODE_BAD_REQUEST)
        jsonExceptionHandler(JSONException::class.java, STATUS_CODE_BAD_REQUEST)
        jsonExceptionHandler(PSQLException::class.java, STATUS_CODE_BAD_REQUEST)

        exception(Exception::class.java) { e, req, res ->
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
     * @return property from json object as string.
     */
    private fun JSONObject.asString(name: String): String {
        if (!has(name)) {
            throw BadMemeException("$name must be present")
        }
        return get(name)
                as? String
                ?: throw BadMemeException("$name mut be a string")
    }

    /**
     * @return parsed json object from request.
     */
    private val Request.jsonBody
        get() = JSONObject(JSONTokener(body()))

    /**
     * @return meme converted to json string.
     */
    private val Meme.json
        get() = JSONObject()
                .put("title", title)
                .put("url", url)

    /**
     * @return a Json error response from provided message.
     */
    private fun jsonError(message: String) = JSONObject()
            .put("error", message)
            .toString()

    /**
     * Create a GET html route.
     */
    private fun getHtml(path: String, body: (Request, Response) -> Any) {
        get(path) { req, res ->
            res.type(MEDIA_TYPE_HTML)

            if (req.html) {
                body(req, res)
            } else {
                res.status(STATUS_CODE_UNSUPPORTED_MEDIA_TYPE)
                reader.read("415", mapOf("type" to req.accept))
            }
        }
    }

    /**
     * Register an error handler which will return a Json response to the client.
     */
    private fun <T : Exception> jsonExceptionHandler(
            type: Class<T>,
            status: Int,
            defaultMessage: String = "") {

        exception(type) { e, _, res ->
            res.type(MEDIA_TYPE_JSON)
            res.status(status)
            res.body(jsonError(e.message ?: defaultMessage))
        }
    }

    /**
     * Create a GET json route.
     */
    private fun getJson(path: String, body: (Request, Response) -> Any) {
        get(path, jsonRoute(body))
    }

    /**
     * Create a POST json route.
     */
    private fun postJson(path: String, body: (Request, Response) -> Any) {
        post(path, jsonRoute(body))
    }

    /**
     * Create a generic json route with media type handling.
     */
    private fun jsonRoute(body: (Request, Response) -> Any) = Route { req, res ->
        res.type(MEDIA_TYPE_JSON)

        if (req.json) {
            body(req, res)
        } else {
            res.status(STATUS_CODE_UNSUPPORTED_MEDIA_TYPE)
            "{\"error\": \"Unsupported media type: ${req.accept}\"}"
        }
    }
}
