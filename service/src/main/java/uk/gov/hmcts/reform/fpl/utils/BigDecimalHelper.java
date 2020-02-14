package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class BigDecimalHelper {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private BigDecimalHelper() {

    }

    public static String toCCDMoneyGBP(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        BigDecimal pence = amount.multiply(HUNDRED);
        return String.valueOf(pence.longValue());
    }

    public static BigDecimal fromCCDMoneyGBP(String amount) {
        try {
            int integer = Integer.parseInt(amount);
            return BigDecimal.valueOf(integer, 2);
        } catch (NumberFormatException ex) {
            log.error("couldn't convert \"{}\" to int", amount);
        }
        return null;
    }
}
