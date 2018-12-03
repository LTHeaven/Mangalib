package es.heavensgat.mangalib.server.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import es.heavensgat.mangalib.server.models.*;
import es.heavensgat.mangalib.server.models.Chapter;
import es.heavensgat.mangalib.server.repository.MangaRepository;
import es.heavensgat.mangalib.server.sites.Mangahere;
import es.heavensgat.mangalib.server.sites.Mangahome;
import es.heavensgat.mangalib.server.util.SiteInterface;
import es.heavensgat.mangalib.server.util.SiteNotSupportedException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MangaServiceImpl implements MangaService{
    public static long time = System.currentTimeMillis();
    public static int MAX_CHAPTERS = -1;
//    public static String BASE_DIRECTORY = "C:/Users/bened_000/Pictures/Mangalib";
    public static String BASE_DIRECTORY = "/opt/tomcat";
    public static int CHAPTER_SPLIT_AMOUNT = 50;

    @Autowired
    private MangaRepository mangaRepository;
    @Autowired
    private Mangahere mangahere;
    @Autowired
    private Mangahome mangahome;


    @Override
    public Manga crawlCompleteManga(String url) {
        SiteInterface site;
        if (url.contains("mangahome")){
            site = mangahome;
        }else if (url.contains("mangahere")){
            site = mangahere;
        }else{
            throw new SiteNotSupportedException("Provided manga website is not supported");
        }

        Manga manga;
        List<Manga> byBaseURL = mangaRepository.findByBaseURL(url);
        if (byBaseURL.size() > 0){
            manga = byBaseURL.get(0);
        }else{
            manga = new Manga();
        }
        manga.setAdded(System.currentTimeMillis());
        manga.setProgress(0);
        log("Getting base manga info", manga);
        manga = site.getBaseMangaInfo(url, manga);

        log("Getting chapter info", manga);
        List<Chapter> chapters = site.getChapters(manga);
        manga.setChapters(chapters);
        manga.setChapterAmount(chapters.size());

        log("Getting Images", manga);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int max = 0;
        AtomicInteger current = new AtomicInteger(0);
        for(Chapter chapter : chapters) {
            max += chapter.getPages().size();
        }
        String currentStatus = "0/" + max;
        System.out.print(currentStatus);
        for(Chapter chapter : chapters){
            downloadImages(chapter, site, executorService, max, current, manga);
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log("\n");
        log("Generating PDF", manga);
        generatePDF(manga);
        log("", manga);
        return manga;
    }


    private void addImagePage(String imagePath, com.itextpdf.text.Document document) throws Exception {
        com.itextpdf.text.Image img;
        try{
            img = com.itextpdf.text.Image.getInstance(imagePath);
        }catch(FileNotFoundException e){
            img = com.itextpdf.text.Image.getInstance(MangaServiceImpl.class.getResource("/images/missing-page.jpg"));
        }
        addImage(img, document);
    }

    private void addImage(com.itextpdf.text.Image img, com.itextpdf.text.Document document) throws Exception {
        document.setPageSize(img);
        document.newPage();
        img.setAbsolutePosition(0, 0);
        document.add(img);
    }

    private void addChapterCover(String mangaTitle, String chapterTitle, com.itextpdf.text.Document document) throws DocumentException {
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

    private void generatePDF(Manga manga) {
        if(manga.getChapters().size()>CHAPTER_SPLIT_AMOUNT){
            for (int i = 0; i*CHAPTER_SPLIT_AMOUNT < manga.getChapters().size(); i++){
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                try {
                    PdfWriter.getInstance(document, new FileOutputStream(manga.getMangaFolderPath() + "/" +  URLEncoder.encode(manga.getTitle(), "UTF-8") + "-" + (i+1) + ".pdf"));
                    document.open();
                    addImagePage(manga.getMangaFolderPath() + "/cover.jpg", document);
                    int lastChapter = i * CHAPTER_SPLIT_AMOUNT + CHAPTER_SPLIT_AMOUNT;
                    for (Chapter chapter : manga.getChapters().subList(i*CHAPTER_SPLIT_AMOUNT, lastChapter>manga.getChapters().size() ? manga.getChapters().size() : lastChapter)) {
                        addChapterCover(manga.getTitle(), chapter.getTitle(), document);
                        for (Page page : chapter.getPages()) {
                            addImagePage(page.getImageFilePath(), document);
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    document.close();
                }
            }
        }else{
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(manga.getMangaFolderPath() + "/" +  URLEncoder.encode(manga.getTitle(), "UTF-8") + ".pdf"));
                document.open();
                addImagePage(manga.getMangaFolderPath() + "/cover.jpg", document);
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
            }
        }
        log("--- DONE");
    }


    public void downloadImages(Chapter emptyChapter, SiteInterface site, ExecutorService executorService, int max, AtomicInteger current, Manga manga) {
        for(Page page : emptyChapter.getPages()) {
            String path = manga.getMangaFolderPath() + "/" + emptyChapter.getTitle() + "/";
            String imagePath = path + page.getPageNumber() + ".jpg";
            imagePath = imagePath.replace(' ', '_');
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                page.setImageFilePath(imagePath);
                String currentStatus = "\r--- Getting Images " + current.incrementAndGet() + "/" + max;
                System.out.print(currentStatus);
                setProgress(manga, ((double)current.get())/max);
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
                    setProgress(manga, ((double)current.get())/max);
                });
            }
        }
    }

    public Image getImage(SiteInterface site, Page page, String imagePath){
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


    @Override
    public  Image downloadImage(String url) throws IOException{
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

    @Override
    public MangaListingDTO getMangas(){
        MangaListingDTO ret = new MangaListingDTO();
        File root  = new File(MangaServiceImpl.BASE_DIRECTORY + "/mangas/");
        root.mkdirs();
        List<Manga> all = new ArrayList<>();
        mangaRepository.findAll().forEach(all::add);
        ret.add(all);
        ret.order();
        return ret;
    }

    private void log(String message, Manga manga) {
        if(manga != null) {
            manga.setStatus(message);
            mangaRepository.save(manga);
        }
        log(message);
    }

    private void log(String message) {
        long currentTime = System.currentTimeMillis();
        System.out.println("[" + (currentTime - time) + "] " + message);
        time = currentTime;

    }

    @Override
    public void setProgress(Manga manga, double progress){
        manga.setProgress(progress);
        mangaRepository.save(manga);
    }
}
