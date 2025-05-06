package cn.chahuyun.hibernateplus;

import lombok.Getter;

/**
 * @author Moyuyanli
 * @since 2024/7/18 14:12
 */
@Getter
public enum DriveType {
    /**
     * H2类型数据库
     */
    H2("jdbc:h2:file:", "org.h2.Driver"),
    /**
     * sqlite类型数据库
     */
    SQLITE("jdbc:sqlite:file:", "org.sqlite.JDBC"),
    /**
     * mysql类型数据库
     */
    MYSQL("jdbc:mysql://", "com.mysql.cj.jdbc.Driver");


    private final String urlPrefix;
    private final String driverClass;

    DriveType(String urlPrefix, String driverClass) {
        this.urlPrefix = urlPrefix;
        this.driverClass = driverClass;
    }

}
