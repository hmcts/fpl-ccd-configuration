package uk.gov.hmcts.reform.fpl.handlers;

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
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.StandardDirectionOrderIssuedEmailContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
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
    void shouldNotifyCafcassOfIssuedStandardDirectionsOrder() {
        final SDONotifyData expectedParameters = getStandardDirectionTemplateParameters();

        CaseData caseData = caseData();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassEmailContentProviderSDOIssued.getNotifyData(caseData))
            .willReturn(expectedParameters);

        standardDirectionsOrderIssuedEventHandler.notifyCafcass(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedStandardDirectionsOrder() {
        final SDONotifyData expectedParameters = getStandardDirectionTemplateParameters();
        CaseData caseData = caseData();
        given(localAuthorityEmailContentProvider.buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        given(inboxLookupService.getNotificationRecipientEmail(caseData)).willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        standardDirectionsOrderIssuedEventHandler.notifyLocalAuthority(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, LOCAL_AUTHORITY_EMAIL_ADDRESS,
            expectedParameters, caseData.getId());
    }

    @Test
    void shouldNotifyAllocatedJudgeOfIssuedStandardDirectionsOrderWhenNotificationEnabled() {
        final AllocatedJudgeTemplateForSDO expectedParameters = getAllocatedJudgeSDOTemplateParameters();

        final CaseData caseData = caseData();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).willReturn(true);

        given(standardDirectionOrderIssuedEmailContentProvider.buildNotificationParametersForAllocatedJudge(caseData))
            .willReturn(expectedParameters);

        standardDirectionsOrderIssuedEventHandler.notifyAllocatedJudge(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS, expectedParameters, caseData.getId());
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOfIssuedStandardDirectionsOrderWhenNotificationDisabled() {
        final AllocatedJudgeTemplateForSDO expectedParameters = getAllocatedJudgeSDOTemplateParameters();
        final CaseData caseData = caseData();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).willReturn(false);

        given(standardDirectionOrderIssuedEmailContentProvider.buildNotificationParametersForAllocatedJudge(caseData))
            .willReturn(expectedParameters);

        standardDirectionsOrderIssuedEventHandler.notifyAllocatedJudge(
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

        standardDirectionsOrderIssuedEventHandler.notifyAllocatedJudge(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verifyNoInteractions(notificationService);
    }

    private SDONotifyData getStandardDirectionTemplateParameters() {
        return SDONotifyData.builder()
            .familyManCaseNumber("6789")
            .leadRespondentsName("Moley")
            .hearingDate("21 October 2020")
            .reference("12345")
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }

    private AllocatedJudgeTemplateForSDO getAllocatedJudgeSDOTemplateParameters() {
        return AllocatedJudgeTemplateForSDO.builder()
            .familyManCaseNumber("6789")
            .leadRespondentsName("Moley")
            .hearingDate("21 October 2020")
            .caseUrl("null/case/\" + JURISDICTION + \"/\" + CASE_TYPE + \"/12345")
            .judgeTitle("Her Honour Judge")
            .judgeName("Byrne")
            .build();
    }
}
