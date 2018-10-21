package es.heavensgat.mangalib.server.util;

import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;

import java.util.List;

public interface SiteInterface {

    public Manga getBaseMangaInfo(String url);

    /**
     * Returns a List of Chapters
     *
     * returns List<Chapter>
     * @param baseUrl
     */
    public List<Chapter> getChapters(String baseUrl);

    /**
     * Crawls through a chapter filling it with pages
     *
     * @param emptyChapter
     * @return chapter containing all Pages
     */
    public void downloadImages(Chapter emptyChapter);
}
