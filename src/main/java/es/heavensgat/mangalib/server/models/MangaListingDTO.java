package es.heavensgat.mangalib.server.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MangaListingDTO {
    private List<Manga> items = new ArrayList<>();

    public List<Manga> getItems() {
        return items;
    }

    public void setItems(List<Manga> items) {
        this.items = items;
    }

    public void add(Manga Manga) {
        items.add(Manga);
    }

    public void add(List<Manga> listings){
        for(Manga listing : listings){
            items.add(listing);
        }
    }

    public void order() {
        Collections.sort(items);
    }
}
