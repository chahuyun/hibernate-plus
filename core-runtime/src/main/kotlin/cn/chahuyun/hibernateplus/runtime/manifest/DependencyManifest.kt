package cn.chahuyun.hibernateplus.runtime.manifest

import kotlinx.serialization.Serializable

@Serializable
data class DependencyManifest(
    /**
     * manifest 标识（通常与 providerId 一致，例如 "hibernate6"）
     */
    val id: String,

    /**
     * manifest 版本（由你控制，建议与 impl 版本一致）
     */
    val version: String,

    /**
     * 依赖工件列表（jar）
     */
    val artifacts: List<Artifact>,

    /**
     * 对 [fingerprint] 的签名（Base64，算法由 runtime 决定，默认 SHA256withRSA）。
     */
    val signatureBase64: String? = null,

    /**
     * 用于签名/验签的稳定指纹（建议由发布端生成并固化，避免 JSON 规范化问题）。
     *
     * 如果为空，runtime 会根据 [artifacts] 计算一个指纹再验签（不推荐，最好发布端生成）。
     */
    val fingerprint: String? = null,
)

@Serializable
data class Artifact(
    val fileName: String,
    val url: String,
    /**
     * jar 的 sha256（小写 hex）
     */
    val sha256: String,
    /**
     * 可选：用于进度/快速校验
     */
    val size: Long? = null,
)

