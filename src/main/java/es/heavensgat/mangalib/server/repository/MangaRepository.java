package es.heavensgat.mangalib.server.repository;

import es.heavensgat.mangalib.server.models.Manga;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MangaRepository extends CrudRepository<Manga, Integer> {
    List<Manga> findByBaseURL(String baseUrl);
    Manga findByMangaFolderPath(String mangaFolderPath);
}