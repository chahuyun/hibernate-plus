package cn.chahuyun.hibernateplus.runtime.net

import cn.chahuyun.hibernateplus.api.OrmRuntimeException
import cn.chahuyun.hibernateplus.runtime.manifest.Artifact
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

internal object ArtifactDownloader {

    fun ensureDownloaded(
        artifact: Artifact,
        targetFile: File,
        connectTimeoutMs: Int,
        readTimeoutMs: Int,
    ) {
        if (targetFile.exists() && targetFile.isFile) {
            val sha = sha256Hex(targetFile)
            if (sha.equals(artifact.sha256, ignoreCase = true)) return
            // hash 不匹配，删掉重下
            targetFile.delete()
        }

        targetFile.parentFile?.mkdirs()
        val tmp = File(targetFile.absolutePath + ".tmp")
        if (tmp.exists()) tmp.delete()

        val bytes = HttpBytes.get(artifact.url, connectTimeoutMs, readTimeoutMs)
        Files.write(tmp.toPath(), bytes)

        val sha = sha256Hex(tmp)
        if (!sha.equals(artifact.sha256, ignoreCase = true)) {
            tmp.delete()
            throw OrmRuntimeException("sha256 mismatch for ${artifact.fileName}: expected=${artifact.sha256}, actual=$sha")
        }

        // 原子替换
        if (targetFile.exists()) targetFile.delete()
        if (!tmp.renameTo(targetFile)) {
            // 兜底：copy + delete
            Files.copy(tmp.toPath(), targetFile.toPath())
            tmp.delete()
        }
    }

    private fun sha256Hex(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { b -> ((b.toInt() and 0xFF) + 0x100).toString(16).substring(1) }
    }
}

