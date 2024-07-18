package cc;

import cc.entity.myUser;
import cn.chahuyun.hibernateplus.Configuration;
import cn.chahuyun.hibernateplus.DriveType;
import cn.chahuyun.hibernateplus.HibernateFactory;
import cn.chahuyun.hibernateplus.HibernatePlusService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author Moyuyanli
 * @date 2024/7/10 15:59
 */

@Slf4j
public class Test {

    public static void main(String[] args) throws IOException {
        Configuration configuration = HibernatePlusService.createConfiguration();
        configuration.setDriveType(DriveType.MYSQL);
        configuration.setAddress("localhost:3306/huyan");
        configuration.setAutoReconnect(true);
        configuration.setUser("root");
        configuration.setPassword("Zz123456");
        configuration.setClassLoader(Test.class.getClassLoader());
//        configuration.setPackageName("cn.chahuyun.cc.entity");
        HibernatePlusService.loadingService(configuration);
        myUser user = new myUser();
        user.setName("张");
        user.setSex(2);
        myUser merge = HibernateFactory.merge(user);
        log.info(merge.toString());

        myUser one = HibernateFactory.selectOne(myUser.class, 1);
        log.info(one.toString());
    }


}
