package com.example.imexport;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Import/Export Tool Application
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.example.imexport.mapper")
public class ImportExportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImportExportApplication.class, args);
    }
}
