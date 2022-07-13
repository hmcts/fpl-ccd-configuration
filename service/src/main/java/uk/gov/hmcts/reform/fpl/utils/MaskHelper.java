package uk.gov.hmcts.reform.fpl.utils;

import java.util.Optional;

public class MaskHelper {

    private MaskHelper() {
    }

    public static String maskEmail(String text, String email) {
        return Optional.ofNullable(text)
                .map(t -> t.replaceAll(email, maskEmail(email)))
                .orElse(null);
    }

    public static String maskEmail(String email) {
        return email;
//        return Optional.ofNullable(email)
//                .map(e -> e.replaceAll("[^@]", "*"))
//                .orElse("");
    }

}
