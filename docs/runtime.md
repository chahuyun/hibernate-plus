# runtime（按需下载 + 验签 + ClassLoader 隔离）

本仓库从 `2.2.0` 起拆分为三层：

- `orm-core-api`：稳定的最小 API（`OrmProvider` / `OrmService` / `OrmConfig`）
- `orm-core-runtime`：下载 manifest、校验、验签、child-first 加载 provider
- `hibernate-plus`：Hibernate6 具体实现（包含 Hibernate/Hikari/JDBC/Reflections 等）

同时提供一个便捷聚合依赖：

- `orm-core-mod`：等价于同时引入 `orm-core-api` + `orm-core-runtime`

## 1. Manifest 格式

`core-runtime` 默认使用 JSON manifest（通过 `kotlinx-serialization` 解析）：

```json
{
  "id": "hibernate6",
  "version": "6.5.2.Final-impl.1",
  "artifacts": [
    {
      "fileName": "hibernate-plus-2.2.0.jar",
      "url": "https://your.domain/artifacts/hibernate-plus-2.2.0.jar",
      "sha256": "...."
    }
  ],
  "fingerprint": "....",
  "signatureBase64": "...."
}
```

## 2. 指纹（fingerprint）如何生成

为避免 “JSON 字段顺序/空白” 导致签名不稳定，runtime 使用稳定指纹规则：

- `artifacts` 按 `fileName` 排序
- 拼接：`id|version|fileName:sha256:url|...`
- 对拼接字符串做 `SHA-256`，输出小写 hex

对应实现：`cn.chahuyun.hibernateplus.runtime.manifest.ManifestFingerprint.compute(...)`

## 3. 签名/验签

runtime 默认用 `SHA256withRSA` 验签，公钥为 **X.509 编码**后再 Base64。

验签入口：`cn.chahuyun.hibernateplus.runtime.manifest.RsaManifestVerifier.verifyOrThrow(...)`

建议发布端流程：

- 构建 jar 列表（固定版本）
- 计算每个 jar 的 sha256
- 生成 fingerprint
- 对 fingerprint 做 RSA 私钥签名（SHA256withRSA）
- 把 fingerprint + signatureBase64 写回 manifest

## 4. 缓存目录与下载

`OrmRuntime(cacheDir=...)` 会把 jar 缓存到：

`{cacheDir}/{manifest.id}/{manifest.version}/{artifact.fileName}`

下载完成后会进行 sha256 校验，不匹配会直接报错并删除临时文件。

## 5. ClassLoader 隔离策略

runtime 使用 `ChildFirstURLClassLoader` 来加载 provider 及其依赖：

- 对 `java.*` / `kotlin.*` / `org.slf4j.*` / `cn.chahuyun.hibernateplus.(api|runtime).*` 走 parent-first
- 其他包默认 child-first

目的是降低 Forge/Modpack 场景下“依赖版本互相覆盖”的概率。

## 6. 生命周期（必须 close）

你需要在服务器停服/卸载阶段调用：

- `OrmService.close()`：释放 SessionFactory、连接池线程、SQLite 文件锁等
- `LoadedProvider.close()`：关闭 child-first ClassLoader（防止类泄漏）

