package com.edd.memegrid.util

import org.json.JSONObject
import spark.Request

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
 * @return json error response from provided message.
 */
fun jsonError(message: String) = JSONObject()
        .put("error", message)
        .toString()

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
