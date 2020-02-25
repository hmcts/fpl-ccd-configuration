package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
public class BigDecimalHelper {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 2;

    private BigDecimalHelper() {

    }

    public static String toCCDMoneyGBP(BigDecimal amount) {
        return Optional.ofNullable(amount)
            .map(BigDecimalHelper::toPence)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
    }

    public static Optional<BigDecimal> fromCCDMoneyGBP(String amount) {
        try {
            return Optional.of(fromPence(amount));
        } catch (NumberFormatException ex) {
            log.error("couldn't convert \"{}\" to int", amount);
            return Optional.empty();
        }
    }

    private static Long toPence(BigDecimal amount) {
        return amount.multiply(HUNDRED).longValue();
    }

    private static BigDecimal fromPence(String amount) {
        long pence = Long.parseLong(amount);
        return BigDecimal.valueOf(pence, SCALE);
    }
}
