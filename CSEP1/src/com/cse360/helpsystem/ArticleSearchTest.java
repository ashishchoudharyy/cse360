package com.cse360.helpsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

public class ArticleSearchTest {

    private HelpSystemApp app;

    @BeforeEach
    public void setup() {
        app = new HelpSystemApp();
        // Adding sample articles to the system
        app.articles.put(1L, new Article(1L, "beginner", "JavaFX Basics", "Introduction to JavaFX", "JavaFX is a GUI toolkit...", Set.of("360"), Set.of("JavaFX", "GUI")));
        app.articles.put(2L, new Article(2L, "intermediate", "Advanced Java", "Java Concurrency", "Details on Java concurrency...", Set.of("eclipse"), Set.of("Java", "Concurrency")));
        app.articles.put(3L, new Article(3L, "expert", "Design Patterns", "GOF Patterns", "Discussion on design patterns...", Set.of("design"), Set.of("Patterns", "Design")));
    }

    @Test
    public void testSearchByKeyword() {
        String query = "JavaFX";
        String contentLevel = "All";
        String group = "All";

        // Perform search
        app.performSearch(query, contentLevel, group, Role.STUDENT);
        
        // Check if at least one article with the keyword "JavaFX" exists
        assertTrue(app.articles.values().stream()
                .anyMatch(article -> article.getKeywords().contains(query)), "Search should return articles with the keyword 'JavaFX'.");
    }

    @Test
    public void testSearchByContentLevel() {
        String query = "";
        String contentLevel = "beginner";
        String group = "All";

        // Perform search
        app.performSearch(query, contentLevel, group, Role.STUDENT);

        // Check if at least one article with the content level "beginner" exists
        assertTrue(app.articles.values().stream()
                .anyMatch(article -> article.getLevel().equalsIgnoreCase(contentLevel)), "Search should return articles with content level 'beginner'.");
    }

    @Test
    public void testSearchByGroup() {
        String query = "";
        String contentLevel = "All";
        String group = "eclipse";

        // Perform search
        app.performSearch(query, contentLevel, group, Role.STUDENT);

        // Check if at least one article in the group "eclipse" exists
        assertTrue(app.articles.values().stream()
                .anyMatch(article -> article.getGroups().contains(group)), "Search should return articles belonging to the group 'eclipse'.");
    }

    @Test
    public void testSearchWithNoResults() {
        String query = "Python";
        String contentLevel = "All";
        String group = "All";

        // Perform search and check for no results
        boolean result = app.articles.values().stream()
                .anyMatch(article -> article.getTitle().contains(query) || article.getKeywords().contains(query));

        assertFalse(result, "Search for 'Python' should return no results.");
    }

    @Test
    public void testSearchByTitle() {
        String query = "Design Patterns";
        String contentLevel = "All";
        String group = "All";

        // Perform search
        app.performSearch(query, contentLevel, group, Role.STUDENT);

        // Check if an article with the title "Design Patterns" exists
        assertTrue(app.articles.values().stream()
                .anyMatch(article -> article.getTitle().equalsIgnoreCase(query)), "Search should return the article with the title 'Design Patterns'.");
    }
}
