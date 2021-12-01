package net.lianbian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("net.lianbian.mapper")
public class lianbianBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(lianbianBootApplication.class);
    }
}
