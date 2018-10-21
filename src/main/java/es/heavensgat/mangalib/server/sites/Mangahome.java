package es.heavensgat.mangalib.server.sites;

import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.Page;
import es.heavensgat.mangalib.server.util.MangaUtil;
import es.heavensgat.mangalib.server.util.SiteInterface;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mangahome implements SiteInterface {
    public Manga getBaseMangaInfo(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            Manga manga = new Manga();

            manga.setBaseURL(url);
            manga.setTitle(doc.select("div.manga-detail > h1").first().text());
            Elements authors = doc.select("div.manga-detail a[href^=/author/]");
            Elements artists = doc.select("div.manga-detail a[href^=/artist/]");
            manga.setSummary(doc.select("div.manga-detailmiddle > p.mobile-none").first().text());
            manga.setAuthor(getPersonIfExists(authors));
            manga.setArtist(getPersonIfExists(artists));
            manga.setCoverImage(getImage(doc.select("img.detail-cover").first().attr("src")));
            return manga;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPersonIfExists(Elements elements){
        if(elements.size() > 0){
            return elements.first().text();
        }
        return "N/A";
    }

    public List<Chapter> getChapters(String baseUrl) {
        try{
            Connection connection = Jsoup.connect(baseUrl);
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            Elements chapterLis = doc.select(".detail-chlist > li");
            Collections.reverse(chapterLis);
            List<Chapter> chapters = new ArrayList<>();
            for(Element li : chapterLis.subList(0, MangaUtil.MAX_CHAPTERS)){
                Chapter chapter = new Chapter();
                chapter.setTitle(li.select("span.mobile-none").first().text());
                chapter.setFirstPageURL(li.select("a[href]").first().attr("href").replaceFirst("//www.", "http://"));
                chapter.setPages(getPages(chapter));
                chapters.add(chapter);
                System.out.println(chapter.getTitle());
            }
            return chapters;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Page> getPages(Chapter chapter){
        List pages = new ArrayList();
        try{
            Connection connection = Jsoup.connect(chapter.getFirstPageURL());
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            Elements pageElements = doc.select("div.mangaread-pagenav").first().select("option:not(:contains(featured))");
            for(Element currentPage : pageElements){
                Page page = new Page();
                page.setPageNumber(pageElements.indexOf(currentPage) + 1);
                page.setUrl(currentPage.attr("value").replace("//www.", "http://"));
                page.setParentChapter(chapter);
                pages.add(page);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pages;
    }

    public void downloadImages(Chapter emptyChapter) {
        for(Page page : emptyChapter.getPages()) {
            System.out.println("downloading: " + emptyChapter.getTitle() + " - " + page.getPageNumber());
            try{
                Connection connection = Jsoup.connect(page.getUrl());
                connection.userAgent("Chrome/69.0.3497.100");

                Document doc = connection.get();
                page.setImage(getImage(doc.select("img#image").first().attr("src")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Image getImage(String url) throws IOException{
        return ImageIO.read(new URL(url));
    }
}