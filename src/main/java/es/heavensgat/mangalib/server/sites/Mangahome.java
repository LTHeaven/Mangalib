package es.heavensgat.mangalib.server.sites;

import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.Page;
import es.heavensgat.mangalib.server.service.MangaService;
import es.heavensgat.mangalib.server.service.MangaServiceImpl;
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
public class Mangahome implements SiteInterface {
    @Autowired
    private MangaService mangaService;

    public Manga getBaseMangaInfo(String url, Manga manga) {
        try {
            Connection connection = Jsoup.connect(url);
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();

            manga.setBaseURL(url);
            manga.setTitle(doc.select("div.manga-detail > h1").first().text());
            Elements authors = doc.select("div.manga-detail a[href^=/author/]");
            Elements artists = doc.select("div.manga-detail a[href^=/artist/]");
            manga.setSummary(doc.select("div.manga-detailmiddle > p.mobile-none").first().text());
            manga.setAuthor(getPersonIfExists(authors));
            manga.setArtist(getPersonIfExists(artists));

            String folderPath = MangaServiceImpl.BASE_DIRECTORY + "/mangas/" + URLEncoder.encode(manga.getTitle(), "UTF-8");
            manga.setMangaFolderPath(folderPath);
            File outputFile = new File(folderPath + "/cover.jpg");
            outputFile.mkdirs();
            ImageIO.write((BufferedImage) mangaService.downloadImage(doc.select("img.detail-cover").first().attr("src")), "jpg", outputFile);
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

    public List<Chapter> getChapters(Manga manga) {
        try{
            Connection connection = Jsoup.connect(manga.getBaseURL());
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            Elements chapterLis = doc.select(".detail-chlist > li");
            Collections.reverse(chapterLis);
            List<Chapter> chapters = new ArrayList<>();
            for(int i = 0; i < chapterLis.size(); i++){
                Element li = chapterLis.get(i);
                Chapter chapter = new Chapter();
                chapter.setTitle((chapterLis.indexOf(li)+1) + "-" + li.select("span.mobile-none").first().text());
                chapter.setFirstPageURL(li.select("a[href]").first().attr("href").replaceFirst("//www.", "http://"));
                chapter.setPages(getPages(chapter));
                chapter.setManga(manga);
                chapters.add(chapter);
                System.out.println(chapter.getTitle());
                mangaService.setProgress(manga, 1.*i/chapterLis.size());
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

    public String getImageUrl(Page page){
        try{
            Connection connection = Jsoup.connect(page.getUrl());
            connection.userAgent("Chrome/69.0.3497.100");

            Document doc = connection.get();
            return doc.select("img#image").first().attr("src");
        }catch(IOException ie) {
            ie.printStackTrace();
        }
        return null;
    }

}
