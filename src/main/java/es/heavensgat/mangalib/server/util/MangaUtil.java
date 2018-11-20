package es.heavensgat.mangalib.server.util;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.Page;
import es.heavensgat.mangalib.server.sites.Mangahere;
import es.heavensgat.mangalib.server.sites.Mangahome;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MangaUtil {
    public static long time = System.currentTimeMillis();
    public static int MAX_CHAPTERS = -1;
//    public static String BASE_DIRECTORY = "C:/Users/bened_000/Pictures/Mangalib";
    public static String BASE_DIRECTORY = "/opt/tomcat";

    public static Manga crawlCompleteManga(String url) {
        return crawlUntilChapter(url, MAX_CHAPTERS);
    }

    public static Manga crawlUntilChapter(String url, int chapterAmount){
        SiteInterface site;
        if (url.contains("mangahome")){
            site = new Mangahome();
        }else if (url.contains("mangahere")){
            site = new Mangahere();
        }else{
            throw new SiteNotSupportedException("Provided manga website is not supported");
        }

        log("--- Getting base manga info");
        Manga manga = site.getBaseMangaInfo(url);

        log("--- Getting chapter info");
        List<Chapter> chapters = site.getChapters(manga, chapterAmount);
        manga.setChapters(chapters);

        log("--- Getting Images");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int max = 0;
        AtomicInteger current = new AtomicInteger(0);
        for(Chapter chapter : chapterAmount == -1 ? chapters : chapters.subList(0, chapterAmount)) {
            max += chapter.getPages().size();
        }
        System.out.print("0/" + max);
        for(Chapter chapter : chapterAmount == -1 ? chapters : chapters.subList(0, chapterAmount)){
            downloadImages(chapter, site, executorService, max, current);
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log("\n--- Generating PDF");
        generatePDF(manga, chapterAmount);
        return manga;
    }


    private static void addImagePage(Page page, com.itextpdf.text.Document document) throws Exception {
        com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(page.getImageFilePath());
        document.setPageSize(img);
        document.newPage();
        img.setAbsolutePosition(0, 0);
        document.add(img);
    }

    private static void addChapterCover(String mangaTitle, String chapterTitle, com.itextpdf.text.Document document) throws DocumentException {
        Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
        Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        Chunk chunk = new Chunk(chapterTitle, chapterFont);
        Paragraph paragraphManga = new Paragraph(mangaTitle, paragraphFont);
        Paragraph paragraphChapter = new Paragraph(chunk);
        paragraphChapter.setAlignment(Element.ALIGN_CENTER);
        paragraphManga.setAlignment(Element.ALIGN_CENTER);
        com.itextpdf.text.Chapter chapter = new com.itextpdf.text.Chapter(paragraphChapter, 1);
        chapter.setNumberDepth(0);
        chapter.add(paragraphManga);
        document.add(chapter);
    }

    private static void generatePDF(Manga manga, int chapterAmount) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(MangaUtil.BASE_DIRECTORY + "/mangas/" + manga.getTitle() + "-complete.pdf"));
            document.open();
            for (Chapter chapter : manga.getChapters()) {
                addChapterCover(manga.getTitle(), chapter.getTitle(), document);
                for (Page page : chapter.getPages()) {
                    addImagePage(page, document);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            document.close();
            log("--- DONE");
        }
    }


    public static void downloadImages(Chapter emptyChapter, SiteInterface site, ExecutorService executorService, int max, AtomicInteger current) {
        for(Page page : emptyChapter.getPages()) {
            String path = MangaUtil.BASE_DIRECTORY + "/mangas/" + emptyChapter.getManga().getTitle() + "/" + emptyChapter.getTitle() + "/";
            String imagePath = path + page.getPageNumber() + ".jpg";
            imagePath = imagePath.replace(' ', '_');
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                page.setImageFilePath(imagePath);
                System.out.print("\r" + current.incrementAndGet() + "/" + max);
            } catch(Exception e){
                String finalImagePath = imagePath;
                executorService.submit(() -> {
                    Image image = getImage(site, page, finalImagePath);
                    for(int i = 0; i<300 && image == null; i++){
                        log("download image failed, retrying...");
                        try {
                            Thread. sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        image = getImage(site, page, finalImagePath);
                    }
                    page.setImageFilePath(finalImagePath);
                    System.out.print("\r" + current.incrementAndGet() + "/" + max);
                });
            }
        }
    }

    public static Image getImage(SiteInterface site, Page page, String imagePath){
        try{
            Connection connection = Jsoup.connect(page.getUrl());
            connection.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

            Document doc = connection.get();
            BufferedImage image = (BufferedImage)downloadImage(site.getImageUrl(page));
            File outputFile = new File(imagePath);
            outputFile.mkdirs();
            ImageIO.write(image, "jpg", outputFile);
            return image;
        } catch(SocketTimeoutException se) {
            log("error1");
            se.printStackTrace();
        }catch(IOException ie) {
            log("error2");
            ie.printStackTrace();
        }catch(Exception e) {
            log("error3");

        }
        return null;
    }


    public static Image downloadImage(String url) throws IOException{
        final URL urlObj = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) urlObj
                .openConnection();
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
        InputStream inputStream = connection.getInputStream();
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        return bufferedImage;
    }

    private static void log(String message) {
        long currentTime = System.currentTimeMillis();
        System.out.println("[" + (currentTime - time) + "] " + message);
        time = currentTime;

    }
}
