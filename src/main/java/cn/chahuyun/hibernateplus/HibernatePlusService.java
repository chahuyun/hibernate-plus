package cn.chahuyun.hibernateplus;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author Moyuyanli
 * @date 2024/7/10 15:54
 */
@Slf4j
public class HibernatePlusService {


    private HibernatePlusService() {

    }

    public static Configuration createConfiguration() {
        return new Configuration();
    }

    /**
     * 从自定义配置文件加载 hibernate 服务
     *
     * @param configuration 自定义配置文件
     */
    public static void loadingService(Configuration configuration) {
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.toProperties())
                .build();

        MetadataSources sources = new MetadataSources(serviceRegistry);
        extracted(configuration, sources);
        log.info("Hibernate 加载成功!");
    }

    /**
     * 从 hibernate.properties 文件加载 hibernate 服务
     *
     * @param classLoader 类加载器
     */
    public static void loadingService(ClassLoader classLoader) throws IOException {
        Properties properties = new Properties();
        properties.load(classLoader.getResourceAsStream("/hibernate.properties"));

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(properties)
                .build();

        MetadataSources sources = new MetadataSources(serviceRegistry);
        Configuration configuration = new Configuration();
        configuration.setClassLoader(classLoader);
        extracted(configuration, sources);
        log.info("Hibernate 加载成功!");
    }

    private static void extracted(Configuration configuration, MetadataSources sources) {
        Set<Class<?>> entityClass = configuration.toEntityClass();
        for (Class<?> aClass : entityClass) {
            sources.addAnnotatedClass(aClass);
        }
        Metadata metadata = sources.getMetadataBuilder().build();
        HibernateFactory factory = new HibernateFactory(metadata.getSessionFactoryBuilder().build());
        HibernateFactory.setFactory(factory);
    }


}
