package uk.gov.hmcts.reform.fpl.service.job;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.email.content.UpcomingHearingsContentProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.lineSeparator;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UpcomingHearingsContentProviderTest {

    private UpcomingHearingsContentProvider contentProvider = new UpcomingHearingsContentProvider("test-host");

    @Test
    void shouldPrepareParametersForMultipleCases() {
        LocalDate hearingDate = LocalDate.of(2020, MAY, 20);
        CaseDetails case1 = buildCase(1L, "11", "case1");
        CaseDetails case2 = buildCase(2L, "22", "case2");

        Map<String, Object> expectedParams = Map.of(
            "hearing_date", "20 May 2020",
            "cases", "11 case1 test-host/case/PUBLICLAW/CARE_SUPERVISION_EPO/1#OrdersTab"
                +
                lineSeparator()
                +
                "22 case2 test-host/case/PUBLICLAW/CARE_SUPERVISION_EPO/2#OrdersTab");

        Map<String, Object> actualParams = contentProvider.buildParameters(hearingDate, List.of(case1, case2));

        assertThat(actualParams).isEqualTo(expectedParams);
    }

    @Test
    void shouldPrepareParametersForSingleCase() {
        LocalDate hearingDate = LocalDate.of(2020, JANUARY, 9);
        CaseDetails case1 = buildCase(1L, "11", "case1");

        Map<String, Object> expectedParams = Map.of(
            "hearing_date", "9 January 2020",
            "cases", "11 case1 test-host/case/PUBLICLAW/CARE_SUPERVISION_EPO/1#OrdersTab");


        Map<String, Object> actualParams = contentProvider.buildParameters(hearingDate, List.of(case1));

        assertThat(actualParams).isEqualTo(expectedParams);
    }

    @Test
    void shouldPrepareParametersWhenCaseNameIsAbsentOrEmpty() {
        final LocalDate hearingDate = LocalDate.of(2020, MAY, 1);
        CaseDetails case1 = buildCase(1L, "11", "");
        CaseDetails case2 = buildCase(2L, "22", null);

        Map<String, Object> expectedParams = Map.of(
            "hearing_date", "1 May 2020",
            "cases", "11 test-host/case/PUBLICLAW/CARE_SUPERVISION_EPO/1#OrdersTab"
                +
                lineSeparator()
                +
                "22 test-host/case/PUBLICLAW/CARE_SUPERVISION_EPO/2#OrdersTab");

        Map<String, Object> actualParams = contentProvider.buildParameters(hearingDate, List.of(case1, case2));

        assertThat(actualParams).isEqualTo(expectedParams);
    }

    private CaseDetails buildCase(Long caseId, String caseNumber, String caseName) {
        final Map<String, Object> data = new HashMap<>();
        ofNullable(caseNumber).ifPresent(cn -> data.put("familyManCaseNumber", cn));
        ofNullable(caseName).ifPresent(cn -> data.put("caseName", cn));

        return CaseDetails.builder()
            .id(caseId)
            .data(data)
            .build();
    }
}
