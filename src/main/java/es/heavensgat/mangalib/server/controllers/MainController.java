package es.heavensgat.mangalib.server.controllers;

import es.heavensgat.mangalib.server.service.MangaServiceImpl;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by bened_000 on 03.11.2018.
 */
@Controller
public class MainController {
    @RequestMapping(value = "/files")
    public void getFile(@RequestParam(value = "file_name") String fileName, HttpServletResponse response) {
        try{
            String encodedName = URLEncoder.encode(fileName, "UTF-8");
            File file = new File(MangaServiceImpl.BASE_DIRECTORY + "/mangas/" + encodedName + "/" + encodedName + ".pdf");
            InputStream is = new FileInputStream(file);
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".pdf");
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
            System.out.println("error reading file");
        }
    }


}
