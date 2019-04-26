package es.heavensgat.mangalib.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by bened_000 on 03.11.2018.
 */
@SpringBootApplication
@EnableScheduling
public class MainSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainSpringApplication.class, args);
    }

}
