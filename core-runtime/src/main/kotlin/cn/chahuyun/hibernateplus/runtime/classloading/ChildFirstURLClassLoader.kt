package cn.chahuyun.hibernateplus.runtime.classloading

import java.net.URL
import java.net.URLClassLoader

/**
 * child-first URLClassLoader（用于隔离 provider 的第三方依赖版本）。
 *
 * 规则：
 * - 对 core-api 自身包名、JDK、Kotlin、SLF4J 走 parent-first（避免重复加载导致类型不一致）
 * - 其他包名默认 child-first
 */
class ChildFirstURLClassLoader(
    urls: Array<URL>,
    parent: ClassLoader,
) : URLClassLoader(urls, parent) {

    private val parentFirstPrefixes = arrayOf(
        "java.",
        "javax.",
        "kotlin.",
        "kotlinx.",
        "org.slf4j.",
        "cn.chahuyun.hibernateplus.api.",
        "cn.chahuyun.hibernateplus.runtime.",
    )

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        // parent-first 白名单
        if (parentFirstPrefixes.any { name.startsWith(it) }) {
            return super.loadClass(name, resolve)
        }

        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }
            try {
                val c = findClass(name)
                if (resolve) resolveClass(c)
                return c
            } catch (_: ClassNotFoundException) {
                return super.loadClass(name, resolve)
            }
        }
    }
}

