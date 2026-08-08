package com.finpilot.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class EmiCalculator {

    private EmiCalculator() {
    }

    /**
     * Standard reducing-balance EMI formula:
     * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     * where r is the monthly interest rate (annual rate / 12 / 100)
     */
    public static BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        if (annualRatePercent.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(12), MathContext.DECIMAL64)
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, MathContext.DECIMAL64);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent
                .divide(BigDecimal.valueOf(12), MathContext.DECIMAL64)
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
    }
}