package es.heavensgat.mangalib.server.sites;

import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.Page;
import es.heavensgat.mangalib.server.service.MangaService;
import es.heavensgat.mangalib.server.service.MangaServiceImpl;
import es.heavensgat.mangalib.server.util.MangaException;
import es.heavensgat.mangalib.server.util.SiteInterface;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class Manganelo implements SiteInterface {
    @Autowired
    protected MangaService mangaService;

    public Manga getBaseMangaInfo(String url, Manga manga) {
        try {
            Connection connection = Jsoup.connect(url);
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();

            manga.setBaseURL(url);
            manga.setTitle(doc.select("div.story-info-right h1").first().text());
            Elements authors = doc.select("ul.manga-info-text a[href*=author/]");
            Elements artists = doc.select("ul.manga-info-text a[href*=artist/]");
            manga.setSummary(doc.select("div#panel-story-info-description").first().text());
            manga.setAuthor(getPersonIfExists(authors));
            manga.setArtist(getPersonIfExists(artists));

            String folderPath = MangaServiceImpl.BASE_DIRECTORY + "/mangas/" + URLEncoder.encode(manga.getTitle(), "UTF-8");
            manga.setMangaFolderPath(folderPath);
            File outputFile = new File(folderPath + "/cover.jpg");
            outputFile.mkdirs();
            ImageIO.write((BufferedImage) mangaService.downloadImage(doc.select("span.info-image img").first().attr("src")), "jpg", outputFile);
            return manga;
        } catch (IOException e) {
            throw new MangaException("Error getting base manga info");
        } catch (NullPointerException e) {
            throw new MangaException("Error getting base manga info");
        }
    }

    protected String getPersonIfExists(Elements elements){
        if(elements.size() > 0){
            return "N/A";
        }
        return "N/A";
    }

    public boolean newChaptersFound(Manga manga) {
        try{
            Connection connection = Jsoup.connect(manga.getBaseURL());
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            Elements chapterAs = doc.select("div.chapter-list a");
            return chapterAs.size() > manga.getChapterAmount();
        } catch (IOException e) {
        }catch (NullPointerException e) {
        }
        return false;
    }

    public List<Chapter> getChapters(Manga manga) {
        try{
            Connection connection = Jsoup.connect(manga.getBaseURL());
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            Elements chapterAs = doc.select("ul.row-content-chapter a");
            Collections.reverse(chapterAs);
            List<Chapter> chapters = new ArrayList<>();
            for(int i = 0; i < chapterAs.size(); i++){
                Element a = chapterAs.get(i);
                Chapter chapter = new Chapter();
                chapter.setTitle((chapterAs.indexOf(a)+1) + "-" + a.attr("title"));
                chapter.setFirstPageURL(a.attr("href").replaceFirst("//www.", "http://"));
                chapter.setPages(getPages(chapter));
                chapter.setManga(manga);
                chapters.add(chapter);
                System.out.println(chapter.getTitle());
                mangaService.setProgress(manga, 1.*i/chapterAs.size());
            }
            if (chapters.size() <= 0){
                throw new MangaException("No Chapters found");
            }
            return chapters;
        } catch (IOException e) {
            throw new MangaException("Error getting chapters - IO");
        }catch (NullPointerException e) {
            throw new MangaException("Error getting chapters - NP");
        }
    }

    private List<Page> getPages(Chapter chapter){
        List pages = new ArrayList();
        try{
            Connection connection = Jsoup.connect(chapter.getFirstPageURL());
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            Elements pageElements = doc.select("div.container-chapter-reader img");
            for(Element currentPage : pageElements){
                Page page = new Page();
                page.setPageNumber(pageElements.indexOf(currentPage) + 1);
                page.setUrl(currentPage.attr("src"));
                page.setParentChapter(chapter);
                pages.add(page);
            }
        } catch (IOException e) {
            throw new MangaException("Error getting pages - IO");
        } catch (NullPointerException e) {
            throw new MangaException("Error getting pages - NP");
        }
        return pages;
    }

    public String getImageUrl(Page page){
        try{
            return page.getUrl();
        }catch(NullPointerException e) {
            throw new MangaException("Error getting page image url - NP");
        }
    }

}
