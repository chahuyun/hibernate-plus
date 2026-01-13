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
import java.util.ServiceLoader

/**
 * OrmRuntime（运行时加载器）
 *
 * 这个类解决的核心问题是：
 * Forge/Modpack 环境里 ClassLoader 往往“不隔离”，不同 Mod 可能携带不同版本的依赖（Hibernate/Hikari/JDBC 等）。
 * 如果把这些依赖直接放在同一个类路径里，就很容易出现：
 * - NoSuchMethodError / ClassNotFoundException（版本不一致）
 * - ServiceLoader 串台（加载到了别的 Mod 的 SPI 实现）
 *
 * 因此这里采用 “按需下载 + child-first ClassLoader” 的方式来隔离 provider 依赖：
 *
 * ### 执行流程（最重要）
 * 1) 下载并解析 manifest（JSON）
 * 2) （可选）对 manifest 做 RSA 验签，确保清单可信
 * 3) 按 manifest 列表下载 jar 到 cacheDir，并对每个 jar 做 sha256 校验
 * 4) 用这些 jar 构建一个 child-first 的 URLClassLoader
 * 5) 用 ServiceLoader 在该 ClassLoader 下发现 [OrmProvider]
 * 6) 返回 [LoadedProvider]（包含 provider 实例 + ClassLoader + manifest）给调用方
 *
 * ### 生命周期（调用方必须遵守）
 * - 你创建出来的 ORM 实例（[cn.chahuyun.hibernateplus.api.OrmService]）**必须 close**
 *   - 否则连接池线程、SQLite 文件锁等可能残留。
 * - 你拿到的 [LoadedProvider] **也必须 close**
 *   - 否则 child-first ClassLoader 不会释放（类/资源句柄可能泄漏）。
 *
 * ### 线程上下文类加载器（TCCL）
 * - ServiceLoader/资源查找很多时候依赖 TCCL
 * - 所以这里在 provider 发现阶段会临时切换 TCCL，最终恢复，避免污染外部线程。
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

    /**
     * 加载一个 provider（通常是 "hibernate6"）。
     *
     * @param manifestUrl 你服务器上的 manifest JSON 地址
     * @param expectedProviderId 期望的 provider id（防止 URL 指向了别的清单）
     * @param rsaPublicKeyBase64 可选：用于验签的 RSA 公钥（X.509 编码后 Base64）
     */
    fun loadProvider(
        manifestUrl: String,
        expectedProviderId: String,
        rsaPublicKeyBase64: String? = null,
    ): LoadedProvider {
        // 1) 下载 & 解析 manifest（包含 jar 列表、hash、签名等）
        val manifest = fetchManifest(manifestUrl)

        // 2) 最小安全校验：防止你误配 URL 或被劫持到了“别的 provider 清单”
        if (manifest.id != expectedProviderId) {
            throw OrmRuntimeException("manifest.id mismatch: expected=$expectedProviderId actual=${manifest.id}")
        }

        // 3) 可选：验签（用于保证 manifest 没被篡改）
        // 只要 manifest 可信，里面每个 jar 的 sha256 也就可信；后续再用 sha256 校验文件内容即可。
        if (rsaPublicKeyBase64 != null) {
            RsaManifestVerifier.verifyOrThrow(manifest, rsaPublicKeyBase64)
        }

        // 4) 下载/缓存 jars，并逐个做 sha256 校验（不匹配直接报错）
        val jars = ensureArtifacts(manifest)
        val urls = jars.map { it.toURI().toURL() }.toTypedArray()

        // 5) 构建 child-first ClassLoader
        // parent 选择当前线程的 TCCL：通常在 Mod 环境下，它指向“业务侧/Mod 自身的 ClassLoader”。
        // 这样 provider 里如果需要访问你的实体类（Entity），可以从 parent 找到。
        val parent = Thread.currentThread().contextClassLoader
        val loader = ChildFirstURLClassLoader(urls, parent)

        // 6) 在这个 loader 下用 ServiceLoader 发现 provider 实现类
        val provider = discoverProvider(loader, expectedProviderId)

        // 7) 返回 LoadedProvider 给调用方，后续由调用方负责 close()
        return LoadedProvider(provider, loader, manifest)
    }

    private fun fetchManifest(url: String): DependencyManifest {
        // HTTP GET 获取 manifest 内容（json）
        val bytes = HttpBytes.get(url, connectTimeoutMs, readTimeoutMs)
        val text = bytes.toString(Charsets.UTF_8)
        return try {
            json.decodeFromString(DependencyManifest.serializer(), text)
        } catch (e: Exception) {
            throw OrmRuntimeException("failed to parse manifest json: $url", e)
        }
    }

    private fun ensureArtifacts(manifest: DependencyManifest): List<File> {
        // 缓存路径：{cacheDir}/{id}/{version}/...
        val base = File(cacheDir, "${manifest.id}/${manifest.version}")
        base.mkdirs()

        val files = mutableListOf<File>()
        for (a in manifest.artifacts) {
            val target = File(base, a.fileName)
            // 如果已存在且 hash 匹配则跳过；否则重下并校验
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
            // 一定恢复，避免污染外部线程/Mod 环境
            Thread.currentThread().contextClassLoader = old
        }
    }
}

/**
 * 一个已加载的 provider 容器。
 *
 * 注意：这个类本身只负责“关闭 ClassLoader”；
 * 业务侧创建出来的 OrmService 仍需要你自己 close。
 */
data class LoadedProvider(
    val provider: OrmProvider,
    private val classLoader: ChildFirstURLClassLoader,
    val manifest: DependencyManifest,
) : AutoCloseable {
    override fun close() {
        classLoader.close()
    }
}

