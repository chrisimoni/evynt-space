package com.chrisimoni.evyntspace.payment.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentUtil {
    // A static map to cache the country name to country code mappings
    private static final Map<String, String> countryCodeCache = new HashMap<>();

    static {
        String[] locales = Locale.getISOCountries();
        for (String countryCode : locales) {
            Locale obj = Locale.of("", countryCode);
            countryCodeCache.put(obj.getDisplayCountry().toLowerCase(), countryCode);
        }
    }

    /**
     * Finds the ISO 3166-1 alpha-2 country code for a given country name using a cache.
     *
     * @param countryName The name of the country (e.g., "Portugal", "United States").
     * @return The two-letter country code (e.g., "PT", "US"), or null if not found.
     */
    public static String getCountryCode(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            return null;
        }
        return countryCodeCache.get(countryName.toLowerCase());
    }

    public static long convertAmountToCent(BigDecimal amount) {
        // Multiply by 100 to get the value in cents
        BigDecimal priceInCents = amount.multiply(new BigDecimal("100"));
        // Convert to a long integer
        return priceInCents.longValueExact();
    }
}
