# hibernate-plus
hibernate enhanced integration

hibernate 的一个强化集成模块

用于以最小的程度连接你的数据库，为你带来最舒服的数据持久化。

本项目大部分借鉴于[mirai-hibernate-plugin](https://github.com/cssxsh/mirai-hibernate-plugin)

### 使用方法

在你的项目中引用
maven:
```maven
<dependency>
  <groupId>cn.chahuyun</groupId>
  <artifactId>hibernate-plus</artifactId>
  <version>1.0.7</version>
  <type>module</type>
</dependency>
```
gradle:
```kts
implementation("cn.chahuyun:hibernate-plus:1.0.7")
```


通过使用 [HibernatePlusService](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/HibernatePlusService.java) 来创建自定义配置 [Configuration](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/Configuration.java) 后，在配置中指定参数

```java
//这里是你的启动类
Configuration configuration = HibernatePlusService.createConfiguration(Test.class);

configuration.setDriveType(DriveType.MYSQL);
configuration.setAddress("localhost:3306/test");
configuration.setAutoReconnect(true);
configuration.setUser("root");
configuration.setPassword("123456");

//configuration.setPackageName("cc.cb.entity");
```

对于驱动类型[DriveType](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/DriveType.java)，目前只提供了3种类型

* H2
* MYSQL
* SQLITE

对于实体映射，可以填写`packageName`，进行指定包扫描，模板将会自动将带有`Entity`的实体添加到映射目录中。

当然你也可以不填写，那么我将会根据你的启动类自动扫描`"entry", "entity", "entities", "model", "models", "bean", "beans", "dto"`几个包名下面的实体。


给定参数之后就可以通过[HibernatePlusService](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/HibernatePlusService.java)来创建hibernate服务了

```java
HibernatePlusService.loadingService(configuration);
```

成功后即可使用[HibernateFactory](https://github.com/chahuyun/hibernate-plus/blob/dev/src/main/java/cn/chahuyun/hibernateplus/HibernateFactory.java)来进行数据操作，我这里封装了几个常用的简单操作

```java
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
```

更复杂的操作请自行获取`SessionFactory`去创建。

```java
SessionFactory session = HibernateFactory.getSession();
```

### 自定义使用

你也可以不使用我给你的推荐配置，只需要在resources目录下填写`hibernate.properties`就行，然后通过
```java
HibernatePlusService.loadingService(Test.class);
```
就可以使用自定义配置进行连接



