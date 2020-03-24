package uk.gov.hmcts.reform.fpl.service.email.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;

@Service
public class UpcomingHearingsContentProvider {

    private final String uiBaseUrl;

    @Autowired
    public UpcomingHearingsContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl) {
        this.uiBaseUrl = uiBaseUrl;
    }

    public Map<String, Object> buildParameters(final LocalDate dateOfHearing, final List<CaseDetails> casesToBeHeard) {
        final String formattedCases = casesToBeHeard.stream()
            .map(this::formatCase)
            .collect(joining(lineSeparator()));

        return Map.of(
            "hearing_date", formatLocalDateToString(dateOfHearing, FormatStyle.LONG),
            "cases", formattedCases
        );
    }

    private String formatCase(CaseDetails caseDetails) {
        Object caseNumber = caseDetails.getData().get("familyManCaseNumber");
        Object caseName = caseDetails.getData().get("caseName");
        String caseUrl = format("%s/case/%s/%s/%s#OrdersTab", uiBaseUrl, JURISDICTION, CASE_TYPE, caseDetails.getId());

        return Stream.of(caseNumber, caseName, caseUrl)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(" "));
    }
}
