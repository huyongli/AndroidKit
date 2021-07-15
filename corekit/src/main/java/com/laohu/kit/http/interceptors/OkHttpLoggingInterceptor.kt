package com.laohu.kit.http.interceptors

import android.util.Log
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.EOFException
import java.io.IOException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class OkHttpLoggingInterceptor : Interceptor {
    companion object {
        private const val TAG = "OkHttpLogging"
        private val UTF8 = Charset.forName("UTF-8")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val entity = LoggingEntity()

        val requestMethod = request.method()
        val requestUrl = request.url().toString()
        val requestHeader = parseRequestHeaderLog(request)
        val requestBody = parseRequestBodyLog(request)

        val startNs = System.nanoTime()
        val response: Response
        var responseBody = ""
        var responseStatus = ""

        try {
            response = chain.proceed(request)

            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

            responseStatus = "${response.code()}, take:${tookMs}ms"
            responseBody = parseResponseBodyLog(response)
        } catch (e: Exception) {
            responseBody = "HTTP FAILED: $e"
            Log.e(TAG, entity.toString())
            throw e
        } finally {
            val entity = LoggingEntity(
                requestMethod = requestMethod,
                requestUrl = requestUrl,
                requestHeader = requestHeader,
                requestBody = requestBody,
                responseStatus = responseStatus,
                responseBody = responseBody
            )
            printLog(entity)
        }
        return response
    }

    private fun parseRequestHeaderLog(request: Request): String {
        val requestBody = request.body()

        val buffer = StringBuffer()
        requestBody?.let {
            it.contentType()?.let { buffer.append("Content-Type: ${requestBody.contentType()}") }
            if (requestBody.contentLength() != -1L) {
                buffer.append("Content-Length: ${requestBody.contentLength()}")
            }
        }

        val headers = request.headers()
        run {
            var i = 0
            while (i < headers.size()) {
                val name = headers.name(i)
                if (buffer.isNotEmpty()) buffer.append("; ")
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                    buffer.append("$name: ${headers.value(i)}")
                }
                i++
            }
        }
        return buffer.toString()
    }

    private fun parseRequestBodyLog(request: Request): String {
        return if (bodyEncoded(request.headers())) {
            ""
        } else {
            request.body()?.let { requestBody ->
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                var charset = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }

                val requestBodyBuffer = StringBuffer()
                if (isPlaintext(buffer)) {
                    if (requestBody is FormBody) {
                        val formBody = requestBody as FormBody?
                        for (i in 0 until formBody!!.run { size() }) {
                            requestBodyBuffer.append(formBody.name(i)).append(":").append(formBody.value(i)).append(", ")
                        }
                    } else {
                        if (requestBody.contentLength() != 0L) {
                            val buffer = Buffer()
                            requestBody.writeTo(buffer)
                            requestBodyBuffer.append(buffer.clone().readString(charset) ?: "")
                        }
                    }
                }
                requestBodyBuffer.toString()
            } ?: ""
        }
    }

    private fun parseResponseBodyLog(response: Response): String {
        return if (bodyEncoded(response.headers())) {
            ""
        } else {
            val responseBody = response.body()
            val source = responseBody?.source()
            source?.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source?.buffer()

            var charset = UTF8
            val contentType = responseBody?.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }

            if (buffer != null && !isPlaintext(buffer)) {
                "非文本信息"
            } else if (responseBody?.contentLength() != 0L) {
                buffer?.clone()?.readString(charset) ?: ""
            } else {
                ""
            }
        }
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = (if (buffer.size() < 64) buffer.size() else 64).toLong()
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    private fun printLog(entity: LoggingEntity) {
        val builder = StringBuilder(" ").append("\n")
            .append("#### requestMethod: ").append(entity.requestMethod).append("\n")
            .append("#### requestUrl: ").append(entity.requestUrl).append("\n")
            .append("#### requestHeader: ").append(entity.requestHeader).append("\n")
            .append("#### requestBody: ").append(entity.requestBody).append("\n")
            .append("#### responseStatus: ").append(entity.responseStatus).append("\n")
            .append("#### responseBody: ").append(entity.responseBody).append("\n")
            .append("##### End Logging #####").append("\n").append("\n")
        Log.i(TAG, builder.toString())
    }
}

data class LoggingEntity(
    val requestMethod: String = "",
    val requestUrl: String = "",
    val requestHeader: String = "",
    val requestBody: String = "",
    val responseStatus: String = "",
    val responseBody: String = ""
)