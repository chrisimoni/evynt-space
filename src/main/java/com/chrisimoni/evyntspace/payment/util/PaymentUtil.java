package com.chrisimoni.evyntspace.payment.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentUtil {
    public static long convertAmountToCent(BigDecimal amount) {
        // Multiply by 100 to get the value in cents
        BigDecimal priceInCents = amount.multiply(new BigDecimal("100"));
        // Convert to a long integer
        return priceInCents.longValueExact();
    }
}
