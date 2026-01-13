package cn.chahuyun.hibernateplus.runtime.manifest

import cn.chahuyun.hibernateplus.api.OrmRuntimeException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * RSA 验签（SHA256withRSA）。
 *
 * 建议发布端对 manifest 的 [DependencyManifest.fingerprint] 做签名；
 * runtime 侧用 manifest 中携带的 fingerprint + signatureBase64 做验签。
 */
object RsaManifestVerifier {

    fun verifyOrThrow(manifest: DependencyManifest, publicKeyBase64: String) {
        val signatureBase64 = manifest.signatureBase64
            ?: throw OrmRuntimeException("manifest.signatureBase64 is null")

        val fingerprint = manifest.fingerprint ?: ManifestFingerprint.compute(manifest)

        val publicKey = decodePublicKey(publicKeyBase64)
        val sigBytes = Base64.getDecoder().decode(signatureBase64)

        val ok = Signature.getInstance("SHA256withRSA").run {
            initVerify(publicKey)
            update(fingerprint.toByteArray(Charsets.UTF_8))
            verify(sigBytes)
        }

        if (!ok) {
            throw OrmRuntimeException("manifest signature verify failed (id=${manifest.id}, version=${manifest.version})")
        }
    }

    private fun decodePublicKey(publicKeyBase64: String): PublicKey {
        return try {
            val bytes = Base64.getDecoder().decode(publicKeyBase64)
            val spec = X509EncodedKeySpec(bytes)
            KeyFactory.getInstance("RSA").generatePublic(spec)
        } catch (e: Exception) {
            throw OrmRuntimeException("invalid RSA public key (base64)", e)
        }
    }
}

