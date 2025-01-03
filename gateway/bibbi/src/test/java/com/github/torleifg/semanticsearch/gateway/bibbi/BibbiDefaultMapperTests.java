package com.github.torleifg.semanticsearch.gateway.bibbi;

import no.bs.bibliografisk.model.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BibbiDefaultMapperTests {
    final BibbiDefaultMapper mapper = new BibbiDefaultMapper();

    @Test
    void mapPublicationTest() {
        var publication = new GetV1PublicationsHarvest200ResponsePublicationsInner();
        publication.setId("id");
        publication.setIsbn("isbn");
        publication.setName("title");
        publication.setPublisher("publisher");

        var author = new Creator();
        author.setRole(Creator.RoleEnum.AUT);
        author.setName("author");

        var translator = new Creator();
        translator.setRole(Creator.RoleEnum.TRL);
        translator.setName("translator");

        var illustrator = new Creator();
        illustrator.setRole(Creator.RoleEnum.ILL);
        illustrator.setName("illustrator");

        publication.setCreator(List.of(author, translator, illustrator));

        publication.setDatePublished("1970");
        publication.setDescription("description");

        var genreName = new GenreName();
        genreName.setNob("genre");

        var genre = new Genre();
        genre.setName(genreName);

        publication.setGenre(List.of(genre));

        var subjectName = new SubjectName();
        subjectName.setNob("subject");

        var subject = new Subject();
        subject.setName(subjectName);

        publication.setAbout(List.of(subject));

        var publicationImage = new PublicationImage();
        publicationImage.setThumbnailUrl(URI.create("http://thumbnailUrl"));

        publication.setImage(publicationImage);

        var metadata = mapper.from(publication);

        assertEquals("id", metadata.getExternalId());
        assertEquals("isbn", metadata.getIsbn());
        assertEquals("title", metadata.getTitle());
        assertEquals("publisher", metadata.getPublisher());
        assertEquals(1, metadata.getAuthors().size());
        assertEquals(1, metadata.getTranslators().size());
        assertEquals(1, metadata.getIllustrators().size());
        assertEquals("1970", metadata.getPublishedYear());
        assertEquals("description", metadata.getDescription());
        assertEquals(1, metadata.getGenreAndForm().size());
        assertEquals(1, metadata.getAbout().size());
        assertEquals("http://thumbnailUrl", metadata.getThumbnailUrl().toString());
    }

    @Test
    void mapDeletedPublicationTest() {
        var metadata = mapper.from("id");

        assertTrue(metadata.isDeleted());
    }
}
