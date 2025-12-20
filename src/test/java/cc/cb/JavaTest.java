package cc.cb;

import cc.cb.entity.MyUser;
import cn.chahuyun.hibernateplus.Configuration;
import cn.chahuyun.hibernateplus.DriveType;
import cn.chahuyun.hibernateplus.HibernateFactory;
import cn.chahuyun.hibernateplus.HibernatePlusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * v2.0.0 Java 测试示例
 * 演示新版本在 Java 环境下的完美兼容性
 *
 * @author Moyuyanli
 */
public class JavaTest {

    private static final Logger log = LoggerFactory.getLogger(JavaTest.class);

    public static void main(String[] args) {
        log.info("开始 v2.0.0 Java 兼容性测试...");

        // 1. 初始化配置
        Configuration configuration = HibernatePlusService.createConfiguration(JavaTest.class);
        configuration.setDriveType(DriveType.HSQLDB);
        configuration.setAddress("db/java_test_v2");
        configuration.setUser("SA");
        configuration.setPassword("");

        // 2. 启动服务
        HibernatePlusService.loadingService(configuration);

        log.info("Java 环境数据库初始化完成...");

        // 3. 插入数据
        MyUser user = new MyUser();
        user.setName("JavaUser");
        user.setSex(1);

        MyUser savedUser = HibernateFactory.merge(user);
        if (savedUser != null) {
            log.info("保存成功，ID: " + savedUser.getId());
        }

        // 4. 普通查询 (Java 调用方式)
        log.info("--- 测试 selectList(Class) ---");
        List<MyUser> list = HibernateFactory.selectList(MyUser.class);
        for (MyUser u : list) {
            log.info("查询到用户: " + u);
        }

        // 5. 条件查询
        log.info("--- 测试 selectOne(Class, field, value) ---");
        MyUser one = HibernateFactory.selectOne(MyUser.class, "name", "JavaUser");
        log.info("条件查询结果: " + one);

        // 6. HQL 查询
        log.info("--- 测试 selectOneByHql(Class, hql, params) ---");
        Map<String, Object> params = new HashMap<>();
        params.put("sex", 1);
        MyUser hqlUser = HibernateFactory.selectOneByHql(MyUser.class, "from MyUser where sex = :sex", params);
        log.info("HQL 查询结果: " + hqlUser);

        // 7. 原生 SQL 查询
        log.info("--- 测试 selectListBySql(Class, sql, params) ---");
        Map<String, Object> sqlParams = new HashMap<>();
        sqlParams.put("name", "JavaUser");
        List<MyUser> sqlUsers = HibernateFactory.selectListBySql(MyUser.class, "SELECT * FROM MyUser WHERE name = :name", sqlParams);
        for (MyUser u : sqlUsers) {
            log.info("SQL 查询结果: " + u);
        }

        log.info("v2.0.0 Java 兼容性测试完成！");
    }
}

