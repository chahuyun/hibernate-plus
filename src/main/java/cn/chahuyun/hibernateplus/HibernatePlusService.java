package cn.chahuyun.hibernateplus;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.QueryFunction;

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

    /**
     * 创建自定义配置
     *
     * @param clazz 加载类
     * @return {@link Configuration} 自定义配置
     */
    public static Configuration createConfiguration(Class<?> clazz) {
        return new Configuration(clazz);
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
        log.info("Hibernate loaded successfully!");
    }

    /**
     * 从 hibernate.properties 文件加载 hibernate 服务
     *
     * @param clazz 启动类
     */
    public static void loadingService(Class<?> clazz) throws IOException {
        Configuration configuration = new Configuration(clazz);

        Properties properties = new Properties();
        ClassLoader classLoader = configuration.getClassLoader();
        properties.load(classLoader.getResourceAsStream("/example/hibernate.properties"));

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(properties)
                .build();

        MetadataSources sources = new MetadataSources(serviceRegistry);

        configuration.setClassLoader(classLoader);
        extracted(configuration, sources);
        log.info("Hibernate loaded successfully!");
    }

    private static void extracted(Configuration configuration, MetadataSources sources) {
        Thread.currentThread().setContextClassLoader(configuration.getClassLoader());

        Set<Class<?>> classes = scanEntity(configuration);

        for (Class<?> aClass : classes) {
            sources.addAnnotatedClass(aClass);
        }

        if (!configuration.getOtherEntityClasses().isEmpty()) {
            configuration.getOtherEntityClasses().forEach(sources::addAnnotatedClass);
        }

        Metadata metadata = sources.getMetadataBuilder().build();
        HibernateFactory factory = new HibernateFactory(metadata.getSessionFactoryBuilder().build());
        HibernateFactory.setFactory(factory);
    }

    private static Set<Class<?>> scanEntity(Configuration configuration) {
        ClassLoader classLoader = configuration.getClassLoader();
        String packageName = configuration.resolvePackageName();
        if (classLoader == null) {
            throw new RuntimeException("classloader is null !");
        }

        // 创建ConfigurationBuilder并设置自定义ClassLoader
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(packageName, classLoader)
                .addClassLoaders(classLoader));


        QueryFunction<Store, Class<?>> queryFunction = Scanners.TypesAnnotated.of(Entity.class, Embeddable.class, MappedSuperclass.class)
                .asClass(classLoader);

        return queryFunction.apply(reflections.getStore());
    }


}
