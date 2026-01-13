package cn.chahuyun.hibernateplus.runtime

import cn.chahuyun.hibernateplus.api.OrmProvider
import cn.chahuyun.hibernateplus.api.OrmRuntimeException
import cn.chahuyun.hibernateplus.runtime.classloading.ChildFirstURLClassLoader
import cn.chahuyun.hibernateplus.runtime.manifest.DependencyManifest
import cn.chahuyun.hibernateplus.runtime.manifest.RsaManifestVerifier
import cn.chahuyun.hibernateplus.runtime.net.ArtifactDownloader
import cn.chahuyun.hibernateplus.runtime.net.HttpBytes
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL
import java.util.ServiceLoader

/**
 * 运行时：负责
 * - 下载 manifest
 * - 验签（可选）
 * - 下载/缓存 jar
 * - child-first 加载 provider
 */
class OrmRuntime(
    private val cacheDir: File,
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 20_000,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun loadProvider(
        manifestUrl: String,
        expectedProviderId: String,
        rsaPublicKeyBase64: String? = null,
    ): LoadedProvider {
        val manifest = fetchManifest(manifestUrl)

        if (manifest.id != expectedProviderId) {
            throw OrmRuntimeException("manifest.id mismatch: expected=$expectedProviderId actual=${manifest.id}")
        }

        if (rsaPublicKeyBase64 != null) {
            RsaManifestVerifier.verifyOrThrow(manifest, rsaPublicKeyBase64)
        }

        val jars = ensureArtifacts(manifest)
        val urls = jars.map { it.toURI().toURL() }.toTypedArray()
        val parent = Thread.currentThread().contextClassLoader

        val loader = ChildFirstURLClassLoader(urls, parent)
        val provider = discoverProvider(loader, expectedProviderId)
        return LoadedProvider(provider, loader, manifest)
    }

    private fun fetchManifest(url: String): DependencyManifest {
        val bytes = HttpBytes.get(url, connectTimeoutMs, readTimeoutMs)
        val text = bytes.toString(Charsets.UTF_8)
        return try {
            json.decodeFromString(DependencyManifest.serializer(), text)
        } catch (e: Exception) {
            throw OrmRuntimeException("failed to parse manifest json: $url", e)
        }
    }

    private fun ensureArtifacts(manifest: DependencyManifest): List<File> {
        val base = File(cacheDir, "${manifest.id}/${manifest.version}")
        base.mkdirs()

        val files = mutableListOf<File>()
        for (a in manifest.artifacts) {
            val target = File(base, a.fileName)
            ArtifactDownloader.ensureDownloaded(a, target, connectTimeoutMs, readTimeoutMs)
            files += target
        }
        return files
    }

    private fun discoverProvider(loader: ClassLoader, expectedId: String): OrmProvider {
        // ServiceLoader 依赖资源查找，建议临时切换 TCCL 以提升兼容性
        val old = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = loader
            val sl = ServiceLoader.load(OrmProvider::class.java, loader)
            val providers = sl.toList()
            return providers.firstOrNull { it.id == expectedId }
                ?: throw OrmRuntimeException("OrmProvider not found: id=$expectedId, discovered=${providers.map { it.id }}")
        } finally {
            Thread.currentThread().contextClassLoader = old
        }
    }
}

data class LoadedProvider(
    val provider: OrmProvider,
    private val classLoader: ChildFirstURLClassLoader,
    val manifest: DependencyManifest,
) : AutoCloseable {
    override fun close() {
        classLoader.close()
    }
}

