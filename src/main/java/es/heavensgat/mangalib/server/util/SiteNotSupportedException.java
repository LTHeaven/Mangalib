package es.heavensgat.mangalib.server.util;

public class SiteNotSupportedException extends RuntimeException {
    public SiteNotSupportedException(){}

    public SiteNotSupportedException(String message){
        super(message);
    }
}
