package cn.chahuyun.hibernateplus;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Moyuyanli
 * @date 2024/7/18 14:07
 */
@Getter
@Setter
public class Configuration {

    /**
     * 驱动类型
     */
    private DriveType driveType;

    /**
     * 地址
     */
    private String address;

    /**
     * 用户名
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    /**
     * 包含实体的包名
     */
    private String packageName;

    /**
     * 类加载器
     */
    private ClassLoader classLoader;

    /**
     * 数据库自动重连
     */
    private boolean isAutoReconnect = false;

    /**
     * 转换为 properties
     *
     * @return Properties
     */
    protected Properties toProperties() {
        if (driveType == null) {
            throw new RuntimeException("数据库配置:驱动类型为空!");
        }
        if (address == null) {
            throw new RuntimeException("数据库配置:数据库地址为空!");
        }
        Properties properties = new Properties();
        String url;
        if (isAutoReconnect) {
            url = driveType.getUrlPrefix() + address + "?autoReconnect=true";
        } else {
            url = driveType.getUrlPrefix() + address;
        }
        switch (driveType) {
            case H2:
                properties.setProperty("hibernate.connection.url", url);
                properties.setProperty("hibernate.connection.driver_class", driveType.getDriverClass());
                properties.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
                properties.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
                properties.setProperty("hibernate.connection.isolation", "1");
                properties.setProperty("hibernate.hbm2ddl.auto", "update");
                properties.setProperty("hibernate-connection-autocommit", "true");
                break;
            case SQLITE:
                properties.setProperty("hibernate.connection.url", url);
                properties.setProperty("hibernate.connection.driver_class", driveType.getDriverClass());
                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                properties.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
                properties.setProperty("hibernate.hikari.connectionTimeout", "180000");
                properties.setProperty("hibernate.connection.isolation", "1");
                properties.setProperty("hibernate.hbm2ddl.auto", "update");
                properties.setProperty("hibernate-connection-autocommit", "true");
                if (user != null) {
                    properties.setProperty("hibernate.connection.username", user);
                }
                if (password != null) {
                    properties.setProperty("hibernate.connection.password", password);
                }
                properties.setProperty("hibernate.autoReconnect", "true");
                properties.setProperty("hibernate.connection.username", "");
                properties.setProperty("hibernate.connection.password", "");
                properties.setProperty("hibernate.current_session_context_class", "thread");
                break;
            case MYSQL:
                properties.setProperty("hibernate.connection.url", url);
                properties.setProperty("hibernate.connection.driver_class", driveType.getDriverClass());
//                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                properties.setProperty("hibernate.connection.CharSet", "utf8mb4");
                properties.setProperty("hibernate.connection.useUnicode", "true");
                properties.setProperty("hibernate.connection.username", user);
                properties.setProperty("hibernate.connection.password", password);
                properties.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
                properties.setProperty("hibernate.connection.isolation", "1");
                properties.setProperty("hibernate.hbm2ddl.auto", "update");
                properties.setProperty("hibernate.autoReconnect", "true");
                break;
        }
        return properties;
    }

    /**
     * 根据classLoader寻找带有{@link jakarta.persistence.Entity}的实体类
     *
     * @return 类set
     */
    protected Set<Class<?>> toEntityClass() {
        if (classLoader == null) {
            throw new RuntimeException("数据库配置:类加载器(classLoader)为空!");
        }
        Set<Class<?>> entityClasses;
        if (packageName == null || packageName.trim().isBlank()) {
            entityClasses = findEntityClasses(classLoader);
        } else {
            entityClasses = loadEntitiesFromPackage(classLoader, packageName);
        }
        if (entityClasses.isEmpty()) {
            throw new RuntimeException("class scan is empty !");
        }
        return entityClasses;
    }

    private Set<Class<?>> findEntityClasses(ClassLoader classLoader) {
        Set<Class<?>> allClasses = new HashSet<>();
        try {
            Enumeration<URL> resources = classLoader.getResources("");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                    findAndAddClassesInPackageByFile(directory, packageName, allClasses);
                } else if ("jar".equals(resource.getProtocol())) {
                    String jarPath = URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8);
                    findAndAddClassesInPackageByJar(jarPath.substring(5, jarPath.indexOf("!")), null, allClasses);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading entities from package", e);
        }
        return allClasses;
    }


    private Set<Class<?>> loadEntitiesFromPackage(ClassLoader classLoader, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                    findAndAddClassesInPackageByFile(directory, packageName, classes);
                } else if ("jar".equals(resource.getProtocol())) {
                    String jarPath = URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8);
                    findAndAddClassesInPackageByJar(jarPath.substring(5, jarPath.indexOf("!")), packageName, classes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading entities from package", e);
        }
        return classes;
    }

    private void findAndAddClassesInPackageByJar(String jarPath, String packageName, Set<Class<?>> classes) {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (noPackNameScanEntity(packageName)) continue;
                if (name.endsWith(".class") && name.startsWith(packageName.replace('.', '/') + "/")) {
                    String className = name.substring(0, name.indexOf(".class")).replace('/', '.');
                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        if (clazz.isAnnotationPresent(jakarta.persistence.Entity.class)) {
                            classes.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Error loading class", e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing JAR file", e);
        }
    }

    private boolean noPackNameScanEntity(String packageName) {
        if (packageName != null && this.packageName == null) {
            String[] subPackageNames = {"entry", "entity", "entities", "model", "models", "bean", "beans", "dto"};
            boolean isContinue = true;
            for (String subPackageName : subPackageNames) {
                if (packageName.lastIndexOf(subPackageName) != -1) {
                    isContinue = false;
                    break;
                }
            }
            return isContinue;
        }
        return false;
    }


    private void findAndAddClassesInPackageByFile(File directory, String packageName, Set<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String nPackageName = packageName == null ? file.getName() : packageName + "." + file.getName();
                    findAndAddClassesInPackageByFile(file, nPackageName, classes);
                } else {
                    if (noPackNameScanEntity(packageName)) break;
                    if (file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        try {
                            Class<?> aClass = Class.forName(className);
                            if (aClass.isAnnotationPresent(jakarta.persistence.Entity.class)) {
                                classes.add(aClass);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("Error loading class", e);
                        }
                    }
                }
            }
        }
    }

}
