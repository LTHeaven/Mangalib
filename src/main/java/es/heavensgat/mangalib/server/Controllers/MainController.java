package es.heavensgat.mangalib.server.controllers;

import es.heavensgat.mangalib.server.models.MangaListing;
import es.heavensgat.mangalib.server.util.MangaUtil;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bened_000 on 03.11.2018.
 */
@Controller
public class MainController {
    @RequestMapping("/")
    public String overview(Model model) {
        model.addAttribute("mangas", getMangas());
        return "overview";
    }

    @RequestMapping(value = "/files")
    public void getFile(@RequestParam(value = "file_name") String fileName, HttpServletResponse response) {
        try{
            InputStream is = new FileInputStream(MangaUtil.BASE_DIRECTORY + "/mangas/" + fileName);
            response.setContentType("application/pdf");
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            System.out.println("error reading file");
        }
    }

    @RequestMapping(value="/mangas", method = RequestMethod.POST)
    public String mangaAdd(@RequestParam String mangaUrl, Model model){
        MangaUtil.crawlCompleteManga(mangaUrl);
        model.addAttribute("mangas", getMangas());
        return "overview";
    }

    private List<MangaListing> getMangas(){
        List<MangaListing> ret = new ArrayList<>();
        File root  = new File(MangaUtil.BASE_DIRECTORY + "/mangas/");
        root.mkdirs();
        for(File file : root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(".pdf");
            }
        })){
            ret.add(new MangaListing(file.getName().replace(".pdf", ""), "files?file_name=" + file.getName()));
        }
        return ret;
    }
}
