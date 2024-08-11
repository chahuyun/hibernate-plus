package cn.chahuyun.hibernateplus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * hibernate工厂
 *
 * @author Moyuyanli
 * @date 2024/7/18 10:42
 */
@Slf4j
public class HibernateFactory {

    private static HibernateFactory factory;

    private final SessionFactory sessionFactory;


    protected HibernateFactory(SessionFactory session) {
        this.sessionFactory = session;
    }

    protected static void setFactory(HibernateFactory factory) {
        HibernateFactory.factory = factory;
    }

    public static SessionFactory getSession() {
        return factory.sessionFactory;
    }

    /**
     * 查询一个单一对象
     *
     * @param tClass 对象类
     * @param key    主键
     * @param <T>    对象类Class
     * @return 对象 或 null
     */
    public static <T> T selectOne(Class<T> tClass, Object key) {
        return factory.sessionFactory.fromSession(session -> session.find(tClass, key));
    }

    /**
     * 查询一个单一对象
     *
     * @param tClass 对象类
     * @param field  字段
     * @param value  值
     * @param <T>    对象类Class
     * @return 对象 或 null
     */
    @SuppressWarnings("all")
    public static <T> T selectOne(Class<T> tClass, String field, Object value) {
        if (field == null || value == null) {
            return null;
        }
        return factory.sessionFactory.fromSession(session -> session.createQuery(getQuery(tClass, field, value, session)).getSingleResultOrNull());
    }

    /**
     * 查询一个单一对象<br>
     * <br>
     * 参数格式：<br>
     * key -> 对象字段<br>
     * value -> 条件<br>
     * <br><br>
     * 如果查询结果为多个，只拿第一个<br>
     * 如果想获取多个结果，请使用{@link  #selectList}<br>
     * 更多自定义查询请自行使用 {@link  SessionFactory} 建立查询
     *
     * @param tClass 对象类
     * @param params 参数列表
     * @param <T>    对象类Class
     * @return 对象 或 null
     */
    public static <T> T selectOne(Class<T> tClass, Map<String, Object> params) {
        if (params.isEmpty()) {
            return null;
        }
        return factory.sessionFactory.fromSession(session -> {
            CriteriaQuery<T> query = getQuery(tClass, params, session);
            List<T> list = session.createQuery(query).list();
            if (list == null || list.isEmpty()) {
                return null;
            } else {
                return list.get(0);
            }
        });
    }

    /**
     * 查询集合
     * <p>
     *
     * @param tClass 对象类
     * @param params 参数列表
     * @param <T>    对象类Class
     * @return 结果集
     */
    public static <T> List<T> selectList(Class<T> tClass, Map<String, Object> params) {
        if (params.isEmpty()) {
            return null;
        }
        return factory.sessionFactory.fromSession(session -> {
            CriteriaQuery<T> query = getQuery(tClass, params, session);
            return session.createQuery(query).list();
        });
    }


    /**
     * 查询集合
     * <p>
     *
     * @param tClass 对象类
     * @param <T>    对象类Class
     * @return 结果集
     */
    public static <T> List<T> selectList(Class<T> tClass) {
        return factory.sessionFactory.fromSession(session -> {
            CriteriaQuery<T> query = getQuery(tClass, new HashMap<>(), session);
            return session.createQuery(query).list();
        });
    }


    /**
     * 查询集合
     * <p>
     *
     * @param tClass 对象类
     * @param field  条件字段
     * @param value  条件值
     * @param <T>    对象类Class
     * @return 结果集
     */
    public static <T> List<T> selectList(Class<T> tClass, String field, Object value) {
        if (field == null || value == null) {
            return new ArrayList<>(1);
        }
        return factory.sessionFactory.fromSession(session -> session.createQuery(getQuery(tClass, field, value, session)).list());
    }

    /**
     * 保存或更新<br>
     * 如果主键为0或null，则新增<br>
     *
     * @param object 对象
     * @param <T>    对象class
     * @return 新对象
     */
    public static <T> T merge(T object) {
        if (object == null) {
            return null;
        }
        return factory.sessionFactory.fromTransaction(session -> session.merge(object));
    }

    /**
     * 删除一个对象
     *
     * @param object 对象
     * @return true 删除成功
     */
    public static Boolean delete(Object object) {
        if (object == null) {
            return false;
        }
        return factory.sessionFactory.fromTransaction(session -> {
            try {
                session.remove(object);
                return true;
            } catch (Exception e) {
                log.debug(e.getMessage());
                return false;
            }
        });
    }

    @NotNull
    private static <T> CriteriaQuery<T> getQuery(Class<T> tClass, Map<String, Object> params, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(tClass);
        Root<T> from = query.from(tClass);
        query.select(from);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query = query.where(builder.equal(from.get(entry.getKey()), entry.getValue()));
        }
        return query;
    }

    @NotNull
    private static <T> CriteriaQuery<T> getQuery(Class<T> tClass, String filed, Object value, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(tClass);
        Root<T> from = query.from(tClass);
        query.select(from);
        query.where(builder.equal(from.get(filed), value));
        return query;
    }

}
