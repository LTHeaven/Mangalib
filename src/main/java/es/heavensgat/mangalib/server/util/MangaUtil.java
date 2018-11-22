package es.heavensgat.mangalib.server.util;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import es.heavensgat.mangalib.server.models.*;
import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.sites.Mangahere;
import es.heavensgat.mangalib.server.sites.Mangahome;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
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
    public static List<MangaListing> progressListings = new ArrayList<>();

    public static Manga crawlCompleteManga(String url) {
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
        MangaListing listing = new MangaListing(manga.getTitle());
        listing.setMangaSummary(manga.getSummary());
        listing.setAdded(System.currentTimeMillis());
        listing.setPdfPath("?file_name=" + manga.getTitle() + ".pdf");
        progressListings.add(listing);

        log("--- Getting chapter info", listing);
        List<Chapter> chapters = site.getChapters(manga);
        manga.setChapters(chapters);

        log("--- Getting Images", listing);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int max = 0;
        AtomicInteger current = new AtomicInteger(0);
        for(Chapter chapter : chapters) {
            max += chapter.getPages().size();
        }
        String currentStatus = "0/" + max;
        System.out.print(currentStatus);
        listing.setStatus(currentStatus);
        for(Chapter chapter : chapters){
            downloadImages(chapter, site, executorService, max, current, listing);
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log("\n--- Generating PDF", listing);
        generatePDF(manga);
        progressListings.remove(listing);
        return manga;
    }


    private static void addImagePage(String imagePath, com.itextpdf.text.Document document) throws Exception {
        com.itextpdf.text.Image img;
        try{
            img = com.itextpdf.text.Image.getInstance(imagePath);
        }catch(FileNotFoundException e){
            img = com.itextpdf.text.Image.getInstance(MangaUtil.class.getResource("/images/missing-page.jpg"));
        }
        addImage(img, document);
    }

    private static void addImage(com.itextpdf.text.Image img, com.itextpdf.text.Document document) throws Exception {
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

    private static void generatePDF(Manga manga) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(MangaUtil.BASE_DIRECTORY + "/mangas/"  + manga.getTitle().replace(" ", "_") + "/" + manga.getTitle() + ".pdf"));
            document.open();
            addImagePage(manga.getCoverImage(), document);
            for (Chapter chapter : manga.getChapters()) {
                addChapterCover(manga.getTitle(), chapter.getTitle(), document);
                for (Page page : chapter.getPages()) {
                    addImagePage(page.getImageFilePath(), document);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            document.close();
            log("--- DONE");
        }
    }


    public static void downloadImages(Chapter emptyChapter, SiteInterface site, ExecutorService executorService, int max, AtomicInteger current, MangaListing listing) {
        for(Page page : emptyChapter.getPages()) {
            String path = MangaUtil.BASE_DIRECTORY + "/mangas/" + emptyChapter.getManga().getTitle() + "/" + emptyChapter.getTitle() + "/";
            String imagePath = path + page.getPageNumber() + ".jpg";
            imagePath = imagePath.replace(' ', '_');
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                page.setImageFilePath(imagePath);
                String currentStatus = "\r--- Getting Images " + current.incrementAndGet() + "/" + max;
                System.out.print(currentStatus);
                listing.setStatus(currentStatus);
            } catch(Exception e){
                String finalImagePath = imagePath;
                executorService.submit(() -> {
                    Image image = getImage(site, page, finalImagePath);
                    for(int i = 0; i<3 && image == null; i++){
                        log("download image failed, retrying...");
                        try {
                            Thread. sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        image = getImage(site, page, finalImagePath);
                    }
                    page.setImageFilePath(finalImagePath);
                    String currentStatus = "\r--- Getting Images " + current.incrementAndGet() + "/" + max;
                    System.out.print(currentStatus);
                    listing.setStatus(currentStatus);
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


    public static MangaListingDTO getMangas(){
        MangaListingDTO ret = new MangaListingDTO();
        File root  = new File(MangaUtil.BASE_DIRECTORY + "/mangas/");
        root.mkdirs();
        File[] dirs = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        for(File currentDir : dirs) {
            File[] pdf = currentDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.contains(".pdf");
                }
            });
            for(File file : pdf){
                MangaListing mangaListing = new MangaListing(file.getName().replace(".pdf", ""), "?file_name=" + file.getName());
                mangaListing.setAdded(file.lastModified());
                ret.add(mangaListing);
            }
        }
        ret.add(progressListings);
        ret.order();
        return ret;
    }

    private static void log(String message, MangaListing listing) {
        if(listing != null) {
            listing.setStatus(message);
        }
        log(message);
    }

    private static void log(String message) {
        long currentTime = System.currentTimeMillis();
        System.out.println("[" + (currentTime - time) + "] " + message);
        time = currentTime;

    }
}
