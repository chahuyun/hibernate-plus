# hibernate-plus
hibernate enhanced integration

hibernate 的一个强化集成模块

### 使用方法

通过使用 [HibernatePlusService](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/HibernatePlusService.java) 来创建自定义配置 [Configuration](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/Configuration.java) 后，在配置中指定参数

```java
Configuration configuration = HibernatePlusService.createConfiguration();
configuration.setDriveType(DriveType.MYSQL);
configuration.setAddress("localhost:3306/test");
configuration.setAutoReconnect(true);
configuration.setUser("root");
configuration.setPassword("123456");
configuration.setClassLoader(Test.class.getClassLoader());
```

对于驱动类型[DriveType](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/DriveType.java)，目前只提供了3种类型

* H2
* MYSQL
* SQLITE

对于实体映射，可以填写`packageName`，进行指定包扫描，模板将会自动将带有`Entity`的实体添加到映射目录中。

当然你也可以不填写，那么我将会根据你的`ClassLoader`自动扫描`"entry", "entity", "entities", "model", "models", "bean", "beans", "dto"`几个包名下面的实体。



给定参数之后就可以通过[HibernatePlusService](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/HibernatePlusService.java)来创建hibernate服务了

```java
HibernatePlusService.loadingService(configuration);
```

成功后即可使用[HibernateFactory](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/HibernateFactory.java)来进行数据操作，我这里封装了几个常用的简单操作

```java
HibernatePlusService.loadingService(configuration);
        myUser user = new myUser();
        user.setName("张");
        user.setSex(2);
        myUser merge = HibernateFactory.merge(user);
        log.info(merge.toString());

        myUser one = HibernateFactory.selectOne(myUser.class, 1);
        log.info(one.toString());
```

更复杂的操作请自行获取`SessionFactory`去创建。

```java
SessionFactory session = HibernateFactory.getSession();
```



