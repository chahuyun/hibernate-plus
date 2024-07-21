package cc.cb;

import cc.cb.entity.MyUser;
import cn.chahuyun.hibernateplus.Configuration;
import cn.chahuyun.hibernateplus.DriveType;
import cn.chahuyun.hibernateplus.HibernateFactory;
import cn.chahuyun.hibernateplus.HibernatePlusService;

import java.util.List;

/**
 * @author Moyuyanli
 * @Date 2024/7/20 18:03
 */

public class Test {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {

        Configuration configuration = HibernatePlusService.createConfiguration(Test.class);

        configuration.setDriveType(DriveType.MYSQL);
        configuration.setAddress("localhost:3306/test");
        configuration.setAutoReconnect(true);
        configuration.setUser("root");
        configuration.setPassword("123456");

        configuration.setPackageName("cc.cb.entity");

        HibernatePlusService.loadingService(configuration);

        List<MyUser> myUsers = HibernateFactory.selectList(MyUser.class);

        log.info("==========list=============");

        for (MyUser myUser : myUsers) {
            log.info(myUser.toString());
        }

        log.info("===========================");

        MyUser myUser = new MyUser();
        myUser.setName("张");
        myUser.setSex(123);

        Integer id = HibernateFactory.merge(myUser).getId();

        log.info("==========one=============");

        MyUser selectOne = HibernateFactory.selectOne(MyUser.class, id);

        log.info(selectOne.toString());

        log.info("===========================");
    }

}