package com.byzk.serial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SerialApplication {

    public static void main(String[] args) {

        log.info("当前版本为:{}","v1_23_07_27_01");
        SpringApplication.run(SerialApplication.class, args);
    }

}
