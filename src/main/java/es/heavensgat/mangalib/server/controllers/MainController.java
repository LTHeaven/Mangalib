package es.heavensgat.mangalib.server.controllers;

import es.heavensgat.mangalib.server.service.MangaService;
import es.heavensgat.mangalib.server.service.MangaServiceImpl;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * Created by bened_000 on 03.11.2018.
 */
@Controller
public class MainController {
    @Autowired
    private MangaService mangaService;

    @RequestMapping(value = "/files")
    public void getFile(@RequestParam(value = "file_name") String fileName, @RequestParam(value = "index", required = false) Integer index, HttpServletResponse response) {
        try{
            String encodedName = URLEncoder.encode(fileName, "UTF-8");
            String mangaFolderPath = MangaServiceImpl.BASE_DIRECTORY + "/mangas/" + encodedName;
            File file = new File(mangaFolderPath + "/" + encodedName + (index != null ? "-" + index : "") + ".pdf");
            mangaService.removeUpdated(mangaFolderPath);
            InputStream is = new FileInputStream(file);
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + (index != null ? "-" + index : "") + ".pdf");
            response.addHeader("Content-Length", "" + file.length());
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            System.out.println("error reading file");
        }
    }

    @RequestMapping(value = "/cover")
    public void getCover(@RequestParam(value = "file_name") String fileName, HttpServletResponse response) {
        try{
            InputStream is = new FileInputStream(MangaServiceImpl.BASE_DIRECTORY + "/mangas/" + URLEncoder.encode(fileName, "UTF-8") + "/cover.jpg");
//            response.setContentType("application/jpg");
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
        }
    }

    @Scheduled(cron = "30 2 * * *")
    public void checkForUpdated(){
        mangaService.checkForUpdates();
    }

}
