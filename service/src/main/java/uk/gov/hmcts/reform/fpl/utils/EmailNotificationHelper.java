package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class EmailNotificationHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String COMMA = ",";

    private EmailNotificationHelper() {
    }

    public static String buildSubjectLine(final CaseDetails caseDetails) {
        final List<Respondent> respondents = getRespondents1(caseDetails);

        final String lastName = (StringUtils.isNotBlank(getRespondent1Lastname(respondents))
            ? String.format("%1$s", getRespondent1Lastname(respondents)) : "");

        final String familyManCaseNumber = (StringUtils.isNotBlank(getFamilyManCaseNumber(caseDetails))
            ? String.format("%1$s", getFamilyManCaseNumber(caseDetails)) : "");

        return String.format("%1$s%2$s%3$s", lastName,
            (StringUtils.isNoneBlank(lastName, familyManCaseNumber) ? COMMA : ""),
            familyManCaseNumber);
    }

    private static String getFamilyManCaseNumber(final CaseDetails caseDetails) {
        return (String) caseDetails.getData().getOrDefault("familyManCaseNumber", "");
    }

    private static List<Respondent> getRespondents1(final CaseDetails caseDetails) {
        List<Map<String, Object>> respondents1 = (ObjectUtils.isEmpty(caseDetails.getData().get("respondents1"))
            ? Collections.emptyList() : objectMapper.convertValue(
            caseDetails.getData().get("respondents1"), new TypeReference<>() {}));

        return (CollectionUtils.isEmpty(respondents1)
            ? Collections.emptyList() : respondents1.stream().map(respondent ->
            objectMapper.convertValue(respondent.get("value"), Respondent.class)).collect(toList()));
    }

    private static String getRespondent1Lastname(final List<Respondent> respondents) {
        return respondents.stream()
            .filter(Objects::nonNull)
            .findFirst().map(Respondent::getParty)
            .map(RespondentParty::getLastName)
            .orElse("");
    }
}
