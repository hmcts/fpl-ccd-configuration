package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DraftOrderUrgencyOption;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersSubmittedControllerTest extends AbstractUploadDraftOrdersControllerTest {

    private static final long CASE_ID = 12345L;
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2020, Month.NOVEMBER, 3, 0, 0, 0);

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    static Stream<Boolean> provideBooleanValues() {
        return Stream.of(true, false, null);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanValues")
    void shouldSendNotificationsIfNewAgreedCMOUploaded(Boolean urgency) {
        postSubmittedEvent(caseDetails(urgency));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(Boolean.TRUE.equals(urgency) ? URGENT_DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE
                : DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(JUDGE_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-case-summary"), any());
    }

    private CaseDetails caseDetails(Boolean urgency) {
        UUID hearingId = UUID.randomUUID();
        Element<HearingBooking> hearing = hearing(hearingId, HEARING_DATE);
        Element<HearingOrder> order = element(order(hearing.getValue(), SEND_TO_JUDGE));
        Element<HearingOrdersBundle> bundle = element(HearingOrdersBundle.builder()
            .hearingId(hearingId)
            .orders(newArrayList(order))
            .build());

        hearing.getValue().setCaseManagementOrderId(order.getId());

        Judge judy = Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .judgeEmailAddress(JUDGE_EMAIL)
            .build();

        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .allocatedJudge(judy)
            .hearingDetails(List.of(hearing))
            .hearingOrdersBundlesDrafts(List.of(bundle))
            .lastHearingOrderDraftsHearingId(hearing.getId())
            .build();

        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.setId(CASE_ID);
        if (urgency != null) {
            caseDetails.getData().put("draftOrderUrgency", DraftOrderUrgencyOption.builder()
                .urgency(List.of(YesNo.from(urgency)))
                .build());
        }
        return caseDetails;
    }

    private HearingOrder order(HearingBooking hearing, CMOStatus status) {
        return HearingOrder.builder()
            .status(status)
            .hearing(hearing.toLabel())
            .order(TestDataHelper.testDocumentReference())
            .dateSent(dateNow())
            .build();
    }
}
