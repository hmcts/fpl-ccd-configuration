package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.time.LocalDate;
import java.time.Period;

import static com.google.common.base.Preconditions.checkNotNull;

public class AgeDisplayFormatHelper {

    private AgeDisplayFormatHelper() {
    }

    public static String formatAgeDisplay(final LocalDate dateOfBirth,
                                          Language applicationLanguage) {
        checkNotNull(dateOfBirth, "Date of birth value is required");

        final Period period = Period.between(dateOfBirth, LocalDate.now());
        final int years = period.getYears();
        final int months = period.getMonths();
        final int days = period.getDays();

        if (period.isNegative()) {
            return zeroYearsOld(applicationLanguage);
        }
        if (years > 1) {
            return years + yearsOld(applicationLanguage);
        }
        if (years == 1) {
            return years + yearOld(applicationLanguage);
        }
        if (months > 1) {
            return months + monthsOld(applicationLanguage);
        }
        if (months == 1) {
            return months + monthOld(applicationLanguage);
        }
        if (days > 1) {
            return days + daysOld(applicationLanguage);
        }
        if (days == 1) {
            return days + dayOld(applicationLanguage);
        }

        return zeroDaysOld(applicationLanguage);

    }

    private static String zeroDaysOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? "0 days old" : "0 diwrnod oed";
    }

    private static String dayOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? " day old" : " diwrnod oed";
    }

    private static String daysOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? " days old" : " diwrnod oed";
    }

    private static String monthOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? " month old" : " mis oed";
    }

    private static String monthsOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? " months old" : " mis oed";
    }

    private static String yearOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? " year old" : " mlwydd oed";
    }

    private static String yearsOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? " years old" : " mlwydd oed";
    }

    private static String zeroYearsOld(Language applicationLanguage) {
        return isEnglish(applicationLanguage) ? "0 years old" : "0 mlwydd oed";
    }

    private static boolean isEnglish(Language applicationLanguage) {
        return applicationLanguage == Language.ENGLISH;
    }

}
