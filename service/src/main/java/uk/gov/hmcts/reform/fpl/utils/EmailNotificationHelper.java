package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

public class EmailNotificationHelper {

    private EmailNotificationHelper() {
    }

    public static String buildSubjectLine(final CaseData caseData) {
        final String lastName = getRespondents1Lastname(caseData);
        final String familyManCaseNumber = defaultIfNull(caseData.getFamilyManCaseNumber(), "");

        return String.format("%1$s%2$s%3$s", lastName,
            (isNoneBlank(lastName, familyManCaseNumber) ? ", " : ""),
            familyManCaseNumber);
    }

    private static String getRespondents1Lastname(final CaseData caseData) {
        return isEmpty(caseData.getRespondents1()) ? "" : caseData.getRespondents1()
            .stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .findFirst()
            .map(Respondent::getParty)
            .map(RespondentParty::getLastName)
            .orElse("");
    }
}
