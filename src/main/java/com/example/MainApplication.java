package com.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

import java.io.IOException;

@SpringBootApplication
public class MainApplication {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    public static void main(String[] args) throws IOException {

        SpringApplication.run(MainApplication.class,args);
        logger.info("App is Running in " + AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + " mode");
    }
}