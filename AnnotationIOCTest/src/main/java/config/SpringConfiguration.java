package config;

import org.springframework.context.annotation.*;

/**
 * @author ajacker
 * 此类是配置类，和bean.xml作用一样
 */
@Configuration
@ComponentScan(basePackages = "com.ajacker")
@Import(JdbcConfig.class)
public class SpringConfiguration {


}
