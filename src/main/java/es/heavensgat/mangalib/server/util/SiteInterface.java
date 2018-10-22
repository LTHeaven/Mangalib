package es.heavensgat.mangalib.server.util;

import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.Page;

import java.awt.image.BufferedImage;
import java.util.List;

public interface SiteInterface {

    Manga getBaseMangaInfo(String url);

    /**
     * Returns a List of Chapters
     *
     * returns List<Chapter>
     * @param manga
     */
    List<Chapter> getChapters(Manga manga, int chapterAmount);

    /**
     * Crawls through a chapter filling it with pages
     *
     * @return chapter containing all Pages
     */
    BufferedImage getImage(Page page, String ImagePath);
}
