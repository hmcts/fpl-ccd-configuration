package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GeneratedOrderEventHandler.class, InboxLookupService.class, LookupTestConfig.class,
    IssuedOrderAdminNotificationHandler.class, RepresentativeNotificationService.class,
    HmctsAdminNotificationHandler.class})
@TestPropertySource(properties = {"manage-case.ui.base.url=http://fake-url/"})
class GeneratedOrderEventHandlerTest {

    @MockBean
    private GeneratedOrderService generatedOrderService;

    @MockBean
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private RepresentativeNotificationService representativeNotificationService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @Autowired
    private GeneratedOrderEventHandler generatedOrderEventHandler;

    private CaseData caseData = caseData();

    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;

    private final GeneratedOrderEvent event = new GeneratedOrderEvent(caseData, DocumentReference.builder()
        .filename("GeneratedOrder")
        .url("url")
        .binaryUrl("testUrl")
        .build());

    @BeforeEach
    void before() {
        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData, "testUrl",
            GENERATED_ORDER))
            .willReturn(getExpectedParameters(BLANK_ORDER.getLabel(), true));

        given(orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(
            caseData, event.getOrderDocument(), GENERATED_ORDER))
            .willReturn(getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true));
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmission() {
        generatedOrderEventHandler.notifyParties(event);

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(
            caseData,
            "testUrl",
            GENERATED_ORDER);

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            getExpectedParameters(BLANK_ORDER.getLabel(), true),
            caseData.getId().toString());

        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            DIGITAL_SERVICE,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            getExpectedParameters(BLANK_ORDER.getLabel(), true),
            caseData);

        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES,
            getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true),
            caseData);
    }

    @Test
    void shouldNotifyAllocatedJudgeOnOrderIssued() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge@gmail.com")
            .build();

        given(generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData))
            .willReturn(expectedJudgeAndLegalAdvisor);

        final AllocatedJudgeTemplateForGeneratedOrder expectedParameters = getOrderIssuedAllocatedJudgeParameters();

        given(orderIssuedEmailContentProvider.buildAllocatedJudgeOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        generatedOrderEventHandler.notifyAllocatedJudge(event);

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOnOrderIssuedWithNoJudge() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        given(generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData))
            .willReturn(expectedJudgeAndLegalAdvisor);

        final AllocatedJudgeTemplateForGeneratedOrder expectedParameters = getOrderIssuedAllocatedJudgeParameters();

        given(orderIssuedEmailContentProvider.buildAllocatedJudgeOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        generatedOrderEventHandler.notifyAllocatedJudge(event);

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
