package es.heavensgat.mangalib;

import es.heavensgat.mangalib.server.util.MangaUtil;

public class Main {
    public static void main(String[] args){
        MangaUtil.crawlCompleteManga(args[0]);
    }
}
