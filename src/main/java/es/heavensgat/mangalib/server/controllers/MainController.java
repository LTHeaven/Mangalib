package es.heavensgat.mangalib.server.controllers;

import es.heavensgat.mangalib.server.util.MangaUtil;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bened_000 on 03.11.2018.
 */
@Controller
public class MainController {
    @RequestMapping(value = "/files")
    public void getFile(@RequestParam(value = "file_name") String fileName, HttpServletResponse response) {
        try{
            InputStream is = new FileInputStream(MangaUtil.BASE_DIRECTORY + "/mangas/" + fileName);
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            System.out.println("error reading file");
        }
    }


}
