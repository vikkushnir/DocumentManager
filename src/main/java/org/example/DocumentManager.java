package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final Map<String, Document> docsStorage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        }
        docsStorage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return docsStorage.values().stream()
                .filter(document -> matchesTitlePrefixes(document, request))
                .filter(document -> matchesContainsContents(document, request))
                .filter(document -> matchesAuthorIds(document, request))
                .filter(document -> matchesCreatedFrom(document, request))
                .filter(document -> matchesCreatedTo(document, request))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(docsStorage.get(id));
    }

    private boolean matchesTitlePrefixes(Document document, SearchRequest request) {
        List<String> titlePrefixes = request.getTitlePrefixes();
        return titlePrefixes == null || titlePrefixes.stream()
                .allMatch(prefix -> document.getTitle().toLowerCase().startsWith(prefix.toLowerCase()));
    }

    private boolean matchesContainsContents(Document document, SearchRequest request) {
        List<String> containsContents = request.getContainsContents();
        return containsContents == null || containsContents.stream()
                .allMatch(content -> document.getContent().toLowerCase().contains(content.toLowerCase()));
    }

    private boolean matchesAuthorIds(Document document, SearchRequest request) {
        List<String> authorIds = request.getAuthorIds();
        return authorIds == null || authorIds.contains(document.getAuthor().getId());
    }

    private boolean matchesCreatedFrom(Document document, SearchRequest request) {
        Instant createdFrom = request.getCreatedFrom();
        return createdFrom == null || !document.getCreated().isAfter(createdFrom);
    }

    private boolean matchesCreatedTo(Document document, SearchRequest request) {
        Instant createdTo = request.getCreatedTo();
        return createdTo == null || !document.getCreated().isBefore(createdTo);
    }

    @Data
    @Builder
    public static class SearchRequest {

        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
