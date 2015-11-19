package com.webservice.peglefiles;

import com.webservice.peglefiles.spring.configuration.PogodynkaConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("com.webservice.peglefiles")
@Import(PogodynkaConfiguration.class)
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
