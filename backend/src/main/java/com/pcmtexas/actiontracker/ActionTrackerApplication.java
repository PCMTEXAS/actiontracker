package com.pcmtexas.actiontracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ActionTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActionTrackerApplication.class, args);
    }
}
