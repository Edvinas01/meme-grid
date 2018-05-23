package com.edd.memegrid.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URL
import java.net.UnknownHostException

class ImageValidator(private val timeout: Int) {

    private companion object {
        const val REQUEST_METHOD = "HEAD"
        const val USER_AGENT =
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0"

        val LOG: Logger = LoggerFactory.getLogger(ImageValidator::class.java)
    }

    /**
     * @return true if provided raw url is valid and contains an image.
     */
    fun isValid(rawUrl: String) = try {
        val url = URL(rawUrl)

        !isLocal(url) && isImage(url)
    } catch (e: MalformedURLException) {
        LOG.debug("Invalid URL: {}", rawUrl, e)
        false
    }

    /**
     * @return true if provided URL points to this server.
     */
    private fun isLocal(url: URL) = try {
        with(InetAddress.getByName(url.host)) {
            isAnyLocalAddress || isLoopbackAddress
        }

    } catch (e: UnknownHostException) {
        LOG.debug("Could not check if URL: {}, is local", url, e)
        false
    }

    /**
     * @return true if provided URL contains an image.
     */
    private fun isImage(url: URL) = try {
        with(url.openConnection() as HttpURLConnection) {
            setRequestProperty(HEADER_USER_AGENT, USER_AGENT)
            setRequestProperty(HEADER_ACCEPT, MEDIA_TYPE_ANY)

            connectTimeout = timeout
            readTimeout = timeout
            requestMethod = REQUEST_METHOD

            contentType
                    ?.contains("image")
                    ?: false
        }
    } catch (e: IOException) {
        LOG.debug("Could not open connection to URL: {}", url, e)
        false
    }
}
