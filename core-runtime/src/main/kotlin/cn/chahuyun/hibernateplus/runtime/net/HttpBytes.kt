package cn.chahuyun.hibernateplus.runtime.net

import cn.chahuyun.hibernateplus.api.OrmRuntimeException
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

internal object HttpBytes {
    fun get(url: String, connectTimeoutMs: Int, readTimeoutMs: Int): ByteArray {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                throw OrmRuntimeException("HTTP $code when GET $url")
            }
            conn.inputStream.use { input ->
                val out = ByteArrayOutputStream()
                val buf = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val n = input.read(buf)
                    if (n <= 0) break
                    out.write(buf, 0, n)
                }
                return out.toByteArray()
            }
        } catch (e: Exception) {
            if (e is OrmRuntimeException) throw e
            throw OrmRuntimeException("failed to GET $url", e)
        } finally {
            conn.disconnect()
        }
    }
}

