package cn.chahuyun.hibernateplus.runtime.manifest

import java.security.MessageDigest

object ManifestFingerprint {

    /**
     * 生成稳定指纹，避免“JSON 字段顺序/空白”导致签名不稳定。
     *
     * 规则：
     * - artifacts 按 fileName 排序
     * - 拼接：id|version|fileName:sha256:url|...
     * - 最终对拼接字符串做 sha256，输出 hex
     */
    fun compute(manifest: DependencyManifest): String {
        val artifacts = manifest.artifacts.sortedBy { it.fileName }
        val raw = buildString {
            append(manifest.id)
            append('|')
            append(manifest.version)
            for (a in artifacts) {
                append('|')
                append(a.fileName)
                append(':')
                append(a.sha256.lowercase())
                append(':')
                append(a.url)
            }
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.toHex()
    }
}

private fun ByteArray.toHex(): String =
    joinToString(separator = "") { b -> ((b.toInt() and 0xFF) + 0x100).toString(16).substring(1) }

