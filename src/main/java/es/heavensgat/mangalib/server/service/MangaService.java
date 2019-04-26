package es.heavensgat.mangalib.server.service;

import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.MangaListingDTO;

import java.awt.*;
import java.io.IOException;

public interface MangaService {
    Image downloadImage(String src) throws IOException;

    Manga crawlCompleteManga(String url);

    MangaListingDTO getMangas();

    void setProgress(Manga manga, double progress);

    void checkForUpdates();

    void removeUpdated(String mangaFolderPath);
}
