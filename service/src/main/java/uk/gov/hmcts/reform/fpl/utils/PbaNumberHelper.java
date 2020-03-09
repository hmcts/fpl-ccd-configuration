package uk.gov.hmcts.reform.fpl.utils;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@NoArgsConstructor
@Component
public class PbaNumberHelper {

    private static final String PBA_NUMBER_REGEX = "PBA\\d{7}";
    private final Pattern pbaNumberPattern = Pattern.compile(PBA_NUMBER_REGEX);

    public String setPrefix(String pbaNumber) {
        if (pbaNumber.startsWith("PBA")) {
            return pbaNumber;
        } else if (pbaNumber.startsWith("pba")) {
            return pbaNumber.replace("pba", "PBA");
        }
        return "PBA" + pbaNumber;
    }

    public Stream<String> getNonEmptyPbaNumbers(List<Element<Applicant>> applicantElementsList) {
        return applicantElementsList.stream()
            .map(Element::getValue)
            .map(Applicant::getParty)
            .map(ApplicantParty::getPbaNumber)
            .filter(StringUtils::isNotEmpty);
    }

    public Optional<String> getNonEmptyPbaNumber(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(C2DocumentBundle::getPbaNumber)
            .filter(StringUtils::isNotEmpty);
    }

    public boolean isInvalidPbaNumber(String pbaNumber) {
        return !pbaNumberPattern.matcher(pbaNumber).matches();
    }
}
