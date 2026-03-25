package com.mysunriser.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
//import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
//import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
@MapperScan("com.mysunriser.backend.Dao")
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}