package es.heavensgat.mangalib.server.util;

import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.models.Manga;
import es.heavensgat.mangalib.server.models.Page;
import es.heavensgat.mangalib.server.sites.Mangahere;
import es.heavensgat.mangalib.server.sites.Mangahome;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
        ExecutorService executorService = Executors.newFixedThreadPool(5);
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

    private static void addImagePage(BufferedImage bimg, PDDocument document) throws IOException{
        if(bimg == null){
            log("replacing image");
            BufferedImage read = ImageIO.read(MangaUtil.class.getResource("/images/missing-page.jpg"));
            bimg = read;
        }
        float width = bimg.getWidth(null);
        float height = bimg.getHeight(null);
        PDPage pdfPage = new PDPage(new PDRectangle(width, height));
        document.addPage(pdfPage);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bimg, "jpg", byteArrayOutputStream);
        PDImageXObject img = PDImageXObject.createFromByteArray(document, byteArrayOutputStream.toByteArray(),"test");
        PDPageContentStream contentStream = new PDPageContentStream(document, pdfPage);
        contentStream.drawImage(img, 0, 0);
        contentStream.close();
        byteArrayOutputStream.close();
    }

    private static void generatePDF(Manga manga, int chapterAmount) {
        try{
            String path = MangaUtil.BASE_DIRECTORY + "/mangas/" + manga.getTitle() + "/";
            path = path.replace(' ', '_');
            if(chapterAmount == -1){
                PDDocument document = new PDDocument();
                PDDocumentOutline outline = new PDDocumentOutline();
                document.getDocumentCatalog().setDocumentOutline( outline );
                PDOutlineItem root = new PDOutlineItem();
                root.setTitle(manga.getTitle());
                outline.addLast(root);

                addImagePage((BufferedImage) manga.getCoverImage(), document);
                for(Chapter chapter : manga.getChapters()){
                    addChapterCover(manga.getTitle(), chapter.getTitle(), document, root);
                    for(Page page : chapter.getPages()){
                        addImagePage(ImageIO.read(new File(page.getImageFilePath())), document);
                    }
                }

                path += manga.getTitle() + "-complete.pdf";
                document.save(path);
                document.save(MangaUtil.BASE_DIRECTORY + "/mangas/" + manga.getTitle() + "-complete.pdf");
                document.close();
            }else{
                for(Chapter chapter : manga.getChapters()){
                    PDDocument document = new PDDocument();
                    addChapterCover(manga.getTitle(), chapter.getTitle(), document, null);
                    for(Page page : chapter.getPages()){
                        addImagePage(ImageIO.read(new File(page.getImageFilePath())), document);
                    }
                    String chapterPath = path + chapter.getTitle().replace(' ', '_') + ".pdf";
                    document.save(chapterPath);
                    document.save(MangaUtil.BASE_DIRECTORY + "/mangas/" + chapter.getTitle().replace(' ', '_') + ".pdf");
                    document.close();
                }

            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void addChapterCover(String mangaTitle, String chapterTitle, PDDocument document, PDOutlineItem root) throws IOException {
        int marginTop = 350; // Or whatever margin you want.
        String title = mangaTitle + " - " + chapterTitle;
        PDPage page = new PDPage();
        document.addPage(page);
        if(root != null){
            PDOutlineItem firstPageItem = new PDOutlineItem();
            firstPageItem.setTitle( chapterTitle );
            firstPageItem.setDestination(page);
            root.addLast( firstPageItem );
        }
        PDPageContentStream stream = new PDPageContentStream(document, page);
        PDFont font = PDType1Font.HELVETICA_BOLD; // Or whatever font you want.

        int fontSize = 16; // Or whatever font size you want.
        float titleWidth = font.getStringWidth(title) / 1000 * fontSize;
        float titleHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        stream.beginText();
        stream.setFont(font, fontSize);
        stream.moveTextPositionByAmount((page.getMediaBox().getWidth() - titleWidth) / 2, page.getMediaBox().getHeight() - marginTop - titleHeight);
        stream.drawString(title);
        stream.endText();
        stream.close();
    }


    public static void downloadImages(Chapter emptyChapter, SiteInterface site, ExecutorService executorService, int max, AtomicInteger current) {
        for(Page page : emptyChapter.getPages()) {
            String path = MangaUtil.BASE_DIRECTORY + "/mangas/" + emptyChapter.getManga().getTitle() + "/" + emptyChapter.getTitle() + "/";
            String imagePath = path + page.getPageNumber() + ".jpg";
            imagePath = imagePath.replace(' ', '_');
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
//                page.setImage(image);
                System.out.print("\r" + current.incrementAndGet() + "/" + max);
            } catch(Exception e){
                String finalImagePath = imagePath;
//                executorService.submit(() -> {
                    log("thread submitted");
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
//                    page.setImage(image);
                    page.setImageFilePath(finalImagePath);
                    System.out.print("\r" + current.incrementAndGet() + "/" + max);
//                });
            }
        }
    }

    public static Image getImage(SiteInterface site, Page page, String imagePath){
        try{
            Connection connection = Jsoup.connect(page.getUrl());
            connection.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

            Document doc = connection.get();
            log("before download");
            BufferedImage image = (BufferedImage)downloadImage(site.getImageUrl(page));
            log("after download");
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
        log("download connection open before");
        final HttpURLConnection connection = (HttpURLConnection) urlObj
                .openConnection();
        log("download connection open after");
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
        log("before inputStream");
        InputStream inputStream = connection.getInputStream();
        log("before read");
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        log("after read\n\n");
        return bufferedImage;
    }

    private static void log(String message) {
        long currentTime = System.currentTimeMillis();
        System.out.println("[" + (currentTime - time) + "] " + message);
        time = currentTime;

    }
}
