package es.heavensgat.mangalib.server;

import es.heavensgat.mangalib.server.controllers.MainController;
import es.heavensgat.mangalib.server.service.MangaService;
import org.springframework.beans.factory.annotation.Autowired;
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
