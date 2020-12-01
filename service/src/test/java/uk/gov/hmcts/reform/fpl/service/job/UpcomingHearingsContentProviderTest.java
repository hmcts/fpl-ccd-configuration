package uk.gov.hmcts.reform.fpl.service.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.UpcomingHearingNotifyData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
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
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UpcomingHearingsContentProviderTest {

    private static final long CASE_1_ID = 1L;
    private static final long CASE_2_ID = 2L;
    private static final String CASE_1_NAME = "case1";
    private static final String CASE_2_NAME = "case2";
    private static final String CASE_1_NUMBER = "11";
    private static final String CASE_2_NUMBER = "22";
    private static final String CASE_1_URL = "case1Url";
    private static final String CASE_2_URL = "case2Url";

    @Mock
    private CaseUrlService caseUrlService;

    @InjectMocks
    private UpcomingHearingsContentProvider contentProvider;

    @BeforeEach
    void setup() {
        when(caseUrlService.getCaseUrl(CASE_1_ID, "OrdersTab")).thenReturn(CASE_1_URL);
        when(caseUrlService.getCaseUrl(CASE_2_ID, "OrdersTab")).thenReturn(CASE_2_URL);
    }

    @Test
    void shouldPrepareParametersForMultipleCases() {
        LocalDate hearingDate = LocalDate.of(2020, MAY, 20);
        CaseDetails case1 = buildCase(CASE_1_ID, CASE_1_NUMBER, CASE_1_NAME);
        CaseDetails case2 = buildCase(CASE_2_ID, CASE_2_NUMBER, CASE_2_NAME);

        UpcomingHearingNotifyData expectedParams = UpcomingHearingNotifyData.builder()
            .hearingDate("20 May 2020")
            .cases(String.format("%s %s %s", CASE_1_NUMBER, CASE_1_NAME, CASE_1_URL)
                +
                lineSeparator()
                +
                String.format("%s %s %s", CASE_2_NUMBER, CASE_2_NAME, CASE_2_URL))
            .build();

        UpcomingHearingNotifyData actualParams = contentProvider.buildParameters(hearingDate, List.of(case1, case2));

        assertThat(actualParams).isEqualTo(expectedParams);
    }

    @Test
    void shouldPrepareParametersForSingleCase() {
        LocalDate hearingDate = LocalDate.of(2020, JANUARY, 9);
        CaseDetails case1 = buildCase(CASE_1_ID, CASE_1_NUMBER, CASE_1_NAME);

        UpcomingHearingNotifyData expectedParams = UpcomingHearingNotifyData.builder()
            .hearingDate("9 January 2020")
            .cases(String.format("%s %s %s", CASE_1_NUMBER, CASE_1_NAME, CASE_1_URL))
            .build();

        UpcomingHearingNotifyData actualParams = contentProvider.buildParameters(hearingDate, List.of(case1));

        assertThat(actualParams).isEqualTo(expectedParams);
    }

    @Test
    void shouldPrepareParametersWhenCaseNameIsAbsentOrEmpty() {
        final LocalDate hearingDate = LocalDate.of(2020, MAY, 1);
        CaseDetails case1 = buildCase(CASE_1_ID, CASE_1_NUMBER, null);
        CaseDetails case2 = buildCase(CASE_2_ID, CASE_2_NUMBER, null);

        UpcomingHearingNotifyData expectedParams = UpcomingHearingNotifyData.builder()
            .hearingDate("1 May 2020")
            .cases(String.format("%s %s", CASE_1_NUMBER, CASE_1_URL)
                +
                lineSeparator()
                +
                String.format("%s %s", CASE_2_NUMBER, CASE_2_URL))
            .build();

        UpcomingHearingNotifyData actualParams = contentProvider.buildParameters(hearingDate, List.of(case1, case2));

        assertThat(actualParams).isEqualTo(expectedParams);
    }

    @Test
    void shouldSerializeUpcomingHearingsEmailParameters() {
        LocalDate hearingDate = LocalDate.of(2020, JANUARY, 9);
        CaseDetails case1 = buildCase(CASE_1_ID, CASE_1_NUMBER, CASE_1_NAME);

        Map<String, Object> expectedParams = ImmutableMap.of(
            "hearing_date", "9 January 2020",
            "cases", String.format("%s %s %s", CASE_1_NUMBER, CASE_1_NAME, CASE_1_URL));

        ObjectMapper mapper = new ObjectMapper();
        UpcomingHearingNotifyData template = contentProvider.buildParameters(hearingDate, List.of(case1));
        Map<String, Object> actualParams = mapper.convertValue(template, new TypeReference<>() {
        });

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
