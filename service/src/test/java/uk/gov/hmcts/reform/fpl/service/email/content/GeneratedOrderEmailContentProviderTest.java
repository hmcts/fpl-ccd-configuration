package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createJudgeAndLegalAdvisor;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GeneratedOrderEmailContentProvider.class})
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class GeneratedOrderEmailContentProviderTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final LocalDate TODAY = LocalDate.now();
    private static final String FAMILY_MAN_CASE_NUMBER = randomAlphabetic(8);
    private static final UUID DOCUMENT_ID = randomUUID();
    private static final String SUBJECT_LINE = "Jones, " + FAMILY_MAN_CASE_NUMBER;
    private static final String CASE_REFERENCE = "12345";

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    private GeneratedOrderEmailContentProvider orderEmailContentProvider;

    @Test
    void shouldReturnExactOrderLocalAuthorityNotificationParametersWithUploadedDocumentUrl() {
        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        String documentUrl = "http://dm-store:8080/documents/" + DOCUMENT_ID + "/binary";
        CaseDetails caseDetails = createCaseDetailsWithSingleOrderElement();

        Map<String, Object> returnedLocalAuthorityParameters = orderEmailContentProvider
            .buildOrderNotificationParametersForLocalAuthority(caseDetails, LOCAL_AUTHORITY_CODE, documentUrl);

        assertThat(returnedLocalAuthorityParameters)
            .extracting("subjectLine",
                "localAuthorityOrCafcass",
                "hearingDetailsCallout",
                "linkToDocument",
                "reference",
                "caseUrl")
            .containsExactly(SUBJECT_LINE,
                "Example Local Authority",
                String.format("%s, hearing %s", SUBJECT_LINE, formatLocalDateToString(TODAY, FormatStyle.MEDIUM)),
                documentUrl,
                CASE_REFERENCE,
                String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, CASE_REFERENCE));
    }

    private CaseDetails createCaseDetailsWithSingleOrderElement() {
        final LocalDateTime now = LocalDateTime.now();

        return CaseDetails.builder()
            .id(Long.parseLong(CASE_REFERENCE))
            .data(Map.of(HEARING_DETAILS_KEY, createHearingBookings(now, now.plusDays(1)),
                "orderCollection", wrapElements(GeneratedOrder.builder()
                    .title("Example Order")
                    .details("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                    .judgeAndLegalAdvisor(getJudgeAndLegalAdvisor())
                    .document(createDocumentReference(DOCUMENT_ID.toString()))
                    .build()),
                "respondents1", createRespondents(),
                "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
            .build();
    }

    private JudgeAndLegalAdvisor getJudgeAndLegalAdvisor() {
        return createJudgeAndLegalAdvisor("Peter Parker", "Judy", null, HER_HONOUR_JUDGE);
    }
}
