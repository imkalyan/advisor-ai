
package com.advisor;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AdvisorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdvisorApplication.class, args);
    }
    @Bean
    CommandLineRunner repair(Flyway flyway) {
        return args -> {
            flyway.repair();
        };
    }
}


