package es.heavensgat.mangalib.server.models;

import java.util.ArrayList;
import java.util.List;

public class MangaListingDTO {
    private List<MangaListing> items = new ArrayList<>();

    public List<MangaListing> getItems() {
        return items;
    }

    public void setItems(List<MangaListing> items) {
        this.items = items;
    }

    public void add(MangaListing mangaListing) {
        items.add(mangaListing);
    }
}
