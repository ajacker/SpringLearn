---
typora-copy-images-to: spring笔记.assets
---

# 1.IOC控制反转

## 一、程序的耦合和解耦

- **耦合**: 程序间的依赖关系.在开发中,应该做到解决**编译期依赖**,即**编译期不依赖,运行时才依赖**.
- **解耦的思路**: 使用**反射来创建对**象,而**避免使用new关键字**,并**通过读取配置文件来获取要创建的对象全限定类名.**

### 解耦例子：JDBC驱动

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

### 解耦例子：工厂模式 三层架构

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



## 二、使用Spring解决程序耦合

### 准备工作

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

### 修改表现层代码，通过spring创建对象

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

## 三、使用XML配置文件实现IOC详解

### ApplicationContext方式

- 特点：
  - 读取完配置文件，**立马创建对象等待使用**
  - 单例对象使用
    - **我们实际开发中使用这个，因为加载策略会随配置文件改变**

#### ApplicationContext三个常用实现类

![1570379339445](spring笔记.assets/1570379339445.png)

`ClassPathXmlApplicationContext,FileSystemXmlApplicationContext,AnnotationConfigApplicationContext`.

- `ClassPathXmlApplicationContext`: 它是从类的根路径下加载配置文件
- `FileSystemXmlApplicationContext`: 它是从磁盘路径上加载配置文件
- `AnnotationConfigApplicationContext`: 读取注解创建容器

### BeanFactory方式

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

### 使用XML配置文件实现IOC

#### bean标签

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

#### bean作用范围

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

#### 实例化bean的三种方式（模块“threeway”）

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

# 二、DI依赖注入

