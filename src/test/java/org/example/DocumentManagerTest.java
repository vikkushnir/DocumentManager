package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.example.DocumentManager.Author;
import static org.example.DocumentManager.Document;
import static org.example.DocumentManager.SearchRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentManagerTest {
    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void testSaveNewDocument() {
        Document document = Document.builder()
                .title("Test Document")
                .content("This is a test.")
                .author(Author.builder().id("author1").name("Author One").build())
                .created(Instant.now())
                .build();

        Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId(), "Document ID should be generated");
        assertEquals(document.getTitle(), savedDocument.getTitle());
        assertEquals(document.getContent(), savedDocument.getContent());
        assertEquals(document.getAuthor(), savedDocument.getAuthor());
        assertEquals(document.getCreated(), savedDocument.getCreated());
    }

    @Test
    void testSaveExistingDocument() {
        Document document = createFirstDocument(Instant.now());
        documentManager.save(document);

        Document updatedDocument = Document.builder()
                .id(document.getId())
                .title("Updated Title")
                .content("Updated content.")
                .author(document.getAuthor())
                .created(document.getCreated())
                .build();

        Document savedDocument = documentManager.save(updatedDocument);

        assertEquals(document.getId(), savedDocument.getId());
        assertEquals(updatedDocument.getTitle(), savedDocument.getTitle());
        assertEquals(updatedDocument.getContent(), savedDocument.getContent());
        assertEquals(document.getCreated(), savedDocument.getCreated());
    }

    @Test
    void testSearchByTitlePrefixes() {
        Instant now = Instant.now();
        Document doc1 = createFirstDocument(now);
        Document doc2 = createSecondDocument(now);
        documentManager.save(doc1);
        documentManager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .titlePrefixes(List.of("First"))
                .build();

        List<Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match the search criteria");
        assertEquals(doc1.getId(), results.get(0).getId());
    }

    @Test
    void testSearchByContainsContents() {
        Instant now = Instant.now();
        Document doc1 = createFirstDocument(now);
        Document doc2 = createSecondDocument(now.minusSeconds(3600));
        documentManager.save(doc1);
        documentManager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .containsContents(List.of("test"))
                .build();

        List<Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match the content");
        assertEquals(doc1.getId(), results.get(0).getId());
    }

    @Test
    void testSearchByAuthorIds() {
        Instant now = Instant.now();
        Document doc1 = createFirstDocument(now);
        Document doc2 = createSecondDocument(now.minusSeconds(3600));
        documentManager.save(doc1);
        documentManager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .authorIds(List.of("author1"))
                .build();

        List<Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match the author ID");
        assertEquals(doc1.getId(), results.get(0).getId());
    }

    @Test
    void testSearchByCreatedFrom() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Document doc1 = createFirstDocument(now);
        Document doc2 = createSecondDocument(oneHourAgo);
        documentManager.save(doc1);
        documentManager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .createdFrom(oneHourAgo)
                .build();

        List<Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match the createdFrom filter");
        assertEquals(doc2.getId(), results.get(0).getId());
    }

    @Test
    void testSearchByCreatedTo() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Document doc1 = createFirstDocument(now);
        Document doc2 = createSecondDocument(oneHourAgo);
        documentManager.save(doc1);
        documentManager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .createdTo(now)
                .build();

        List<Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match the createdTo filter");
        assertEquals(doc1.getId(), results.get(0).getId());
    }

    @Test
    void testSearchWithMultipleFilters() {
        Instant now = Instant.now();
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        Document doc1 = createFirstDocument(now);
        Document doc2 = createSecondDocument(oneHourAgo);
        documentManager.save(doc1);
        documentManager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .titlePrefixes(List.of("First"))
                .containsContents(List.of("Content"))
                .authorIds(List.of("author1"))
                .build();

        List<Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match the search criteria");
        assertEquals(doc1.getId(), results.get(0).getId());
    }

    @Test
    void testFindById() {
        Instant now = Instant.now();
        Document document = createFirstDocument(now);
        documentManager.save(document);

        Optional<Document> foundDocument = documentManager.findById("1");

        assertTrue(foundDocument.isPresent(), "Document should be found");
        assertEquals(document.getTitle(), foundDocument.get().getTitle());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Document> foundDocument = documentManager.findById("non-existent-id");

        assertTrue(foundDocument.isEmpty(), "Document should not be found");
    }

    private Document createFirstDocument(Instant created) {
        return Document.builder()
                .id("1")
                .title("First Document")
                .content("Content with keyword test.")
                .author(Author.builder().id("author1").name("Author One").build())
                .created(created)
                .build();
    }

    private Document createSecondDocument(Instant created) {
        return Document.builder()
                .id("2")
                .title("Second Document")
                .content("Content without keyword.")
                .author(Author.builder().id("author2").name("Author Two").build())
                .created(created)
                .build();
    }
}
