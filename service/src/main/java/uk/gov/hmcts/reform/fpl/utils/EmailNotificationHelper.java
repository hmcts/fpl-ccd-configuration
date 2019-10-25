package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EmailNotificationHelper {
    private EmailNotificationHelper() {
    }

    public static String buildSubjectLine(final CaseData caseData,
                                          final List<Respondent> respondents) {
        return String.format("%1$s%2$s",
            (StringUtils.isNotBlank(getRespondent1Lastname(respondents))
                ? String.format("%1$s, ", getRespondent1Lastname(respondents)) : ""),
            (StringUtils.isNotBlank(caseData.getFamilyManCaseNumber())
                ? String.format("%1$s", caseData.getFamilyManCaseNumber()) : ""));
    }

    private static String getRespondent1Lastname(final List<Respondent> respondents) {
        Optional<Respondent> optionalRespondent =
            (CollectionUtils.isEmpty(respondents) ? Optional.empty() : respondents
                .stream()
                .filter(Objects::nonNull)
                .findFirst());

        if (optionalRespondent.isPresent()) {
            return StringUtils.defaultIfBlank(optionalRespondent.get().getParty().getLastName(), "");
        }

        return StringUtils.EMPTY;
    }
}
