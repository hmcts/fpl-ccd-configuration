package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GeneratedOrderEventHandler.class, InboxLookupService.class, LookupTestConfig.class,
    IssuedOrderAdminNotificationHandler.class, RepresentativeNotificationService.class,
    HmctsAdminNotificationHandler.class})
class GeneratedOrderEventHandlerTest {

    private static final String DOCUMENT_URL =
        "http://fake-document-gateway/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary";

    @MockBean
    private GeneratedOrderService generatedOrderService;

    @MockBean
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private GeneratedOrderEventHandler generatedOrderEventHandler;

    private CaseData caseData;

    @BeforeEach
    void before() {
        caseData = caseData();

        given(inboxLookupService.getNotificationRecipientEmail(caseData))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData, DOCUMENT_CONTENTS, GENERATED_ORDER))
            .willReturn(getExpectedParameters(BLANK_ORDER.getLabel(), true));

        given(orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(
            caseData, DOCUMENT_CONTENTS, GENERATED_ORDER))
            .willReturn(getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true));
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmission() {
        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            DIGITAL_SERVICE))
            .willReturn(getExpectedDigitalServedRepresentativesForAddingPartiesToCase());

        generatedOrderEventHandler.notifyParties(new GeneratedOrderEvent(caseData, DOCUMENT_URL,
            DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            COURT_EMAIL_ADDRESS,
            getExpectedParameters(BLANK_ORDER.getLabel(), true),
            caseData.getId());

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES,
            "barney@rubble.com",
            getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true),
            caseData.getId());

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            getExpectedParameters(BLANK_ORDER.getLabel(), true),
            caseData.getId());

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            "fred@flinstone.com",
            getExpectedParameters(BLANK_ORDER.getLabel(), true),
            caseData.getId());
    }

    @Test
    void shouldNotifyAllocatedJudgeOnOrderIssuedAndEnabled() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge@gmail.com")
            .build();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType.GENERATED_ORDER))
            .willReturn(true);

        given(generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData))
            .willReturn(expectedJudgeAndLegalAdvisor);

        final AllocatedJudgeTemplateForGeneratedOrder expectedParameters = getOrderIssuedAllocatedJudgeParameters();

        given(orderIssuedEmailContentProvider.buildAllocatedJudgeOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        generatedOrderEventHandler.notifyAllocatedJudge(new GeneratedOrderEvent(caseData,
            DOCUMENT_URL, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOnOrderIssuedAndDisabled() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge@gmail.com")
            .build();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType.GENERATED_ORDER))
            .willReturn(false);

        given(generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData))
            .willReturn(expectedJudgeAndLegalAdvisor);

        final AllocatedJudgeTemplateForGeneratedOrder expectedParameters = getOrderIssuedAllocatedJudgeParameters();

        given(orderIssuedEmailContentProvider.buildAllocatedJudgeOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        generatedOrderEventHandler.notifyAllocatedJudge(new GeneratedOrderEvent(caseData,
            DOCUMENT_URL, DOCUMENT_CONTENTS));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOnOrderIssuedWithNoJudge() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType.GENERATED_ORDER))
            .willReturn(true);

        given(generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData))
            .willReturn(expectedJudgeAndLegalAdvisor);

        final AllocatedJudgeTemplateForGeneratedOrder expectedParameters = getOrderIssuedAllocatedJudgeParameters();

        given(orderIssuedEmailContentProvider.buildAllocatedJudgeOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        generatedOrderEventHandler.notifyAllocatedJudge(new GeneratedOrderEvent(caseData,
            DOCUMENT_URL, DOCUMENT_CONTENTS));

        verifyNoInteractions(notificationService);
    }

    private AllocatedJudgeTemplateForGeneratedOrder getOrderIssuedAllocatedJudgeParameters() {
        return AllocatedJudgeTemplateForGeneratedOrder.builder()
            .orderType("blank order (c21)")
            .callout("^Jones, SACCCCCCCC5676576567, hearing 26 Aug 2020")
            .caseUrl("null/case/\" + JURISDICTION + \"/\" + CASE_TYPE + \"/12345")
            .respondentLastName("Smith")
            .judgeTitle("Her Honour Judge")
            .judgeName("Byrne")
            .build();
    }

    private List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(EMAIL)
                .build());
    }

    private List<Representative> getExpectedDigitalServedRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("fred@flinstone.com")
                .fullName("Fred Flinstone")
                .servingPreferences(DIGITAL_SERVICE)
                .build());
    }
}
