package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createJudgeAndLegalAdvisor;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, GeneratedOrderEmailContentProvider.class, LookupTestConfig.class
})
class GeneratedOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {
    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusDays(1);
    private static final String FAMILY_MAN_CASE_NUMBER = randomAlphabetic(8);
    private static final UUID DOCUMENT_ID = randomUUID();
    private static final String SUBJECT_LINE = "Jones, " + FAMILY_MAN_CASE_NUMBER;

    @Autowired
    private GeneratedOrderEmailContentProvider orderEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(orderEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

    @Test
    void shouldReturnExactOrderLocalAuthorityNotificationParametersWithUploadedDocumentUrl() {
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
                LOCAL_AUTHORITY_NAME,
                String.format("%s, hearing %s", SUBJECT_LINE, localDateToString(FUTURE_DATE.toLocalDate())),
                documentUrl,
                CASE_REFERENCE,
                buildCaseUrl(CASE_REFERENCE));
    }

    private String localDateToString(LocalDate date) {
        return formatLocalDateToString(date, FormatStyle.MEDIUM);
    }

    private CaseDetails createCaseDetailsWithSingleOrderElement() {
        return CaseDetails.builder()
            .id(Long.parseLong(CASE_REFERENCE))
            .data(Map.of(HEARING_DETAILS_KEY, createHearingBookings(FUTURE_DATE, FUTURE_DATE.plusDays(1)),
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
