package es.heavensgat.mangalib.server.util;

import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.Page;

import java.util.List;

public interface SiteInterface {

    Manga getBaseMangaInfo(String url, Manga manga);

    /**
     * Returns a List of Chapters
     *
     * returns List<Chapter>
     * @param manga
     */
    List<Chapter> getChapters(Manga manga);

    /**
     * Crawls through a chapter filling it with pages
     *
     * @return chapter containing all Pages
     */
    String getImageUrl(Page page);
}
