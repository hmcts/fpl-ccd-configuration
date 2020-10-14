package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.StandardDirectionOrderIssuedEmailContentProvider;

import java.util.Map;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_JUDGE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.SDO;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StandardDirectionsOrderIssuedEventHandler.class, LookupTestConfig.class})
class StandardDirectionsOrderIssuedEventHandlerTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;

    @MockBean
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @MockBean
    private StandardDirectionOrderIssuedEmailContentProvider standardDirectionOrderIssuedEmailContentProvider;

    @MockBean
    private InboxLookupService inboxLookupService;

    @Autowired
    private StandardDirectionsOrderIssuedEventHandler standardDirectionsOrderIssuedEventHandler;

    @Test
    void shouldNotifyCafcassOfIssuedStandardDirectionsOrdersWhenNoticeOfProceedingsIsDisabled() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        CaseData caseData = caseData();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassEmailContentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        given(featureToggleService.isSendNoticeOfProceedingsFromSdo())
            .willReturn(false);

        standardDirectionsOrderIssuedEventHandler.notifyCafcassOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            "12345");

        verify(notificationService, never()).sendEmail(
            SDO_AND_NOP_ISSUED_CAFCASS,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyCafcassOfIssuedStandardDirectionsOrderAndNoticeOfProceedingsWhenNoticeOfProceedingsIsEnabled() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        CaseData caseData = caseData();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassEmailContentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        given(featureToggleService.isSendNoticeOfProceedingsFromSdo())
            .willReturn(true);

        standardDirectionsOrderIssuedEventHandler.notifyCafcassOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_CAFCASS,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            "12345");

        verify(notificationService, never()).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedStandardDirectionsOrderWhenNoticeOfProceedingsIsDisabled() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();
        CaseData caseData = caseData();
        given(localAuthorityEmailContentProvider.buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        given(inboxLookupService.getRecipients(caseData))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(featureToggleService.isSendNoticeOfProceedingsFromSdo())
            .willReturn(false);

        standardDirectionsOrderIssuedEventHandler.notifyLocalAuthorityOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            "12345");

        verify(notificationService, never()).sendEmail(
            SDO_AND_NOP_ISSUED_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedStandardDirectionsOrderWhenNoticeOfProceedingsIsEnabled() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();
        CaseData caseData = caseData();
        given(localAuthorityEmailContentProvider.buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        given(inboxLookupService.getRecipients(caseData))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(featureToggleService.isSendNoticeOfProceedingsFromSdo())
            .willReturn(true);

        standardDirectionsOrderIssuedEventHandler.notifyLocalAuthorityOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            "12345");

        verify(notificationService, never()).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyAllocatedJudgeOfIssuedStandardDirectionsOrderWhenNotificationEnabled() {
        final AllocatedJudgeTemplateForSDO expectedParameters = getAllocatedJudgeSDOTemplateParameters();

        final CaseData caseData = caseData();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).willReturn(true);

        given(standardDirectionOrderIssuedEmailContentProvider.buildNotificationParametersForAllocatedJudge(caseData))
            .willReturn(expectedParameters);

        given(featureToggleService.isSendNoticeOfProceedingsFromSdo())
            .willReturn(false);

        standardDirectionsOrderIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE, ALLOCATED_JUDGE_EMAIL_ADDRESS, expectedParameters,
            "12345");

        verify(notificationService, never()).sendEmail(
            SDO_AND_NOP_ISSUED_JUDGE, ALLOCATED_JUDGE_EMAIL_ADDRESS, expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyAllocatedJudgeOfIssuedStandardDirectionsOrderAndNoticeOfProceedingsWhenNotificationEnabled() {
        final AllocatedJudgeTemplateForSDO expectedParameters = getAllocatedJudgeSDOTemplateParameters();

        final CaseData caseData = caseData();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).willReturn(true);

        given(standardDirectionOrderIssuedEmailContentProvider.buildNotificationParametersForAllocatedJudge(caseData))
            .willReturn(expectedParameters);

        given(featureToggleService.isSendNoticeOfProceedingsFromSdo())
            .willReturn(true);

        standardDirectionsOrderIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_JUDGE, ALLOCATED_JUDGE_EMAIL_ADDRESS, expectedParameters,
            "12345");

        verify(notificationService, never()).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE, ALLOCATED_JUDGE_EMAIL_ADDRESS, expectedParameters,
            "12345");
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOfIssuedStandardDirectionsOrderWhenNotificationDisabled() {
        final AllocatedJudgeTemplateForSDO expectedParameters = getAllocatedJudgeSDOTemplateParameters();
        final CaseData caseData = caseData();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).willReturn(false);

        given(standardDirectionOrderIssuedEmailContentProvider.buildNotificationParametersForAllocatedJudge(caseData))
            .willReturn(expectedParameters);

        standardDirectionsOrderIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOfIssuedStandardDirectionsOrderWhenJudgeNotAllocated() {
        final AllocatedJudgeTemplateForSDO expectedParameters = getAllocatedJudgeSDOTemplateParameters();

        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .build();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).willReturn(true);

        given(standardDirectionOrderIssuedEmailContentProvider.buildNotificationParametersForAllocatedJudge(caseData))
            .willReturn(expectedParameters);

        standardDirectionsOrderIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verifyNoInteractions(notificationService);
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "6789")
            .put("leadRespondentsName", "Moley")
            .put("hearingDate", "21 October 2020")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }

    private AllocatedJudgeTemplateForSDO getAllocatedJudgeSDOTemplateParameters() {
        AllocatedJudgeTemplateForSDO allocatedJudgeTemplate = new AllocatedJudgeTemplateForSDO();
        allocatedJudgeTemplate.setFamilyManCaseNumber("6789");
        allocatedJudgeTemplate.setLeadRespondentsName("Moley");
        allocatedJudgeTemplate.setHearingDate("21 October 2020");
        allocatedJudgeTemplate.setCaseUrl("null/case/\" + JURISDICTION + \"/\" + CASE_TYPE + \"/12345");
        allocatedJudgeTemplate.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplate.setJudgeName("Byrne");

        return allocatedJudgeTemplate;
    }
}
