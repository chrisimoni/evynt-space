package com.chrisimoni.evyntspace.payment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentUtil {
    public static long convertAmountToCent(BigDecimal amount) {
        // Explicitly round to two decimal places (standard for currencies)
        BigDecimal roundedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        // Multiply by 100 to get the value in cents
        BigDecimal priceInCents = roundedAmount.multiply(new BigDecimal("100"));

        return priceInCents.longValueExact();
    }

    public static BigDecimal convertAmountToBigDecimal(long amount) {
        // Convert amount from cents/smallest unit to BigDecimal
        return BigDecimal.valueOf(amount).movePointLeft(2);
    }

    public static long calculatePlatformFee(BigDecimal amount, int platformFeePercentage) {
        // 1. Convert the percentage to a decimal (e.g., 10 -> 0.10)
        BigDecimal feeRate = new BigDecimal(platformFeePercentage)
                .divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);

        // Calculate the fee amount (Amount * Rate)
        BigDecimal feeAmount = amount.multiply(feeRate);

        return convertAmountToCent(feeAmount);
    }
}
