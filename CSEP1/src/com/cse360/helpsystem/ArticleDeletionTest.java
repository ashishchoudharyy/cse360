package com.cse360.helpsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

public class ArticleDeletionTest {

    private HelpSystemApp app;

    @BeforeEach
    public void setup() {
        app = new HelpSystemApp();
        app.articles.put(1L, new Article(1, "beginner", "JavaFX Basics", "Intro to JavaFX", "JavaFX body", Set.of("360"), Set.of("JavaFX")));
    }

    @Test
    public void testValidArticleDeletion() {
        app.deleteArticle(1L);
        assertFalse(app.articles.containsKey(1L), "Article should be deleted successfully.");
    }

    @Test
    public void testDeletionOfNonExistentArticle() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            app.deleteArticle(2L);
        });
        assertEquals("Article not found", exception.getMessage());
    }
}
