package com.chrisimoni.evyntspace.event.util;

import java.text.Normalizer;
import java.util.Locale;

public class EventUtil {
    public static String generateSlug(String title) {
        // Convert to lowercase
        String slug = title.toLowerCase(Locale.ROOT);
        // Remove accents and special characters
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", ""); // Removes combining diacritical marks
        // Replace non-alphanumeric characters with hyphens
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        // Replace whitespace and consecutive hyphens with a single hyphen
        slug = slug.replaceAll("\\s+", "-").replaceAll("-{2,}", "-");
        // Trim leading or trailing hyphens
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }
}
