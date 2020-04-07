package uk.gov.hmcts.reform.fpl.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedOrderNotificationParameters;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createJudgeAndLegalAdvisor;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, GeneratedOrderEmailContentProvider.class,
    HearingBookingService.class, LookupTestConfig.class})
class GeneratedOrderEmailContentProviderTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusDays(1);
    private static final byte[] DOCUMENT_CONTENTS = {1, 2, 3};
    private static final String COURT_EMAIL_ADDRESS = "admin@family-court.com";
    private static final String COURT_NAME = "Test court";
    private static final String COURT_CODE = "000";

    @MockBean
    private HmctsCourtLookupConfiguration courtLookupConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HearingBookingService hearingBookingService;

    private GeneratedOrderEmailContentProvider orderEmailContentProvider;

    private UUID documentId;

    @BeforeEach
    void setup() {
        this.orderEmailContentProvider = new GeneratedOrderEmailContentProvider("",
            objectMapper, hearingBookingService, courtLookupConfiguration);

        given(courtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

        documentId = randomUUID();
    }

    @Test
    void shouldReturnExactOrderNotificationParameters() {
        CaseDetails caseDetails = createCaseDetailsWithSingleOrderElement();

        Map<String, Object> returnedNotificationParameters =
            orderEmailContentProvider.buildOrderNotificationParameters(
                caseDetails, LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS);

        assertEquals(returnedNotificationParameters, getExpectedOrderNotificationParameters());
    }

    private CaseDetails createCaseDetailsWithSingleOrderElement() {
        return CaseDetails.builder()
            .id(167888L)
            .data(ImmutableMap.of(HEARING_DETAILS_KEY, createHearingBookings(FUTURE_DATE, FUTURE_DATE.plusDays(1)),
                "orderCollection", ImmutableList.of(
                    Element.<GeneratedOrder>builder()
                        .value(GeneratedOrder.builder()
                            .type(BLANK_ORDER.getLabel())
                            .title("Example Order")
                            .details(
                                "Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                            .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Peter Parker",
                                "Judy", null, HER_HONOUR_JUDGE))
                            .document(createDocumentReference(documentId.toString()))
                            .build())
                        .build()),
                "respondents1", createRespondents(),
                "familyManCaseNumber", "111111111"))
            .build();
    }
}
