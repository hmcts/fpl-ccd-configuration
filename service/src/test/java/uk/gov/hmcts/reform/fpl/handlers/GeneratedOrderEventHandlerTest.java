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
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedCaseUrlParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GeneratedOrderEventHandler.class, InboxLookupService.class, LookupTestConfig.class,
    IssuedOrderAdminNotificationHandler.class, RepresentativeNotificationService.class,
    HmctsAdminNotificationHandler.class, FixedTimeConfiguration.class})
class GeneratedOrderEventHandlerTest {

    final String mostRecentUploadedDocumentUrl =
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
    private CaseUrlService caseUrlService;

    @Autowired
    private GeneratedOrderEventHandler generatedOrderEventHandler;

    @MockBean
    private FeatureToggleService featureToggleService;

    private CaseData caseData;

    @BeforeEach
    void before() {
        caseData = caseData();

        given(inboxLookupService.getRecipients(caseData))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(orderIssuedEmailContentProvider.buildParametersWithCaseUrl(caseData, DOCUMENT_CONTENTS, GENERATED_ORDER))
            .willReturn(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true));

        given(orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(
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

        generatedOrderEventHandler.sendEmailsForOrder(new GeneratedOrderEvent(caseData, mostRecentUploadedDocumentUrl,
            DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(COURT_EMAIL_ADDRESS),
            eqJson(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true)),
            eq("12345"));

        verify(notificationService).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
            eq("barney@rubble.com"),
            eqJson(getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true)),
            eq("12345"));

        verify(notificationService).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS)),
            eqJson(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true)),
            eq("12345"));

        verify(notificationService).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq("fred@flinstone.com"),
            eqJson(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true)),
            eq("12345"));
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

        generatedOrderEventHandler.sendNotificationToAllocatedJudgeForOrder(new GeneratedOrderEvent(caseData,
            mostRecentUploadedDocumentUrl, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE),
            eq(ALLOCATED_JUDGE_EMAIL_ADDRESS),
            eq(expectedParameters),
            eq("12345"));
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

        generatedOrderEventHandler.sendNotificationToAllocatedJudgeForOrder(new GeneratedOrderEvent(caseData,
            mostRecentUploadedDocumentUrl, DOCUMENT_CONTENTS));

        verify(notificationService, never()).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE),
            anyString(),
            anyMap(),
            anyString());
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

        generatedOrderEventHandler.sendNotificationToAllocatedJudgeForOrder(new GeneratedOrderEvent(caseData,
            mostRecentUploadedDocumentUrl, DOCUMENT_CONTENTS));

        verify(notificationService, never()).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE),
            anyString(),
            anyMap(),
            anyString());
    }

    private AllocatedJudgeTemplateForGeneratedOrder getOrderIssuedAllocatedJudgeParameters() {
        AllocatedJudgeTemplateForGeneratedOrder judgeTemplate = new AllocatedJudgeTemplateForGeneratedOrder();
        judgeTemplate.setOrderType("blank order (c21)");
        judgeTemplate.setCallout("^Jones, SACCCCCCCC5676576567, hearing 26 Aug 2020");
        judgeTemplate.setCaseUrl("null/case/\" + JURISDICTION + \"/\" + CASE_TYPE + \"/12345");
        judgeTemplate.setRespondentLastName("Smith");
        judgeTemplate.setJudgeTitle("Her Honour Judge");
        judgeTemplate.setJudgeName("Byrne");

        return judgeTemplate;
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
