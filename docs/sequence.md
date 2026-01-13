# OrmRuntime 执行时序图（从 Mod 启动到关闭）

下面这个时序图描述的是 **“隔离/按需下载模式”** 的完整执行链路：

- 业务侧（你的 Forge Mod）只依赖 `orm-core-*`
- provider（例如 Hibernate6）由 runtime 通过 manifest 下载并用 child-first ClassLoader 加载
- 业务侧创建 `OrmService` 后负责业务调用，停服时必须 close

```mermaid
sequenceDiagram
    autonumber
    participant App as 业务侧(Mod/项目)
    participant RT as OrmRuntime(core-runtime)
    participant HTTP as HTTP(你的依赖服务器)
    participant Cache as 本地缓存目录(cacheDir)
    participant CL as ChildFirstURLClassLoader
    participant SL as ServiceLoader(SPI)
    participant P as OrmProvider(impl-hibernate6)
    participant ORM as OrmService(会话/连接池)

    App->>RT: new OrmRuntime(cacheDir)
    App->>RT: loadProvider(manifestUrl, expectedId, publicKey?)

    RT->>HTTP: GET manifestUrl
    HTTP-->>RT: manifest.json
    RT->>RT: 解析 JSON -> DependencyManifest

    alt 配置了 publicKey
        RT->>RT: 计算/读取 fingerprint
        RT->>RT: RSA验签(signatureBase64, fingerprint)
    else 未配置 publicKey
        RT->>RT: 跳过验签（仍会做 jar 的 sha256 校验）
    end

    loop 对每个 artifact
        RT->>Cache: 检查 {id}/{version}/{fileName} 是否存在
        alt 已存在且 sha256 匹配
            RT->>RT: 复用缓存文件
        else 需要下载/重下
            RT->>HTTP: GET artifact.url
            HTTP-->>RT: artifact.jar bytes
            RT->>Cache: 写入 .tmp
            RT->>RT: 计算 sha256 校验
            RT->>Cache: rename .tmp -> 正式 jar
        end
    end

    RT->>CL: new ChildFirstURLClassLoader(jarUrls, parent=AppClassLoader/TCCL)
    RT->>SL: ServiceLoader.load(OrmProvider, CL)\n(临时切换TCCL=CL)
    SL-->>RT: 发现 Provider 列表
    RT-->>App: LoadedProvider(provider, classLoader, manifest)

    App->>P: provider.create(OrmConfig)\n(appClassLoader, entityPackage, settings...)
    P-->>App: OrmService(内部持有 SessionFactory/Hikari 等)

    App->>ORM: select/merge/delete/...（业务调用）

    App->>ORM: close()\n释放 SessionFactory/连接池/线程/文件锁
    App->>CL: LoadedProvider.close()\n关闭 child-first ClassLoader
```

## 关闭顺序（非常重要）

1. 先 `OrmService.close()`：释放 Hibernate SessionFactory、连接池线程、SQLite 文件锁等。
2. 再 `LoadedProvider.close()`：释放 child-first ClassLoader（防止类/资源句柄泄漏）。

