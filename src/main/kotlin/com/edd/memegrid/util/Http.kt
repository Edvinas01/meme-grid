package com.edd.memegrid.util

import org.json.JSONObject
import spark.Request
import spark.Response
import spark.Route
import spark.Spark.delete
import spark.Spark.exception
import spark.Spark.get
import spark.Spark.post
import kotlin.reflect.KClass

const val HEADER_USER_AGENT = "User-Agent"
const val HEADER_ACCEPT = "Accept"

const val MEDIA_TYPE_JSON = "application/json"
const val MEDIA_TYPE_HTML = "text/html"
const val MEDIA_TYPE_ANY = "*/*"

const val STATUS_CODE_UNSUPPORTED_MEDIA_TYPE = 415
const val STATUS_CODE_SERVER_ERROR = 500
const val STATUS_CODE_BAD_REQUEST = 400
const val STATUS_CODE_NOT_FOUND = 404
const val STATUS_CODE_CREATED = 201

/**
 * @return accept header value.
 */
val Request.accept: String
    get() = headers(HEADER_ACCEPT)
            .trim()
            .toLowerCase()

/**
 * @return true if this request contains json content or false otherwise.
 */
val Request.json: Boolean
    get() = accept.let { accept ->
        accept.contains(MEDIA_TYPE_JSON) || accept.contains(MEDIA_TYPE_ANY)
    }

/**
 * @return true if this request contains html content or false otherwise.
 */
val Request.html: Boolean
    get() = accept.let { accept ->
        accept.contains(MEDIA_TYPE_HTML) || accept.contains(MEDIA_TYPE_ANY)
    }

/**
 * @return parsed json object from request.
 */
val Request.jsonBody
    get() = JSONObject(body())

/**
 * @return parameter as an Int number.
 */
infix fun Request.intParam(name: String): Int = params(name)
        .trim()
        .toIntOrNull()
        ?: throw BadPageException("Parameter $name must be a number")

/**
 * @return parameter as a Long number.
 */
infix fun Request.longParam(name: String): Long = params(name)
        .trim()
        .toLongOrNull()
        ?: throw BadMemeException("Parameter $name, must be a number")

/**
 * @return property from json object as string.
 * @throws BadMemeException if property is missing or blank.
 */
infix fun JSONObject.string(name: String): String {
    if (isNull(name)) {
        throw BadMemeException("$name must be present")
    }

    val value = get(name)
            as? String
            ?: throw BadMemeException("$name must be a string")

    if (value.isBlank()) {
        throw BadMemeException("$name must not be blank")
    }
    return value
}

/**
 * @return json error response from provided message.
 */
fun jsonError(message: String) = JSONObject()
        .put("error", message)
        .toString()

/**
 * Create a GET json route.
 */
fun getJson(path: String, body: (Request, Response) -> Any) {
    get(path, jsonRoute(body))
}

/**
 * Create a POST json route.
 */
fun postJson(path: String, body: (Request, Response) -> Any) {
    post(path, jsonRoute(body))
}

/**
 * Create a DELETE json route.
 */
fun deleteJson(path: String, body: (Request, Response) -> Any) {
    delete(path, jsonRoute(body))
}

/**
 * Create a generic json route with media type handling.
 */
fun jsonRoute(body: (Request, Response) -> Any) = Route { req, res ->
    res.type(MEDIA_TYPE_JSON)

    if (req.json) {
        body(req, res)
    } else {
        res.status(STATUS_CODE_UNSUPPORTED_MEDIA_TYPE)
        jsonError("Unsupported media type: ${req.accept}")
    }
}

/**
 * Register an error handler which will return a Json response to the client.
 */
inline infix fun <reified T : Exception> KClass<T>.jsonException(status: Int) {
    exception(T::class.java) { e, _, res ->
        res.type(MEDIA_TYPE_JSON)
        res.status(status)
        res.body(jsonError(e.message ?: "Error"))
    }
}
