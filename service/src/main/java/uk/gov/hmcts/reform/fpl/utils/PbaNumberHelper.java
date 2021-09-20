package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PbaNumberHelper {

    private PbaNumberHelper() {
    }

    private static final String PBA_NUMBER_REGEX = "PBA\\d{7}";
    private static final Pattern PBA_NUMBER_PATTERN = Pattern.compile(PBA_NUMBER_REGEX);

    public static String setPrefix(String pbaNumber) {
        if (pbaNumber == null) {
            return null;
        }
        if (pbaNumber.startsWith("PBA")) {
            return pbaNumber;
        } else if (pbaNumber.startsWith("pba")) {
            return pbaNumber.replace("pba", "PBA");
        }
        return "PBA" + pbaNumber;
    }

    public static Stream<String> getNonEmptyPbaNumbers(List<Element<Applicant>> applicantElementsList) {
        return applicantElementsList.stream()
            .map(Element::getValue)
            .map(Applicant::getParty)
            .map(ApplicantParty::getPbaNumber)
            .filter(StringUtils::isNotEmpty);
    }

    public static Optional<String> getNonEmptyPbaNumber(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(C2DocumentBundle::getPbaNumber)
            .filter(StringUtils::isNotEmpty);
    }

    public static Optional<String> getPBAPaymentWithNonEmptyPbaNumber(PBAPayment pbaPayment) {
        return Optional.ofNullable(pbaPayment)
            .map(PBAPayment::getPbaNumber)
            .filter(StringUtils::isNotEmpty);
    }

    public static boolean isInvalidPbaNumber(String pbaNumber) {
        return !PBA_NUMBER_PATTERN.matcher(pbaNumber).matches();
    }
}
