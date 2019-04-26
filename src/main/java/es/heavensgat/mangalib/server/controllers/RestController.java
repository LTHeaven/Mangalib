package es.heavensgat.mangalib.server.controllers;

import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.MangaListingDTO;
import es.heavensgat.mangalib.server.service.MangaService;
import es.heavensgat.mangalib.server.service.MangaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;
import java.util.List;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @Autowired
    private MangaService mangaService;

    @RequestMapping("/mangas")
    public MangaListingDTO listMangas(){
        return mangaService.getMangas();
    }

    @RequestMapping("checkForUpdates")
    public void checkForUpdates(){
        mangaService.checkForUpdates();
    }

    @PostMapping("/mangas")
    public void crawlManga(@RequestBody String mangaUrl) {
        String url = mangaUrl;
        if(mangaUrl.contains("%2f") || mangaUrl.contains("%2F")){
            url = URLDecoder.decode(mangaUrl.contains("=&") ? mangaUrl.split("=&")[0] : mangaUrl);
        }
        mangaService.crawlCompleteManga(url);
    }
}
