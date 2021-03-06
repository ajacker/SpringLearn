---
typora-copy-images-to: spring笔记.assets
---

[TOC]



# 第一部分 IOC控制反转和DI依赖注入

## A、IOC控制反转

### 一、程序的耦合和解耦

- **耦合**: 程序间的依赖关系.在开发中,应该做到解决**编译期依赖**,即**编译期不依赖,运行时才依赖**.
- **解耦的思路**: 使用**反射来创建对**象,而**避免使用new关键字**,并**通过读取配置文件来获取要创建的对象全限定类名.**

#### 解耦例子：JDBC驱动

在注册驱动的时候不使用`DriverManager`的`register`方法，而采用`Class.forName("驱动类全类名")`的方式

```java
public static void main(String[] args) {
        //注册驱动的方式1：创建驱动类的实例
        //DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        //注册驱动的方式2：通过反射
        try {
            // 实际开发中此类名从properties文件中读取
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // 产生的是运行时错误，不在编译期出错
            System.err.println("无法找到jdbc驱动！");
        }
    }
```

- 在第一种方式中，如果没有导入相应的依赖，就会无法通过编译，这就是我们说的**编译期依赖**

![1565775189251](spring笔记.assets\1565775189251.png)

- 在第二种方式中，没有导入相应的包**可以通过编译**，只是会在**运行期间**产生异常

![1565775270922](spring笔记.assets\1565775270922.png)

这就完成了**从编译期依赖转到运行期依赖**的过程，也就是实现了一定程度的**解耦合**

#### 解耦例子：工厂模式 三层架构

- 首先我们模拟三层架构的实现

  - 业务层

    ```java
    /**
     * @author ajacker
     * 账户业务层接口
     */
    public interface IAccountService {
        /**
         * 模拟保存账户
         */
        void saveAccount();
    }
    ```

    

    ```java
    /**
     * @author ajacker
     * 账户的业务层实现类
     */
    public class AccountServiceImpl implements IAccountService {
        private IAccountDao accountDao = new AccountDaoImpl();
        @Override
        public void saveAccount() {
            accountDao.saveAccount();
        }
    }
    ```

  - 持久层

    ```java
    /**
     * @author ajacker
     * 账户的持久层接口
     */
    public interface IAccountDao {
        /**
         * 模拟保存账户
         */
        void saveAccount();
    }
    ```

    ```java
    /**
     * @author ajacker
     * 账户的持久层实现类
     */
    public class AccountDaoImpl implements IAccountDao {
        @Override
        public void saveAccount() {
            System.out.println("保存了账户");
        }
    }
    ```

- 表现层：

  ```java
  /**
   * @author ajacker
   * 模拟一个表现层
   */
  public class Client {
      public static void main(String[] args) {
          IAccountService as = new AccountServiceImpl();
          as.saveAccount();
      }
  }
  ```

  

  还记得上个例子中说的**解耦合**问题吗，很明显当前程序的耦合度是较高的，如果我们缺失了某个类，例如`AccountDaoImpl`，编译就无法通过

  ​	![1565798483650](spring笔记.assets/1565798483650.png)

- 我们可以使用工厂设计模式来缓解这个问题：

  ```java
  /**
   * @author ajacker
   * javabean的工厂
   */
  public class BeanFactory {
      /**
       * 定义一个Properties对象
       */
      private static Properties props;
      //使用静态代码块为Properties对象赋值
      static {
          try {
              //实例化对象
              props = new Properties();
              //获取Properties流对象
              InputStream in = BeanFactory.class.getClassLoader().getResourceAsStream("bean.properties");
              props.load(in);
          } catch (IOException e) {
              throw new ExceptionInInitializerError("初始化Properties失败");
          }
      }
  
      /**
       * 根据bean的名称返回对象
       * @param beanName bean名
       * @return 对应类型的对象
       */
      public static Object getBean(String beanName){
          Object bean = null;
          String beanPath = props.getProperty(beanName);
          try {
              //反射创建对象
              bean = Class.forName(beanPath).newInstance();
          } catch (Exception e) {
              e.printStackTrace();
          }
          return bean;
      }
  }
  ```

  我们通过读取`bean.properties`文件中的全限定类名，**使用反射来创建对象**，于此同时，我们表现层和业务层的代码做相应修改：

  ```java
  //使用工厂创建业务层对象
  IAccountService as = (IAccountService) BeanFactory.getBean("accountService");
  //使用工厂创建持久层对象
  private IAccountDao accountDao = (IAccountDao) BeanFactory.getBean("accountDao");
  ```

此时我们的程序在编译期间已经不硬性依赖于`AccountDaoImpl`和`AccountServiceImpl`类了，而是**在运行期间通过读取配置文件中的全限定类名来反射创建**我们所需要的对象。

### 工厂设计模式存在的问题和改造

我们循环创建四个对象并打印

![1565847387511](spring笔记.assets/1565847387511.png)

可以清楚的看见创建的是不同的对象，也就是说之前我们的模式是**多例**的，每次获得的对象都是新创建的

现在我们对之前的设计进行一些改进

- 在`BeanFactory`类中添加一个`Map`，来保存`key`和具体对象的映射关系
- 在静态代码块中添加相应的代码来初始化这个`Map`

- 将获取对象的`getBean()`方法改为从`Map`中获得

全部改造完成以后大概是这个样子：

```java
/**
 * @author ajacker
 * javabean的工厂
 */
public class BeanFactory {
    /**
     * 定义一个Properties对象
     */
    private static Properties props;

    /**
     * 定义一个Map用来保存创建的对象
     */
    private static Map<String,Object> beans;
    //使用静态代码块为Properties对象赋值
    static {
        try {
            //实例化对象
            props = new Properties();
            //获取Properties流对象
            InputStream in = BeanFactory.class.getClassLoader().getResourceAsStream("bean.properties");
            props.load(in);
            //实例化容器
            beans = new HashMap<>();
            //取出配置文件中所有的keys
            Enumeration keys = props.keys();
            //遍历枚举
            while (keys.hasMoreElements()){
                //取出每个key
                String key = keys.nextElement().toString();
                //获取对应的value
                String beanPath = props.getProperty(key);
                //反射创建对象
                Object value = Class.forName(beanPath).newInstance();
                //把key和value存入容器
                beans.put(key,value);
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError("初始化Properties失败");
        }
    }

    /**
     * 根据bean的名称返回对象
     * @param beanName bean名
     * @return 对应类型的对象
     */
    public static Object getBean(String beanName){
        return beans.get(beanName);
//        Object bean = null;
//        String beanPath = props.getProperty(beanName);
//        try {
//            //反射创建对象
//            bean = Class.forName(beanPath).newInstance();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return bean;
    }
}

```

这时候我们再尝试运行，发现已经成为了**单例**的了：

![1565847850181](spring笔记.assets/1565847850181.png)



### 二、使用Spring解决程序耦合

#### 准备工作

1. 使用maven引入依赖,创建maven项目,配置其`pom.xml`如下:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.9.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.ajacker</groupId>
    <artifactId>springlearn</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

2. 在`resources`下创建bean的配置文件`bean.xml`

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
       <!--把对象的创建交给spring管理-->
       <bean id="accountService" class="com.ajacker.xmltest.service.impl.AccountServiceImpl"/>
       <bean id="accountDao" class="com.ajacker.xmltest.dao.impl.AccountDaoImpl"/>
   </beans>
   ```

#### 修改表现层代码，通过spring创建对象

1. 将ui层代码修改为，通过容器获取了对象

   ```java
   /**
    * @author ajacker
    * 模拟一个表现层
    */
   public class Client {
       public static void main(String[] args) {
           //获取核心容器对象
           ApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
           //根据id获取对象
           IAccountService as = ac.getBean("accountService", IAccountService.class);
           IAccountDao adao = ac.getBean("accountDao", IAccountDao.class);
           as.saveAccount();
       }
   }
   ```

2. 运行查看结果，发现可以正常执行业务逻辑

   ![1570378333484](spring笔记.assets/1570378333484.png)

### 三、使用XML配置文件实现IOC详解

#### ApplicationContext方式

- 特点：
  - 读取完配置文件，**立马创建对象等待使用**
  - 单例对象使用
    - **我们实际开发中使用这个，因为加载策略会随配置文件改变**

##### ApplicationContext三个常用实现类

![1570379339445](spring笔记.assets/1570379339445.png)

`ClassPathXmlApplicationContext,FileSystemXmlApplicationContext,AnnotationConfigApplicationContext`.

- `ClassPathXmlApplicationContext`: 它是从类的根路径下加载配置文件
- `FileSystemXmlApplicationContext`: 它是从磁盘路径上加载配置文件
- `AnnotationConfigApplicationContext`: 读取注解创建容器

#### BeanFactory方式

- 将表现层代码修改为：

  ```java
  Resource resource = new ClassPathResource("bean.xml");
          BeanFactory factory = new XmlBeanFactory(resource);
          IAccountService as = factory.getBean("accountService", IAccountService.class);
          as.saveAccount();
  ```

- 特点：

  - 什么时候`getBean()`,什么时候创建
  - 多例对象使用

#### 使用XML配置文件实现IOC

##### bean标签

- 作用: 配置托管给spring的对象,默认情况下调用类的无参构造函数,若果没有无参构造函数则不能创建成功
- 属性:
  - `id`: 指定对象在容器中的标识,将其作为参数传入getBean()方法可以获取获取对应对象.
  - `class`: 指定类的全类名,默认情况下调用无参构造函数
  - `scope`: 指定对象的作用范围,可选值如下
    - `singleton`: 单例对象,默认值
    - `prototype`: 多例对象
    - `request`: 将对象存入到web项目的request域中
    - `session`: 将对象存入到web项目的session域中
    - `global session`: 将对象存入到web项目集群的session域中,若不存在集群,则global session相当于session
  - `init-method`：指定类中的初始化方法名称,在对象创建成功之后执行
  - `destroy-method`：指定类中销毁方法名称,对prototype多例对象没有作用,因为多利对象的销毁时机不受容器控制

##### bean作用范围

- 单例对象：`scope="singleton"`
  - 作用范围: **每个应用只有一个该对象的实例**,它的作用范围就是整个应用
  - 生命周期: 单例对象的创建与销毁**和容器的创建与销毁时机一致**
    - 对象出生: 当**应用加载,创建容器时**,对象就被创建
    - 对象活着: 只要**容器存在**,对象一直活着
    - 对象死亡: 当应用卸载,**销毁容器**时,对象就被销毁
- 多例对象：`scope="prototype"`
  - 作用范围: **每次访问对象时,都会重新创建对象实例**.
  - 生命周期: 多例对象的创建与销毁时机**不受容器控制**
    - 对象出生: 当使用对象时,创建新的对象实例
    - 对象活着: 只要对象**在使用中**,就一直活着
    - 对象死亡: 当对象**长时间不用**时,被 java 的垃圾回收器**回收**了

##### 实例化bean的三种方式（threeway）

1. 使用默认无参构造函数创建对象: **默认情况下会根据默认无参构造函数来创建类对象**,若Bean类中**没有默认无参构造函数,将会创建失败**.

```xml
<bean id="accountService" 
	class="cn.maoritian.service.impl.AccountServiceImpl"></bean>
```

2. 使用实例工厂的方法（某个类中的方法）创建对象:

   - 创建一个工厂用于创建对象
   
     ```java
     /**
      * @author ajacker
      * 模拟一个无法修改的用于创建对象的类
      */
     public class InstanceFactory {
         public IAccountService getAccountService(){
             return new AccountServiceImpl();
         }
     }
     ```
   
   - 配置文件中配置工厂和工厂方法
   
     ```xml
     <bean id="instanceFactory" class="com.ajacker.factory.InstanceFactory"/>
     <bean id="accountService" factory-bean="instanceFactory" factory-method="getAccountService"/>
     ```
   
     - `factory-bean`属性: 指定实例工厂的`id`
     - `factory-method`属性: 指定实例工厂中生产对象的方法

3. 使用静态工厂的方法创建对象

   - 创建一个静态工厂：

     ```java
     /**
      * @author ajacker
      * 模拟一个静态工厂类
      */
     public class StaticFactory {
         public static IAccountService getAccountService(){
             return new AccountServiceImpl();
         }
     }
     ```

   - 配置文件中配置静态工厂方法

     ```xml
     <!--第三种方式，使用静态工厂创建对象-->
     <bean id="accountService" class="com.ajacker.factory.StaticFactory" factory-method="getAccountService"/>
     ```

## B、DI依赖注入(DITest)

### 一、依赖注入的概念

- 依赖注入(Dependency Injection)是spring框架**核心ioc的具体实现**.

- 通过**控制反转,我们把创建对象托管给了spring**,但是代码中不可能消除所有依赖,例如:业务层仍然会调用持久层的方法,因此业务层类中应包含持久化层的实现类对象.
  我们**等待框架通过配置的方式将持久层对象传入业务层,而不是直接在代码中new某个具体的持久化层实现类**,这种方式称为依赖注入.

### 二、依赖注入的方法

因为我们是通过反射的方式来创建属性对象的,而不是使用new关键字,因此我们要指定创建出对象各字段的取值.

#### 使用构造函数注入
- 通过类默认的构造函数来给创建类的字段赋值,**相当于调用类的构造方法**.

- 涉及的标签: `<constructor-arg>`用来定义构造函数的参数,其属性可大致分为两类:
  1. 寻找要赋值给的字段
     `index`: 指定参数在构造函数参数列表的索引位置
     `type`: 指定参数在构造函数中的数据类型
     `name`: 指定参数在构造函数中的变量名,最常用的属性
  2. 指定赋给字段的值
     `value`: 给基本数据类型和String类型赋值
     `ref`: 给**其它Bean类型的字段赋值**,ref属性的值应为配置文件中配置的Bean的id

1. 为要注入的类添加字段和构造函数

   ```java
   /**
    * @author ajacker
    * 账户的业务层实现类
    */
   public class AccountServiceImpl implements IAccountService {
       /**
        * 如果数据经常变化 则不适合用配置文件注入
        */
       private String name;
       private Integer age;
       private Date birthday;
   
       public AccountServiceImpl(String name, Integer age, Date birthday) {
           this.name = name;
           this.age = age;
           this.birthday = birthday;
       }
   
       @Override
       public void saveAccount() {
           System.out.println("service中的saveAccount被执行了。。。"+name+","+age+","+birthday);
       }
   }
   ```

2. 配置注入xml

    ```xml
   <!--构造函数注入-->
   <bean id="accountService" class="com.ajacker.service.impl.AccountServiceImpl">
       <constructor-arg name="name" value="test"/>
       <constructor-arg name="age" value="18"/>
       <constructor-arg name="birthday" ref="now"/>
   </bean>
   <!--配置一个日期对象-->
   <bean id="now" class="java.util.Date"/>
   ```

3. 运行结果

   ![1570456066190](spring笔记.assets/1570456066190.png)

#### 使用set注入(常用)

- 在类中提供需要注入成员属性的set方法,创建对象只调用要赋值属性的set方法.

- 涉及的标签: `<property>`,用来定义要调用set方法的成员. 其主要属性可大致分为两类:

  1. 指定要调用set方法赋值的成员字段
     - `name`：要调用set方法赋值的成员字段

  2. 指定赋给字段的值
     - `value`: 给基本数据类型和String类型赋值
     - `ref`: 给其它Bean类型的字段赋值,ref属性的值应为配置文件中配置的Bean的id

1. 为要注入的类添加Setter方法

   ```java
   package com.ajacker.service.impl;
   
   import com.ajacker.service.IAccountService;
   import java.util.Date;
   
   /**
    * @author ajacker
    * 账户的业务层实现类
    */
   public class AccountServiceImpl2 implements IAccountService {
       /**
        * 如果数据经常变化 则不适合用配置文件注入
        */
       private String name;
       private Integer age;
       private Date birthday;
   
       public void setUserName(String name) {
           this.name = name;
       }
   
       public void setAge(Integer age) {
           this.age = age;
       }
   
       public void setBirthday(Date birthday) {
           this.birthday = birthday;
       }
   
       @Override
       public void saveAccount() {
           System.out.println("service中的saveAccount被执行了。。。"+name+","+age+","+birthday);
       }
   }
   ```

2. 配置注入xml

   ```xml
   <!--set方法注入-->
   <bean id="accountService2" class="com.ajacker.service.impl.AccountServiceImpl2">
   	<!--这里的name属性是setXXX的XXX，不一定非得是属性名-->
   	<property name="userName" value="test"/>
   	<property name="age" value="19"/>
   	<property name="birthday" ref="now"/>
   </bean>
   <!--配置一个日期对象-->
   <bean id="now" class="java.util.Date"/>
   ```

#### 注入集合字段

- 集合字段及其对应的标签按照集合的结构分为两类: **相同结构的集合标签之间可以互相替换.**

  1. 只有键的结构:
     - 数组字段:` <array>`标签表示集合,`<value>`标签表示集合内的成员.
     - List字段: `<list>`标签表示集合,`<value>`标签表示集合内的成员.
     - Set字段: `<set>`标签表示集合,`<value>`标签表示集合内的成员.
       其中`<array>,<list>,<set>`标签之间**可以互相替换**使用.

  2. 键值对的结构:
     - Map字段: `<map>`标签表示集合,`<entry>`标签表示集合内的键值对,其key属性表示键,value属性表示值.
     - Properties字段: `<props>`标签表示集合,`<prop>`标签表示键值对,其key属性表示键,标签内的内容表示值.
     - 其中`<map>,<props>`标签之间,`<entry>,<prop>`标签之间**可以互相替换**使用.

1. 为注入的类添加复杂类型（集合）字段

   ```java
   package com.ajacker.service.impl;
   
   
   import com.ajacker.service.IAccountService;
   
   import java.util.*;
   
   /**
    * @author ajacker
    * 账户的业务层实现类
    */
   public class AccountServiceImpl3 implements IAccountService {
       private String[] myStrs;
       private List<String> myList;
       private Set<String> mySet;
       private Map<String,String> myMap;
       private Properties myProps;
   
       public void setMyStrs(String[] myStrs) {
           this.myStrs = myStrs;
       }
   
       public void setMyList(List<String> myList) {
           this.myList = myList;
       }
   
       public void setMySet(Set<String> mySet) {
           this.mySet = mySet;
       }
   
       public void setMyMap(Map<String, String> myMap) {
           this.myMap = myMap;
       }
   
       public void setMyProps(Properties myProps) {
           this.myProps = myProps;
       }
   
       @Override
       public void saveAccount() {
           System.out.println(Arrays.toString(myStrs));
           System.out.println(myList);
           System.out.println(mySet);
           System.out.println(myMap);
           System.out.println(myProps);
       }
   }
   ```

2. 配置xml文件注入

   ```xml
   <!--集合类型的注入-->
       <bean id="accountService3" class="com.ajacker.service.impl.AccountServiceImpl3">
           <!--这里的name属性是setXXX的XXX，不一定非得是属性名-->
   
           <property name="myStrs">
               <array>
                   <value>AAA</value>
                   <value>BBB</value>
                   <value>CCC</value>
               </array>
           </property>
           <property name="myList">
               <list>
                   <value>AAA</value>
                   <value>BBB</value>
                   <value>CCC</value>
               </list>
           </property>
           <property name="mySet">
               <set>
                   <value>AAA</value>
                   <value>BBB</value>
                   <value>CCC</value>
               </set>
           </property>
           <property name="myMap">
               <map>
                   <entry key="keyA" value="AAA"/>
                   <entry key="keyB" value="BBB"/>
                   <entry key="keyC" value="CCC"/>
               </map>
           </property>
           <property name="myProps">
               <props>
                   <prop key="AAA">pAA</prop>
                   <prop key="BBB">pBB</prop>
                   <prop key="CCC">pCC</prop>
               </props>
           </property>
       </bean>
   ```

3. 运行结果

   ```
   [AAA, BBB, CCC]
   [AAA, BBB, CCC]
   [AAA, BBB, CCC]
   {keyA=AAA, keyB=BBB, keyC=CCC}
   {AAA=pAA, CCC=pCC, BBB=pBB}
   ```

## C、使用注解实现IOC

### 一、常用注解

#### 用于创建对象的注解

这些注解的作用**相当于**`bean.xml`中的`<bean>`标签

- `@Component`: 把当前类对象存入spring容器中,其属性如下:
  - `value`: 用于指定当前类的id. 不写时**默认值是当前类名,且首字母改小写**
- `@Controller`: 将当前表现层对象存入spring容器中
- `@Service`: 将当前业务层对象存入spring容器中
- `@Repository`: 将当前持久层对象存入spring容器中
- `@Controller,@Service,@Repository`注解的作用和属性与@Component是一模一样的,**可以相互替代**,它们的作用是使三层对象的分别更加清晰.

#### 用于注入数据的注解

这些注解的作用**相当于**`bean.xml`中的`<property>`标签

- `@Autowired`: 自动按照**成员变量类型**注入.
  - 注入过程
    - 当spring容器中**有且只有一个对象的类型与要注入的类型相同**时,注入该对象.
    - 当spring容器中有**多个对象类型**与要注入的类型相同时,**使用要注入的变量名作为bean的id**,在spring - 容器查找,找到则注入该对象.找不到则报错.
  - 出现位置: 既可以在变量上,也可以在方法上
  - 细节: 使用注解注入时,set方法可以省略
- `@Qualifier`: 在**自动按照类型注入的基础之上,再按照bean的id注入.**
  - 出现位置: 既可以在变量上,也可以在方法上.注入变量时不能独立使用,必须和`@Autowired`一起使用; **注入方法时可以独立使用**.
  - 属性:
    - value: 指定bean的id
- `@Resource`: **直接按照bean的id注入**,它可以独立使用.独立使用时相当于同时使用`@Autowired`和`@Qualifier`两个注解.
  - 属性:
    - name: 指定bean的id
- `@Value`: 注入**基本数据类型和String类型数据**
  - 属性:
    - value: 用于指定数据的值,可以使用el表达式(`${表达式}`)

#### 用于改变作用范围的注解
这些注解的作用相当于`bean.xml`中的`<bean>`标签的`scope`属性.

- `@Scope`: 指定`bean`的作用范围
  - 属性:
    - `value`: 用于指定作用范围的取值,`"singleton","prototype","request","session","globalsession"`

#### 和生命周期相关的注解

这些注解的作用相当于`bean.xml`中的`<bean>`标签的`init-method`和`destroy-method`属性

- `@PostConstruct`: 用于指定初始化方法

- `@PreDestroy`: 用于指定销毁方法

#### 这些注解的一个例子:

> `AccountServiceImpl`类：
>
> ```java
> @Service(value = "accountService")
> @Scope(value = "singleton")
> public class AccountServiceImpl implements IAccountService {
>     @Autowired
>     @Qualifier(value = "accountDao")
>     private IAccountDao accountDao;
> 
>     @PostConstruct
>     public void init(){
>         System.out.println("初始化方法调用");
>     }
> 
>     @PreDestroy
>     public void destroy(){
>         System.out.println("销毁方法调用");
>     }
> 
>     @Override
>     public void saveAccount() {
>         accountDao.saveAccount();
>     }
> }
> ```
>
> 表现层代码:
>
> ```java
> public class Client {
>     public static void main(String[] args) {
>         //获取核心容器对象
>         AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
>         //根据id获取对象
>         IAccountService as = ac.getBean("accountService", IAccountService.class);
>         IAccountService as2 = ac.getBean("accountService", IAccountService.class);
>         System.out.println("是否为单例："+as.equals(as2));
>         as.saveAccount();
>         ac.close();
>     }
> }
> ```
>
> 结果：
>
> ```
> 初始化方法调用
> 是否为单例：true
> 保存了账户
> 销毁方法调用
> ```
>
> 如果设置为多例模式（`@Scope(value = "prototype")`）的结果为
>
> ```
> 初始化方法调用
> 初始化方法调用
> 是否为单例：false
> 保存了账户
> ```
>
> 可以看到调用了两次初始化方法，没有**调用销毁是因为多例对象的死亡由java的GC自动管理**

### 二、spring的半注解配置和纯注解配置

#### 半注解配置

在半注解配置下,spring容器仍然使用`ClassPathXmlApplicationContext`类从`xml`文件中读取`IOC`配置,同时在`xml`文件中**告知spring创建容器时要扫描的包.**

例如,使用半注解模式时,上述简单实例中的`beans.xml`内容如下:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">
    <!--告知Spring需要扫描注解的包-->
    <context:component-scan base-package="com.ajacker"/>
</beans>
```

#### 纯注解配置

在纯注解配置下,我们用配置类替代`bean.xml`,spring容器使用`AnnotationApplicationContext`类从spring配置类中读取IOC配置

#### 关于纯注解相关的注解

- `@Configuration`: 用于**指定当前类是一个spring配置类**,当创建容器时会从该类上加载注解.获取容器时需要使用AnnotationApplicationContext(有@Configuration注解的类.class).
- `@ComponentScan`: 指定spring在初始化容器时要扫描的包,作用和`bean.xml `文件中<context:component-scan base-package="要扫描的包名"/>是一样的. 其属性如下:
  - `value/basePackages`: 用于指定要扫描的包,是value属性的别名
- `@Bean`: 该注解只能写在方法上,表明使用此方法创建一个对象,并放入spring容器,其属性如下:
  - `name`: 指定此方法创建出的bean对象的id
  - 细节: 使用注解配置方法时,如果方法有参数,Spring框架会到容器中查找有没有可用的bean对象,查找的方式与@Autowired注解时一样的.
- `@PropertySource`: 用于加载properties配置文件中的配置.例如配置数据源时,可以把连接数据库的信息写到properties配置文件中,就可以使用此注解指定properties配置文件的位置,其属性如下:
  - `value`: 用于指定properties文件位置.如果是在类路径下,需要写上"classpath:"
- `@Import`: 用于**导入其他配置类**.当我们使用@Import注解之后,有@Import注解的类就是父配置类,而导入的都是子配置类. 其属性如下:
  - `value`: 用于指定其他配置类的字节码

## D、案例

### 一、纯注解配置案例（XmlIOCTest）

#### 1. 编写业务层

- 业务层接口`IAccountService.java`

  ```java
  /**
   * @author ajacker
   * 账户的业务层接口
   */
  public interface IAccountService {
  
      /**
       * 查询所有
       * @return
       */
      List<Account> findAllAccount();
  
      /**
       * 查询一个
       * @param id
       * @return
       */
      Account findAccountById(Integer id);
  
      /**
       *  保存操作
       * @param account
       */
      void saveAccount(Account account);
  
      /**
       * 更新
       * @param account
       */
      void updateAccount(Account account);
  
      /**
       * 删除
       * @param id
       */
      void deleteAccount(Integer id);
  }
  ```

- 业务层实现类`AccountServiceImpl.java`，调用持久层对象的函数

  ```java
  /**
   * @author ajacker
   * 账户的业务层实现类
   */
  public class AccountServiceImpl implements IAccountService {
      private IAccountDao accountDao;
  
      public void setAccountDao(IAccountDao accountDao) {
          this.accountDao = accountDao;
      }
  
      @Override
      public List<Account> findAllAccount() {
          return accountDao.findAllAccount();
      }
  
      @Override
      public Account findAccountById(Integer id) {
          return accountDao.findAccountById(id);
      }
  
      @Override
      public void saveAccount(Account account) {
          accountDao.saveAccount(account);
      }
  
      @Override
      public void updateAccount(Account account) {
          accountDao.updateAccount(account);
      }
  
      @Override
      public void deleteAccount(Integer id) {
          accountDao.deleteAccount(id);
      }
  
  }
  ```

#### 2. 编写持久层

- 持久层接口`IAccountDao.java`

  ```java
  /**
   * @author ajacker
   * 账户的持久层接口
   */
  public interface IAccountDao {
      /**
       * 查询所有
       * @return
       */
      List<Account> findAllAccount();
  
      /**
       * 查询一个
       * @return
       */
      Account findAccountById(Integer id);
  
      /**
       *  保存操作
       * @param account
       */
      void saveAccount(Account account);
  
      /**
       * 更新
       * @param account
       */
      void updateAccount(Account account);
  
      /**
       * 删除
       * @param id
       */
      void deleteAccount(Integer id);
  }
  ```

- 持久层实现类`AccountDaoImpl.java`，完成数据库的操作

  ```java
  /**
   * @author ajacker
   */
  public class AccountDaoImpl implements IAccountDao {
      private QueryRunner runner;
  
      public void setRunner(QueryRunner runner) {
          this.runner = runner;
      }
  
      @Override
      public List<Account> findAllAccount() {
          try {
              return runner.query("select * from account", new BeanListHandler<>(Account.class));
          } catch (SQLException e) {
              throw new RuntimeException();
          }
      }
  
      @Override
      public Account findAccountById(Integer id) {
          try {
              return runner.query("select * from account where id = ?", new BeanHandler<>(Account.class),id);
          } catch (SQLException e) {
              throw new RuntimeException();
          }
      }
  
      @Override
      public void saveAccount(Account account) {
          try {
              runner.update("insert into account(name,money) values(?,?)",account.getName(),account.getMoney());
          } catch (SQLException e) {
              throw new RuntimeException();
          }
      }
  
      @Override
      public void updateAccount(Account account) {
          try {
              runner.update("update account set name=?,money=? where id=?",account.getName(),account.getMoney(),account.getId());
          } catch (SQLException e) {
              throw new RuntimeException();
          }
      }
  
      @Override
      public void deleteAccount(Integer id) {
          try {
              runner.update("delete from account where id=?",id);
          } catch (SQLException e) {
              throw new RuntimeException();
          }
      }
  
  }
  
  ```

#### 3. 编写控制层

- 实体类`Account.java`

  ```java
  /**
   * @author ajacker
   * 账户的实体类
   */
  public class Account {
      private Integer id;
      private String name;
      private Float money;
  
      public Integer getId() {
          return id;
      }
  
      public void setId(Integer id) {
          this.id = id;
      }
  
      public String getName() {
          return name;
      }
  
      public void setName(String name) {
          this.name = name;
      }
  
      public Float getMoney() {
          return money;
      }
  
      public void setMoney(Float money) {
          this.money = money;
      }
  
      @Override
      public String toString() {
          return "Account{" +
                  "id=" + id +
                  ", name='" + name + '\'' +
                  ", money=" + money +
                  '}';
      }
  }
  ```

#### 4. 配置xml

- `bean.xml`，其中注意使用的`property`代表需要提供`setter`方法注入

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans.xsd">
  
      <!--配置Service-->
      <bean id="accountService" class="com.ajacker.service.impl.AccountServiceImpl">
          <!--注入dao-->
          <property name="accountDao" ref="accountDao"/>
      </bean>
      <!--配置Dao对象-->
      <bean id="accountDao" class="com.ajacker.dao.impl.AccountDaoImpl">
          <property name="runner" ref="queryRunner"/>
      </bean>
      <!--配置QueryRunner对象-->
      <bean id="queryRunner" class="org.apache.commons.dbutils.QueryRunner" scope="prototype">
          <!--注入数据源-->
          <constructor-arg name="ds" ref="dataSource"/>
      </bean>
      <!--配置数据源-->
      <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
          <!--连接数据库的必要信息-->
          <property name="driverClass" value="com.mysql.cj.jdbc.Driver"/>
          <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai"/>
          <property name="user" value="root"/>
          <property name="password" value="456852"/>
      </bean>
  </beans>
  ```

#### 5. 编写测试类

- `AccountServiceTest.java`，使用`Junit`进行单元测试

  ```java
  /**
   * 使用junit单元测试配置
   */
  public class AccountServiceTest {
      @Test
      public void testFindAll() {
          //1.获取容器
          AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
          //2.获取业务层对象
          IAccountService as = ac.getBean("accountService",IAccountService.class);
          //3.执行方法
          List<Account> accounts = as.findAllAccount();
          accounts.forEach(System.out::println);
      }
      @Test
      public void testFindOne() {
          //1.获取容器
          AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
          //2.获取业务层对象
          IAccountService as = ac.getBean("accountService",IAccountService.class);
          //3.执行方法
          Account account = as.findAccountById(1);
          System.out.println(account);
      }
      @Test
      public void testSave() {
          //1.获取容器
          AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
          //2.获取业务层对象
          IAccountService as = ac.getBean("accountService",IAccountService.class);
          //3.执行方法
          Account account = new Account();
          account.setName("test");
          account.setMoney(1234f);
          as.saveAccount(account);
          System.out.println(account);
      }
      @Test
      public void testUpdate() {
          //1.获取容器
          AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
          //2.获取业务层对象
          IAccountService as = ac.getBean("accountService",IAccountService.class);
          //3.执行方法
          Account account = as.findAccountById(1);
          account.setMoney(555f);
          as.updateAccount(account);
          System.out.println(account);
      }
      @Test
      public void testDelete() {
          //1.获取容器
          AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
          //2.获取业务层对象
          IAccountService as = ac.getBean("accountService",IAccountService.class);
          //3.执行方法
          as.deleteAccount(3);
      }
  }
  ```

### 二、半注解配置案例（HalfXmlIOCTest）

#### 1. 修改业务层

- 删除`setter`方法，修改为注解形式

  ```java
  @Service("accountService")
  public class AccountServiceImpl implements IAccountService {
  
      @Resource(name = "accountDao")
      private IAccountDao accountDao;
  
      .....
  
  }
  ```

#### 2. 修改持久层

- 删除`setter`方法，修改为注解形式

  ```java
  /**
   * @author ajacker
   */
  @Repository("accountDao")
  public class AccountDaoImpl implements IAccountDao {
  
      @Resource(name = "queryRunner")
      private QueryRunner runner;
  
      ....
  
  }
  
  ```

#### 3. 修改xml配置

- 修改`xml`，添加命名空间依赖，删除可用注解代替的`bean`标签

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:context="http://www.springframework.org/schema/context"
         xsi:schemaLocation="http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans.xsd
          http://www.springframework.org/schema/context
          http://www.springframework.org/schema/context/spring-context.xsd">
  
      <context:component-scan base-package="com.ajacker"/>
  
      <!--配置QueryRunner对象-->
      <bean id="queryRunner" class="org.apache.commons.dbutils.QueryRunner" scope="prototype">
          <!--注入数据源-->
          <constructor-arg name="ds" ref="dataSource"/>
      </bean>
      <!--配置数据源-->
      <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
          <!--连接数据库的必要信息-->
          <property name="driverClass" value="com.mysql.cj.jdbc.Driver"/>
          <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai"/>
          <property name="user" value="root"/>
          <property name="password" value="456852"/>
      </bean>
  </beans>
  ```

### 三、纯注解配置案例（AnnotationIOCTest）

#### 1. 新建总配置类

- `config.SpringConfiguration`，并用注解配置扫描的包和子配置类字节码

  ```java
  /**
   * @author ajacker
   * 此类是配置类，和bean.xml作用一样
   */
  @Configuration
  @ComponentScan(basePackages = "com.ajacker")
  @Import(JdbcConfig.class)
  public class SpringConfiguration {
  
  }
  ```

#### 2. 创建子配置类

- `config.JdbcConfiguration`，并配置`properties`文件源，注入jdbc相关的配置和bean

  ```java
  /**
   * @author ajacker
   * 关于数据库的配置类
   */
  @Configuration
  @PropertySource("classpath:jdbcConfig.properties")
  public class JdbcConfig {
      @Value("${jdbc.driver}")
      private String driver;
      @Value("${jdbc.url}")
      private String url;
      @Value("${jdbc.username}")
      private String username;
      @Value("${jdbc.password}")
      private String password;
  
      /**
       * 用于创建一个QueryRunner对象
       * @param dataSource 数据源
       * @return 对象
       */
      @Bean(name = "queryRunner")
      public QueryRunner createQueryRunner(DataSource dataSource){
          return new QueryRunner(dataSource);
      }
  
      /**
       * 创建数据源对象
       * @return
       */
      @Bean(name = "dataSource")
      @Scope("prototype")
      public DataSource createDataSource(){
          ComboPooledDataSource dataSource = new ComboPooledDataSource();
          try {
              dataSource.setDriverClass(driver);
              dataSource.setJdbcUrl(url);
              dataSource.setUser(username);
              dataSource.setPassword(password);
          } catch (PropertyVetoException e) {
              throw new RuntimeException(e);
          }
          return dataSource;
      }
  }
  
  ```

#### 3. 创建jdbc配置文件

- 在`resources`下创建配置文件`jdbcConfig.properties`

  ```properties
  jdbc.driver=com.mysql.cj.jdbc.Driver
  jdbc.url=jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai
  jdbc.username=root
  jdbc.password=456852
  ```

#### 4. 修改测试类

- 将获取容器的实现类修改为`AnnotationConfigApplicationContext`

  ```java
  AbstractApplicationContext ac = new AnnotationConfigApplicationContext(SpringConfiguration.class);
  ```

## E. 其它

### 一、Spring整合Junit

1. 在测试类上添加注解，配置使用spring测试，配置配置文件或配置类

   ```java
   @RunWith(SpringJUnit4ClassRunner.class)
   @ContextConfiguration(classes = SpringConfiguration.class)
   ```

2. 在测试类中使用注解注入

   ```java
       @Resource(name = "accountService")
       private IAccountService as;
   ```

# 第二部分 AOP面向切片编程

## A. 代码冗余与装饰器模式(AOPTest)

### 一、代码冗余现象

- 为了保证数据库的一致性，我们添加了事务控制，但是这样使得每个数据库操作都要加上重复的事务控制的代码,如下:

  ```java
  @Override
      public Account findAccountById(Integer id) {
          try {
              //1.开启事务
              transactionManager.beginTransaction();
              //2.执行操作
              Account account = accountDao.findAccountById(id);
              //3.提交事务
              transactionManager.commit();
              //4.返回结果
              return account;
          }catch (Exception e){
              //5.回滚操作
              transactionManager.rollback();
          }finally {
              //6.释放连接
              transactionManager.release();
          }
          return null;
      }
  
      @Override
      public void saveAccount(Account account) {
          try {
              //1.开启事务
              transactionManager.beginTransaction();
              //2.执行操作
              accountDao.saveAccount(account);
              //3.提交事务
              transactionManager.commit();
          }catch (Exception e){
              //5.回滚操作
              transactionManager.rollback();
          }finally {
              //6.释放连接
              transactionManager.release();
          }
      }
  ```

- 这会导致两个问题：
  - 业务层方法变得臃肿了,里面充斥着很多重复代码（事务控制）
  - 业务层方法和事务控制方法耦合高. 若提交,回滚,释放资源中任何一个**方法名变更,都需要修改业务层的代码**

### 二、动态代理解决方案

- 我们使用动态代理对上述Service进行改造,创建`BeanFactory`类作为service层**对象工厂**,通过其`getAccountService`方法得到业务层对象

    ```java
    /**
     * @author ajacker
     * 用于创建Service的代理对象的工厂
     */
    @Component
    public class BeanFactory {
    private final IAccountService accountService;
        private final TransactionManager transactionManager;

        public BeanFactory(TransactionManager transactionManager, IAccountService accountService) {
            this.transactionManager = transactionManager;
            this.accountService = accountService;
        }
    
        @Bean("proxyAccountService")
        public IAccountService getAccountService(){
            return (IAccountService) Proxy.newProxyInstance(accountService.getClass().getClassLoader(),
                accountService.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        Object rtValue;
                        try {
                            //1.开启事务
                        transactionManager.beginTransaction();
                            //2.执行操作
                            rtValue = method.invoke(accountService, args);
                            //3.提交事务
                            transactionManager.commit();
                            //4.返回结果
                            return rtValue;
                        }catch (Exception e){
                            //5.回滚操作
                            transactionManager.rollback();
                            throw new RuntimeException(e);
                        }finally {
                            //6.释放连接
                            transactionManager.release();
                        }
                    });
        }
    }
    
    ```
- 将业务层代码恢复到之前没有事务控制的情况:

  ```java
   @Override
      public List<Account> findAllAccount() {
          return accountDao.findAllAccount();
      }
  
      @Override
      public Account findAccountById(Integer id) {
          return accountDao.findAccountById(id);
      }
  ```

- 将测试类中的对象注入改为代理后的业务层对象(`"proxyAccountService"`)

    ```java
    /**
     * 使用junit单元测试配置
     */
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration("classpath:bean.xml")
    public class AccountServiceTest {
        @Resource(name = "proxyAccountService")
        private IAccountService as;
    
        @Test
        public void testFindAll() {
            List<Account> accounts = as.findAllAccount();
            accounts.forEach(System.out::println);
        }
    
        @Test
        public void testTransfer() {
            as.transfer("aaa", "bbb", 100f);
        }
    }
    
    ```

- 此时我们就通过`Spring `获取了动态代理过的对象

## B. AOP解决代码冗余

### 一、 AOP相关术语

  - Joinpoint(连接点): 被拦截到的方法.

  - Pointcut(切入点): 我们对其进行增强的方法.

  - Advice(通知/增强): 对切入点进行的增强操作
    - 包括前置通知,后置通知,异常通知,最终通知,环绕通知

  - Weaving(织入): 是指把增强应用到目标对象来创建新的代理对象的过程。

  - Aspect(切面): 是切入点和通知的结合

### 二、 使用XML配置AOP的步骤

#### 1. 添加Aop的依赖

```xml
<dependency>
          <groupId>org.aspectj</groupId>
          <artifactId>aspectjweaver</artifactId>
          <version>1.9.4</version>
</dependency>
```

#### 2. 在`bean.xml`中引入约束

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
</beans>
```

#### 3. 使用`<aop:config>`标签声明AOP配置

```xml
<aop:config>
    <!--aop相关的配置-->
</aop:config>
```

#### 4. 使用`<aop:aspect>`标签配置切面

- `id`: 指定切面的`id`
- `ref`: 引用通知类的`id`

```xml
<aop:config>
	<aop:aspect id="logAdvice" ref="logger">
    	<!--配置通知的类型要写在此处-->
    </aop:aspect>
</aop:config>
```

#### 5. 使用`<aop:pointcut>`配置切入点

- `id`: 指定切入点表达式的`id`
- `expression`: 指定**切入点表达式**

```xml
<aop:config>
        <aop:aspect id="logAdvice" ref="logger">
    		<!--配置切入点-->
            <aop:pointcut id="accountServicePoints" expression="execution( * com.ajacker.service.impl.AccountServiceImpl.*(..))"/>
        </aop:aspect>
</aop:config>
```

#### 6. 配置具体的通知方法

- 类型：
  - `<aop:before>`: 配置前置通知,指定的增强方法在切入点方法之前执行.
  - `<aop:after-returning>`: 配置后置通知,指定的增强方法在切入点方法正常执行之后执行.
  - `<aop:after-throwing>`: 配置异常通知,指定的增强方法在切入点方法产生异常后执行.
  - `<aop:after>`: 配置最终通知,无论切入点方法执行时是否发生异常,指定的增强方法都会最后执行.
  - `<aop:around>`: 配置环绕通知,可以在代码中手动控制增强代码的执行时机.
  
- 属性：

  - `method`: 指定通知类中的增强方法名.
  - `ponitcut-ref`: 指定切入点的表达式的`id`
  - `poinitcut`: 指定切入点表达式

  > 其中`pointcut-ref`和`point-ref`属性只能有其中一个

- 一个例子：

  ```xml
  <!--配置aop-->
  <aop:config>
      <aop:aspect id="logAdvice" ref="logger">
          <aop:pointcut id="pt" expression="execution( * com.ajacker.service.impl.AccountServiceImpl.*(..))"/>
          <!--前置通知-->
          <aop:before method="printLogBefore" pointcut-ref="pt"/>
          <!--异常通知-->
          <aop:after-throwing method="printLogAfterThrowing" pointcut-ref="pt"/>
          <!--后置通知-->
          <aop:after-returning method="printLogAfterReturning" pointcut-ref="pt"/>
          <!--最终通知-->
          <aop:after method="printLogAfter" pointcut-ref="pt"/>
          <!--环绕通知-->
          <aop:around method="printLogAround" pointcut-ref="pt"/>
      </aop:aspect>
  </aop:config>
  ```

  

#### *.1 切入点表达式

- 格式：`execution([修饰符] 返回值类型 包路径.类名.方法名(参数))`

- 写法：

  - 完全形式：

    ```xml
    <aop:pointcut expression="execution( public void com.ajacker.service.impl.AccountServiceImpl.saveAccount())" id="pt"/>
    ```

  - 省略访问修饰符：

    ```xml
    <aop:pointcut expression="execution( void com.ajacker.service.impl.AccountServiceImpl.saveAccount())" id="pt"/>
    ```

  - 用`*`表示任意返回值:

    ```xml
    <aop:pointcut expression="execution( * com.ajacker.service.impl.AccountServiceImpl.saveAccount())" id="pt"/t>
    ```

  - 用`*`表示任意包，但是`*.`的**个数要和包的层级数相匹配**

    ```xml
    <aop:pointcut expression="execution( * *.*.*.*.AccountServiceImpl.saveAccount())" id="pt"/>
    ```

  - 用`*..`表示当前包及其子包

    ```xml
    <aop:pointcut expression="execution( * *..AccountServiceImpl.saveAccount())" id="pt"/>
    ```

  - 用`*`表示任意类

    ```xml
    <aop:pointcut expression="execution( * *..*.saveAccount())" id="pt"/>
    ```

  - 用`*`表示任意方法

    ```xml
    <aop:pointcut expression="execution( * *..*.*())" id="pt"/>
    ```

  - 用`*`表示任意类型参数（必须有参数，不匹配无参）

    ```xml
    <aop:pointcut expression="execution( * *..*.*(*))" id="pt"/>
    ```

  - 参数内用`..`表示有无参数均可，任意类型也可(**全通配写法**)

    ```xml
    <aop:pointcut expression="execution( * *..*.*(..))" id="pt"/>
    ```

- 通常写法:

  一般我们都是对业务层所有实现类的所有方法进行增强,因此切入点表达式写法通常为

  ```xml
  <aop:pointcut id="pt" expression="execution( * com.ajacker.service.impl.AccountServiceImpl.*(..))"/>
  ```

#### *.2 环绕通知

- Spring是基于动态代理对方法进行增强的,`前置通知`,`后置通知`,`异常通知`,`最终通知`在增强方法中的执行时机如下:

  ```java
  // 增强方法
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
      Object rtValue = null;
      try {
          // 执行前置通知
          
          // 执行原方法
          rtValue = method.invoke(accountService, args); 
          
          // 执行后置通知
          return rtValue;
      } catch (Exception e) {
          // 执行异常通知
      } finally {
          // 执行最终通知
      }
  }
  ```

- 我们可通过`环绕通知`，以类似于动态代理的方式更自由地控制增强代码执行的时机

  Spring框架为我们提供一个接口ProceedingJoinPoint,它的实例对象可以作为环绕通知方法的参数,通过参数控制被增强方法的执行时机.

  - `ProceedingJoinPoint`对象的`getArgs()`方法返回被拦截的参数
  - `ProceedingJoinPoint`对象的`proceed()`方法执行被拦截的方法

  ```java
  public Object printLogAround(ProceedingJoinPoint joinPoint) {
      Object rtValue;
      try {
          Object[] args = joinPoint.getArgs();
          System.out.println("Logger类中的printLogAround开始记录日志...前置");
          rtValue = joinPoint.proceed(args);
          System.out.println("Logger类中的printLogAround开始记录日志...后置");
          return rtValue;
      } catch (Throwable throwable) {
          System.out.println("Logger类中的printLogAround开始记录日志...异常");
          throw new RuntimeException(throwable);
      }finally {
          System.out.println("Logger类中的printLogAround开始记录日志...最终");
      }
  }
  ```

## C. 使用注解实现AOP

### 一、 开启AOP注解支持

- 在`bean.xml`添加：

  ```xml
  <aop:aspectj-autoproxy></aop:aspectj-autoproxy>
  ```

### 二、 常用注解

#### 1. 用于声明切面的注解

- `@Aspect`:声明当前类为通知类,该类**定义了一个切面**.相当于xml配置中的`<aop:aspect>`标签

  ```java
  @Component("logger")
  @Aspect
  public class Logger {
      // ...
  }
  ```

#### 2. 用于声明通知的注解

- 类型：
  - `@Before`: 声明该方法为前置通知.相当于xml配置中的`<aop:before>`标签
  - `@AfterReturning`: 声明该方法为后置通知.相当于xml配置中的`<aop:after-returning>`标签
  - `@AfterThrowing`: 声明该方法为异常通知.相当于xml配置中的`<aop:after-throwing>`标签
  - `@After`: 声明该方法为最终通知.相当于xml配置中的`<aop:after>`标签
  - `@Around`: 声明该方法为环绕通知.相当于xml配置中的`<aop:around>`标签

- 属性：
  - `value`:用于指定**切入点表达式或切入点表达式的引用**

#### 3. 用于指定切入点表达式的注解

- `@Pointcut`: 指定切入点表达式,其属性如下:

  - `value`: 指定表达式的内容

  `@Pointcut`注解没有`id`属性,通过调用**被注解的方法**获取切入点表达式.

### 三、半注解配置的例子（AnnotationAOPTest）

#### 1. xml开启支持

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

    <!--配置扫描的包-->
    <context:component-scan base-package="com.ajacker"/>

    <!--开启aop注解支持-->
    <aop:aspectj-autoproxy/>
</beans>
```

#### 2. 配置切面类，切点，通知

```java
/**
 * @author ajacker
 * 记录日志
 */
@Component("logger")
@Aspect
public class Logger {
    @Pointcut("execution( * com.ajacker.service.impl.AccountServiceImpl.*(..))")
    private void pt(){}

    /**
     * 前置通知
     */
    @Before("pt()")
    public void printLogBefore(){
        System.out.println("Logger类中的printLogBefore开始记录日志...");
    }

    /**
     * 后置通知
     */
    @AfterReturning("pt()")
    public void printLogAfterReturning(){
        System.out.println("Logger类中的printLogAfterReturning开始记录日志...");
    }

    /**
     * 异常通知
     */
    @AfterThrowing("pt()")
    public void printLogAfterThrowing(){
        System.out.println("Logger类中的printLogAfterThrowing开始记录日志...");
    }

    /**
     * 最终通知
     */
    @After("pt()")
    public void printLogAfter(){
        System.out.println("Logger类中的printLogAfter开始记录日志...");
    }

    /**
     * 环绕通知
     */
    @Around("pt()")
    public Object printLogAround(ProceedingJoinPoint joinPoint) {
        Object rtValue;
        try {
            Object[] args = joinPoint.getArgs();
            System.out.println("Logger类中的printLogAround开始记录日志...前置");
            rtValue = joinPoint.proceed(args);
            System.out.println("Logger类中的printLogAround开始记录日志...后置");
            return rtValue;
        } catch (Throwable throwable) {
            System.out.println("Logger类中的printLogAround开始记录日志...异常");
            throw new RuntimeException(throwable);
        }finally {
            System.out.println("Logger类中的printLogAround开始记录日志...最终");
        }
    }
}
```

### 四、纯注解配置

在Spring配置类前添加`@EnableAspectJAutoProxy`注解,可以使用纯注解方式配置AOP

```java
@Configuration
@ComponentScan(basePackages="com.ajacker")
@EnableAspectJAutoProxy			// 允许AOP
public class SpringConfiguration {
    // 具体配置
    //...
}
```



### 五、注解配置的BUG！！

- 在使用注解配置AOP时,会出现一个bug. 四个通知的调用顺序依次是:`前置通知`,`最终通知`,`后置通知`. 这会导致一些资源在执行`最终通知`时提前被释放掉了,而执行`后置通知`时就会出错.
- 如果使用注解配置AOP，推荐使用**环绕通知**

# 第三部分 JdbcTemplate

`JdbcTemplate`是Spring框架中提供的一个对象,对原始的JDBC API进行简单封装,其用法与`DBUtils`类似.

## A. 使用方式

### 一、配置数据源

```xml
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai&amp;useSSL=false"/>
    <property name="username" value="root"/>
    <property name="password" value="456852"/>
</bean>
```

### 二、配置JdbcTemplate对象

```xml
<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
    <property name="dataSource" ref="dataSource"/>
</bean>
```

## B. 增删改查（jdbcTemplateTest）

### 一、增加数据

```java
//插入
jt.update("insert into account(name, money) values(?,?)","eee",3333f);
```

### 二、删除数据

```java
//删除
jt.update("delete from account where name=?","aaa");
```

### 三、修改数据

```java
//更新
jt.update("update account set money=? where name=?",6666f,"eee");
```

### 四、查询操作

- 与`DBUtils`十分类似,`JdbcTemplate`的查询操作使用其`query()`方法,其参数如下:

  - `String sql`: SQL语句
  - `RowMapper<T> rowMapper`: 指定如何将查询结果ResultSet对象转换为T对象.
  - `@Nullable Object... args`: SQL语句的参数

- 其中`RowMapper`类类似于`DBUtils`的`ResultSetHandler`类,可以**自己写一个实现类**,但常用Spring框架内置的实现类`BeanPropertyRowMapper<T>(T.class)`(标准Bean)

  - 例如：

    ```java
    /**
     * 定义Account的封装策略
     */
    class AccountRowMapper implements RowMapper<Account>{
    
        /**
         * 把结果集中的数据封装到Account
         * @param resultSet
         * @param i
         * @return
         * @throws SQLException
         */
        @Override
        public Account mapRow(ResultSet resultSet, int i) throws SQLException {
            Account account = new Account();
            account.setId(resultSet.getInt("id"));
            account.setName(resultSet.getString("name"));
            account.setMoney(resultSet.getFloat("money"));
            return account;
        }
    }
    ```

#### 1. 查询所有

```java
List<Account> accounts = jt.query("select * from account where money > ?",new BeanPropertyRowMapper<>(Account.class),100f);
```

或者采用自己实现的`RowMapper`：

```java
List<Account> accounts = jt.query("select * from account where money > ?",new AccountRowMapper(),100f);
```

#### 2. 查询一个

查询一个就是查询所有的特殊情况，我们如果有且只有一个结果取第一个，否则再执行其它逻辑:

```java
List<Account> accounts = jt.query("select * from account where money = ?",new AccountRowMapper(),100f);
Account result = accounts.isEmpty()?null:accounts.get(0);
```

#### 3. 查询聚合函数的结果（或者取某一行某一列）

第二个参数指明了返回的类型：

```java
Integer i = jt.queryForObject("select count(*) from account where money > ?",Integer.class,100f);
```

## C. 在DAO层使用jdbcTemplate（jdbcTemplateDaoTest）

在实际项目中,我们会创建许多`DAO`对象,若每个`DAO`对象都注入一个`JdbcTemplate`对象,会造成代码冗余.

- 实际的项目中我们可以让DAO对象**继承**Spring**内置**的`JdbcDaoSupport`类.在JdbcDaoSupport类中定义了JdbcTemplate和DataSource成员属性,在实际编程中,只需要**向其注入DataSource成员**即可,DataSource的set方法中会注入JdbcTemplate对象.
- DAO的实现类中**调用父类**的`getJdbcTemplate()`方法获得`JdbcTemplate`对象

### 一、xml形式配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!--配置数据源-->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai&amp;useSSL=false"/>
        <property name="username" value="root"/>
        <property name="password" value="456852"/>
    </bean>
    <!--注入dao对象-->
    <bean class="com.ajacker.dao.impl.AccountDaoImpl" id="accountDao">
        <property name="dataSource" ref="dataSource"/>
    </bean>
</beans>
```

### 二、半注解形式配置

- 配置数据源和注解扫描支持

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">
        <context:component-scan base-package="com.ajacker"/>

        <!--配置数据源-->
        <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
            <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
            <property name="url" value="jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai&amp;useSSL=false"/>
            <property name="username" value="root"/>
            <property name="password" value="456852"/>
        </bean>
    </beans>
    ```

- 注解持久层实现类，重写构造函数，使用注解注入参数调用父类的`setDataSource()`方法注入：

  ```java
  /**
   * @author ajacker
   * 持久层实现类
   */
  @Repository("dataSource")
  public class AccountDaoImpl extends JdbcDaoSupport implements IAccountDao {
  	/**
  	 * 使用dataSource注入此参数，调用父类的set方法设置数据源
  	 */
      @Autowired
      public AccountDaoImpl(DataSource dataSource) {
          setDataSource(dataSource);
      }
  
      @Override
      public Account findAccountById(int id) {
          List<Account> accounts = getJdbcTemplate().query("select * from account where id = ?", new BeanPropertyRowMapper<>(Account.class), id);
          return accounts.isEmpty()?null:accounts.get(0);
      }
  
      @Override
      public Account findAccountByName(String name) {
          List<Account> accounts = getJdbcTemplate().query("select * from account where name = ?", new BeanPropertyRowMapper<>(Account.class), name);
          if (accounts.isEmpty()) {
              return null;
          }
          if(accounts.size()>1){
              throw new RuntimeException("结果不止一个");
          }
          return accounts.get(0);
      }
  
      @Override
      public void updateAccount(Account account) {
          getJdbcTemplate().update("update account set name=?,money=? where id=?", account.getName(), account.getMoney(), account.getId());
      }
  }
  
  ```

# 第四部分 事务控制

## A. 使用AOP完成事务控制的例子（AOPTxTest）

我们基于之前的例子（AOPTest），用现在所学的知识完成改造

### 一、配置Xml

开启注解扫描和aop注解支持并使用`spring-jdbc`配置数据源

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/aop https://www.springframework.org/schema/aop/spring-aop.xsd">

    <!--配置注解扫描包-->
    <context:component-scan base-package="com.ajacker"/>
    <!--配置开启aop注解支持-->
    <aop:aspectj-autoproxy proxy-target-class="true"/>
    <!--配置QueryRunner对象-->
    <bean id="queryRunner" class="org.apache.commons.dbutils.QueryRunner" scope="prototype"/>
    <!--配置数据源-->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <!--连接数据库的必要信息-->
        <property name="driverClass" value="com.mysql.cj.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai"/>
        <property name="user" value="root"/>
        <property name="password" value="456852"/>
    </bean>
</beans>
```

### 二、改写TransactionManger

#### 1. 配置切面

```java
@Component
@Aspect
public class TransactionManager {..}
```

#### 2. 添加切入点

```java
/**
 * 配置切入点
 */
@Pointcut("execution(* *..AccountServiceImpl.*(..))")
private void pt({};
```

#### 3. 配置环绕通知

```java
@Around("pt()")
public Object aroundAdvice(ProceedingJoinPoint pjp){
    Object rtValue;
    try {
        //1.获得参数
        Object[] args = pjp.getArgs();
        //2.开启事务
        this.beginTransaction();
        //3.执行方法
        rtValue = pjp.proceed(args);
        //4.提交事务
        this.commit();
        //返回结果
        return rtValue;
    }catch (Throwable e){
        //5.回滚事务
        this.rollback();
        throw new RuntimeException(e);
    }finally {
        //6.释放资源
        this.release();
    }
}
```

## B. Spring事务控制支持

- JavaEE 体系进行分层开发,事务处理位于业务层,Spring提供了分层设计业务层的事务处理解决方
  案
- Spring 框架为我们提供了一组事务控制的接口,这组接口在spring-tx-5.0.2.RELEASE.jar中
- Spring 的事务控制都是基于AOP的,它既可以使用配置的方式实现,也可以使用编程的方式实现.推荐使用配置方式实现

### 一、基础知识

#### 1. 事务的四大特性（ACID）

- 原子性(Atomicity): 事务包含的所有操作要么全部成功,要么全部失败回滚;成功必须要完全应用到数据库,失败则不能对数据库产生影响.
- 一致性(Consistency):事务执行前和执行后必须处于一致性状态.例如:转账事务执行前后,两账户余额的总和不变.
- 隔离性(Isolation): 多个并发的事务之间要相互隔离.
- 持久性(Durability): 事务一旦提交,对数据库的改变是永久性的

#### 2. 事务的隔离级别

- `ISOLATION_READ_UNCOMMITTED`: 读未提交.事务中的修改,即使没有提交,其他事务也可以看得到.会导致脏读,不可重复读,幻读.
- `ISOLATION_READ_COMMITTED`: 读已提交(Oracle数据库默认隔离级别).一个事务不会读到其它并行事务已修改但未提交的数据.避免了脏读,但会导致不可重复读,幻读.
- `ISOLATION_REPEATABLE_READ`: 可重复读(Mysql数据库默认的隔离级别).一个事务不会读到其它并行事务已修改且已提交的数据,(只有当该事务提交之后才会看到其他事务提交的修改).避免了脏读,不可重复读,但会导致幻读.
- `ISOLATION_SERIALIZABLE`: 串行化.事务串行执行,一个时刻只能有一个事务被执行.避免了脏读,不可重复读,幻读.

---

可以通过下面的例子理解事务的隔离级别: 

| 事务A                             | 事务B       |
| --------------------------------- | ----------- |
| 启动事务 查询得到原始值`origin`=1 |             |
|                                   | 启动事务    |
|                                   | 查询得到值1 |
|                                   | 将1改成2    |
| 查询得到值`value1`                |             |
|                                   | 提交事务B   |
| 查询得到值`value2`                |             |
| 提交事务A                         |             |
| 查询得到值`value3`                |             |

对不同的事务隔离级别,事务A三次查询结果分别如下:

| 事务隔离级别               | 原始值`origin` | value1  | **value2**    | **value3** |
| -------------------------- | -------------- | ------- | ------------- | ---------- |
| ISOLATION_READ_UNCOMMITTED | 1              | 2(脏读) | 2             | 2          |
| ISOLATION_READ_COMMITTED   | 1              | 1       | 2(不可重复读) | 2          |
| ISOLATION_REPEATABLE_READ  | 1              | 1       | 1             | 2          |
| ISOLATION_SERIALIZABLE     | 1              | 1       | 1             | 1          |

#### 3. 事务的安全隐患

1. `脏读`: 一个事务读到另外一个事务还未提交(可能被回滚)的脏数据.
2. `不可重复读`: 一个事务执行期间另一事务提交修改,导致第一个事务前后两次查询结果不一致.
3. `幻读`: 一个事务执行期间另一事务提交添加数据,导致第一个事务前后两次查询结果到的数据条数不同.

| **脏读**                   | 脏读 | 不可重复读 | `幻读` |
| -------------------------- | ---- | ---------- | ------ |
| ISOLATION_READ_UNCOMMITTED | ✓    | ✓          | ✓      |
| ISOLATION_READ_COMMITTED   | ✕    | ✓          | ✓      |
| ISOLATION_REPEATABLE_READ  | ✕    | ✕          | ✓      |
| ISOLATION_SERIALIZABLE     | ✕    | ✕          | ✕      |

### 二、事务控制的API

- `PlatformTransactionManager`接口是`Spring`提供的事务管理器,它提供了操作事务的方法如下:

  - `TransactionStatus getTransaction(TransactionDefinition definition)`: 获得事务状态信息
  - `void commit(TransactionStatus status)`: 提交事务
  - `void rollback(TransactionStatus status)`: 回滚事务

  在实际开发中我们使用其实现类:

  - `org.springframework.jdbc.datasource.DataSourceTransactionManager`使用SpringJDBC或iBatis进行持久化数据时使用
  - `org.springframework.orm.hibernate5.HibernateTransactionManager`使用Hibernate版本进行持久化数据时使用

- `TransactionDefinition`: 事务定义信息对象,提供查询事务定义的方法如下:

  - `String getName()`: 获取事务对象名称

  - `int getIsolationLevel()`: 获取事务隔离级别,设置两个事务之间的数据可见性

    事务的隔离级别由弱到强,依次有如下五种:(可以参考文章事务的四种隔离级别,数据库事务4种隔离级别及7种传播行为)

    - `ISOLATION_DEFAULT`: Spring事务管理的的默认级别,使用数据库默认的事务隔离级别.
    - `ISOLATION_READ_UNCOMMITTED`: 读未提交.
    - `ISOLATION_READ_COMMITTED`: 读已提交.
    - `ISOLATION_REPEATABLE_READ`: 可重复读.
    - `ISOLATION_SERIALIZABLE`: 串行化.

  - `getPropagationBehavior()`: 获取事务传播行为,设置新事务是否事务以及是否使用当前事务.

    我们通常使用的是前两种: `REQUIRED`和`SUPPORTS`.事务传播行为如下:

    - `REQUIRED`: Spring默认事务传播行为. 若当前没有事务,就新建一个事务;若当前已经存在一个事务中,加入到这个事务中.增删改查操作均可用
    - `SUPPORTS`: 若当前没有事务,就不使用事务;若当前已经存在一个事务中,加入到这个事务中.查询操作可用
    -` MANDATORY`: 使用当前的事务,若当前没有事务,就抛出异常
    - `REQUERS_NEW`: 新建事务,若当前在事务中,把当前事务挂起
    - `NOT_SUPPORTED`: 以非事务方式执行操作,若当前存在事务,就把当前事务挂起
    - `NEVER`:以非事务方式运行,若当前存在事务,抛出异常
    - `NESTED`:若当前存在事务,则在嵌套事务内执行;若当前没有事务,则执行`REQUIRED`类似的操作

  - `int getTimeout()`: 获取事务超时时间. Spring默认设置事务的超时时间为-1,表示永不超时.

  - `boolean isReadOnly()`: 获取事务是否只读. Spring默认设置为false,建议查询操作中设置为true

- `TransactionStatus`: 事务状态信息对象,提供操作事务状态的方法如下: 
  - `void flush()`: 刷新事务
  - `boolean hasSavepoint()`: 查询是否存在存储点
  - `boolean isCompleted()`: 查询事务是否完成
  - `boolean isNewTransaction()`: 查询是否是新事务
  - `boolean isRollbackOnly()`: 查询事务是否回滚
  - `void setRollbackOnly()`: 设置事务回滚 

## C. 使用Spring进行事务控制

### 一、纯注解配置（XmlTXTest）

#### 1. 配置事务管理器

```xml
<!--配置事务管理器-->
<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <!--注入数据源-->
    <property name="dataSource" ref="dataSource" />
</bean>
```

#### 2. 配置事务通知和属性

- 使用`<tx:advice>`标签声明事务配置,其属性如下:
  - `id`: 事务配置的id
  - `transaction-manager`: 该配置对应的事务管理器
- 在`<tx:advice>`标签内包含`<tx:attributes>`标签表示配置事务属性

- 在`<tx:attributes>`标签内包含`<tx:method>`标签,为切面上的方法配置事务属性,`<tx:method>`标签的属性如下:
  - `name`: 拦截到的方法,可以使用通配符*
  - `isolation`: 事务的隔离级别,Spring默认使用数据库的事务隔离级别
  - `propagation`: 事务的传播行为,默认为REQUIRED.增删改方法应该用REQUIRED,查询方法可以使用SUPPORTS
  - `read-only`: 事务是否为只读事务,默认为false.增删改方法应该用false,查询方法可以使用true
  - `timeout`: 指定超时时间,默认值为-1,表示永不超时
  - `rollback-for`: 指定一个异常,当发生该异常时,事务回滚;发生其他异常时,事务不回滚.无默认值,表示发生任何异常都回滚
  - `no-rollback-for`: 指定一个异常,当发生该异常时,事务不回滚;发生其他异常时,事务回滚.无默认值,表示发生任何异常都回滚

```xml
<!--配置事务通知-->
<tx:advice id="txAdvice" transaction-manager="transactionManager">
    <tx:attributes>
        <tx:method name="*" propagation="REQUIRED" read-only="false"/>
        <tx:method name="find*" propagation="SUPPORTS" read-only="true"/>
    </tx:attributes>
</tx:advice>
```

#### 3. 配置aop

```xml
<!--配置aop-->
<aop:config>
    <!--配置切入点-->
    <aop:pointcut id="pt" expression="execution(* com.ajacker.service.impl.*.*(..))"/>
    <!--配置切入点表达式和通知的关系-->
    <aop:advisor advice-ref="txAdvice" pointcut-ref="pt"/>
</aop:config>
```

### 二、半注解配置（HalfXmlTXTest）

#### 1.  配置事务管理器

```xml
<!--配置事务管理器-->
<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <!--注入数据源-->
    <property name="dataSource" ref="dataSource" />
</bean>
```

#### 2. 开启注解事务支持

```xml
<!--开启事务注解支持-->
<tx:annotation-driven transaction-manager="transactionManager"/>
```

#### 3. 在业务层注解

```java
/**
 * @author ajacker
 * 业务层实现类
 */
@Service("accountService")
@Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
public class AccountServiceImpl implements IAccountService {
    private final IAccountDao accountDao;

    public AccountServiceImpl(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account findAccountById(Integer accountId) {
        return accountDao.findAccountById(accountId);
    }

    @Transactional(propagation = Propagation.REQUIRED,readOnly = false)
    public void transfer(String sourceName, String targetName, Float money) {
        System.out.println("start transfer");
        // 1.根据名称查询转出账户
        Account source = accountDao.findAccountByName(sourceName);
        // 2.根据名称查询转入账户
        Account target = accountDao.findAccountByName(targetName);
        // 3.转出账户减钱
        source.setMoney(source.getMoney() - money);
        // 4.转入账户加钱
        target.setMoney(target.getMoney() + money);
        // 5.更新转出账户
        accountDao.updateAccount(source);
        // 6.更新转入账户
        accountDao.updateAccount(target);
    }
}
```

