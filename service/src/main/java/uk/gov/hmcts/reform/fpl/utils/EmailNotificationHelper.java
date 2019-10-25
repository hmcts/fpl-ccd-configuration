package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Objects;

public class EmailNotificationHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String COMMA = ",";

    private EmailNotificationHelper() {
    }

    public static String buildSubjectLine(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails, CaseData.class);

        final String lastName = (StringUtils.isNotBlank(getRespondents1Lastname(caseData))
            ? String.format("%1$s", getRespondents1Lastname(caseData)) : "");

        final String familyManCaseNumber = (StringUtils.isNotBlank(caseData.getFamilyManCaseNumber())
            ? String.format("%1$s", caseData.getFamilyManCaseNumber()) : "");

        return String.format("%1$s%2$s%3$s", lastName,
            (StringUtils.isNoneBlank(lastName, familyManCaseNumber) ? COMMA : ""),
            familyManCaseNumber);
    }

    private static String getRespondents1Lastname(final CaseData caseData) {
        return CollectionUtils.isEmpty(caseData.getRespondents1()) ? "" : caseData.getRespondents1()
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
