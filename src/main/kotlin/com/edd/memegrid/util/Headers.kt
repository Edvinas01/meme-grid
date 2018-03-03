package com.edd.memegrid.util

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
 * Normalized accept header value.
 */
val Request.accept: String
    get() = headers(HEADER_ACCEPT)
            .trim()
            .toLowerCase()

/**
 * Indicates weather this request contains json content.
 */
val Request.json: Boolean
    get() = accept.let { accept ->
        accept.contains(MEDIA_TYPE_JSON) || accept.contains(MEDIA_TYPE_ANY)
    }

/**
 * Indicates weather this request contains html content.
 */
val Request.html: Boolean
    get() = accept.let { accept ->
        accept.contains(MEDIA_TYPE_HTML) || accept.contains(MEDIA_TYPE_ANY)
    }
