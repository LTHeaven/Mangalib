package es.heavensgat.mangalib.server.controllers;

import es.heavensgat.mangalib.server.models.MangaListingDTO;
import es.heavensgat.mangalib.server.util.MangaUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @RequestMapping("/mangas")
    public MangaListingDTO listMangas(){
        return MangaUtil.getMangas();
    }

    @PostMapping("/mangas")
    public void crawlManga(@RequestBody String mangaUrl) {
        String url = mangaUrl;
        if(mangaUrl.contains("%2f") || mangaUrl.contains("%2F")){
            url = URLDecoder.decode(mangaUrl.contains("=&") ? mangaUrl.split("=&")[0] : mangaUrl);
        }
        MangaUtil.crawlCompleteManga(url);
    }
}
