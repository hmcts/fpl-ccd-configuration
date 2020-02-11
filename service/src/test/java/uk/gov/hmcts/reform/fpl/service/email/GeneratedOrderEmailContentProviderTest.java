package uk.gov.hmcts.reform.fpl.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createJudgeAndLegalAdvisor;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, GeneratedOrderEmailContentProvider.class,
    HearingBookingService.class, LocalAuthorityNameLookupConfiguration.class, DateFormatterService.class})
class GeneratedOrderEmailContentProviderTest {
    private final LocalDate today = LocalDate.now();
    private final DateFormatterService dateFormatterService = new DateFormatterService();
    private final HearingBookingService hearingBookingService = new HearingBookingService();

    private static final String LOCAL_AUTHORITY_CODE = "example";

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    private GeneratedOrderEmailContentProvider generatedOrderEmailContentProvider;

    private String familyManCaseNumber;
    private UUID documentId;
    private String subjectLine;

    @BeforeEach
    void setup() {
        this.generatedOrderEmailContentProvider = new GeneratedOrderEmailContentProvider("",
            objectMapper, hearingBookingService, localAuthorityNameLookupConfiguration, dateFormatterService);

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        familyManCaseNumber = RandomStringUtils.randomAlphabetic(8);
        documentId = randomUUID();
        subjectLine = "Jones, " + familyManCaseNumber;
    }

    @Test
    void shouldReturnExactOrderLocalAuthorityNotificationParametersWithUploadedDocumentUrl() {
        final String documentUrl = "http://dm-store:8080/documents/" + documentId + "/binary";
        CaseDetails caseDetails = createCaseDetailsWithSingleOrderElement();

        Map<String, Object> returnedLocalAuthorityParameters =
            generatedOrderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
                caseDetails, LOCAL_AUTHORITY_CODE, documentUrl);

        assertThat(returnedLocalAuthorityParameters)
            .extracting("subjectLine", "localAuthorityOrCafcass", "hearingDetailsCallout",
                "linkToDocument", "reference", "caseUrl")
            .containsExactly(subjectLine, "Example Local Authority",
                (subjectLine + ", hearing " + dateFormatterService.formatLocalDateToString(today, FormatStyle.MEDIUM)),
                documentUrl, "167888", "/case/" + JURISDICTION + "/" + CASE_TYPE + "/167888");
    }

    private CaseDetails createCaseDetailsWithSingleOrderElement() {
        final LocalDateTime now = LocalDateTime.now();
        return CaseDetails.builder()
            .id(167888L)
            .data(ImmutableMap.of(HEARING_DETAILS_KEY, createHearingBookings(now, now.plusDays(1)),
                "orderCollection", ImmutableList.of(
                    Element.<GeneratedOrder>builder()
                        .value(GeneratedOrder.builder()
                            .title("Example Order")
                            .details(
                                "Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                            .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Peter Parker",
                                "Judy", null, HER_HONOUR_JUDGE))
                            .document(createDocumentReference(documentId.toString()))
                            .build())
                        .build()),
                "respondents1", createRespondents(),
                "familyManCaseNumber", familyManCaseNumber))
            .build();
    }
}
