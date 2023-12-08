package com.heima.user;


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.heima.user.mapper")
@Slf4j
//@EnableTransactionManagement //开启注解方式的事务管理
//@EnableCaching  //开启缓存注解Spring Cache
//@EnableScheduling  //开启任务调度Srping Task
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
        log.info("用户服务端运行中！heima-leadnews-user server is running!");
    }
}
