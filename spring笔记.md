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

