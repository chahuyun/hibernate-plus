package cc.cb

import cc.cb.entity.MyUser
import cn.chahuyun.hibernateplus.DriveType
import cn.chahuyun.hibernateplus.HibernateFactory
import cn.chahuyun.hibernateplus.HibernatePlusService
import org.slf4j.LoggerFactory

/**
 * v2.0.0 测试示例
 * 演示新版本特性：HSQLDB、Kotlin 友好 API、HQL/SQL 查询
 *
 * @author Moyuyanli
 */
object Test {

    private val log = LoggerFactory.getLogger(Test::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        log.info("开始 v2.0.0 新特性测试...")

        // 1. 初始化配置 - 使用 2.0.0 新增的 HSQLDB (无需安装，打包即用)
        val configuration = HibernatePlusService.createConfiguration(Test::class.java).apply {
            driveType = DriveType.HSQLDB
            address = "db/test_v2" // 数据库文件路径
            user = "SA"
            password = ""
        }

        // 2. 启动服务
        HibernatePlusService.loadingService(configuration)

        log.info("数据库初始化完成，开始执行操作...")

        // 3. 插入数据
        val user1 = MyUser().apply {
            name = "Moyu"
            sex = 1
        }
        val user2 = MyUser().apply {
            name = "HibernatePlus"
            sex = 2
        }

        HibernateFactory.merge(user1)
        HibernateFactory.merge(user2)

        // 4. 使用 Kotlin 友好的 Reified API 查询列表
        log.info("--- 测试 selectList<T>() ---")
        val allUsers = HibernateFactory.selectList<MyUser>()
        allUsers.forEach { log.info("查询到用户: $it") }

        // 5. 使用 Kotlin 友好的 Reified API 查询单条数据
        log.info("--- 测试 selectOne<T>(field, value) ---")
        val moyu = HibernateFactory.selectOne<MyUser>("name", "Moyu")
        log.info("通过字段查询: $moyu")

        // 6. 测试 HQL 查询 (v2.0.0 新增)
        log.info("--- 测试 selectOneByHql<T>() ---")
        val hqlUser = HibernateFactory.selectOneByHql<MyUser>(
            "from MyUser where sex = :sex",
            mapOf("sex" to 2)
        )
        log.info("HQL 查询结果: $hqlUser")

        // 7. 测试原生 SQL 查询 (v2.0.0 新增)
        log.info("--- 测试 selectListBySql<T>() ---")
        // 注意：SQL 语法需与当前使用的数据库（HSQLDB）匹配
        val sqlUsers = HibernateFactory.selectListBySql<MyUser>(
            "SELECT * FROM MyUser WHERE name LIKE :name",
            mapOf("name" to "%Moyu%")
        )
        sqlUsers.forEach { log.info("SQL 查询结果: $it") }

        // 8. 测试删除
        if (moyu != null) {
            val deleted = HibernateFactory.delete(moyu)
            log.info("删除用户 ${moyu.name}: $deleted")
        }

        log.info("v2.0.0 所有特性测试完成！")
    }
}
