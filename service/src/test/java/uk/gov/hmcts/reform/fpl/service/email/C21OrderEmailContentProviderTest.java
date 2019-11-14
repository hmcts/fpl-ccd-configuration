package uk.gov.hmcts.reform.fpl.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.C21OrderEmailContentProvider;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createC21Orders;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, C21OrderEmailContentProvider.class,
    HearingBookingService.class, LocalAuthorityNameLookupConfiguration.class, DateFormatterService.class})
class C21OrderEmailContentProviderTest {
    private final LocalDate date = LocalDate.now();
    private final DateFormatterService dateFormatterService = new DateFormatterService();
    private final HearingBookingService hearingBookingService = new HearingBookingService();

    private static final String LOCAL_AUTHORITY_CODE = "example";

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    private C21OrderEmailContentProvider c21OrderEmailContentProvider;

    private String familyManCaseNumber;

    @BeforeEach
    void setup() {
        this.c21OrderEmailContentProvider = new C21OrderEmailContentProvider("",
            objectMapper, hearingBookingService, localAuthorityNameLookupConfiguration, dateFormatterService);

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        familyManCaseNumber = RandomStringUtils.randomAlphabetic(8);
    }

    @Test
    void shouldReturnExactC21NotificationParametersWithUploadedDocumentUrl() {
        CaseDetails caseDetails = populateCaseDetails();

        final String subjectLine = "Jones, " + familyManCaseNumber;

        Map<String, Object> returnedParameters = c21OrderEmailContentProvider.buildC21OrderNotification(caseDetails,
            LOCAL_AUTHORITY_CODE);

        assertThat(returnedParameters)
            .extracting("subjectLine", "localAuthorityOrCafcass", "hearingDetailsCallout", "reference",
                 "caseUrl")
            .containsExactly(subjectLine, "Example Local Authority",
                subjectLine + ", hearing " + dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM),
                "12345", "/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");

        //extracting separately as document_binary_url in createC21Orders() method uses UUID.randomUUID()
        assertThat(returnedParameters).extracting("linkToDocStore").isNotEqualTo("");
    }

    @Test
    void shouldReturnExactC21NotificationParametersWithMostRecentUploadedDocumentUrl() {
        CaseDetails caseDetails = populateCaseDetails();

        Map<String, Object> returnedParameters = c21OrderEmailContentProvider.buildC21OrderNotification(caseDetails,
            LOCAL_AUTHORITY_CODE);

        final String subjectLine = "Jones, " + familyManCaseNumber;

        assertThat(returnedParameters)
            .extracting("subjectLine", "localAuthorityOrCafcass", "hearingDetailsCallout", "linkToDocStore",
                "reference", "caseUrl")
            .containsExactly(subjectLine, "Example Local Authority",
                subjectLine + ", hearing " + dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM),
                "http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary", "12345",
                "/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
    }

    private CaseDetails populateCaseDetails() {
        return CaseDetails.builder()
            .id(12345L)
            .data(ImmutableMap.of("hearingDetails", createHearingBookings(LocalDate.now()),
                "c21Orders", createC21Orders(),
                "respondents1", createRespondents(),
                "familyManCaseNumber", familyManCaseNumber))
            .build();
    }
}
