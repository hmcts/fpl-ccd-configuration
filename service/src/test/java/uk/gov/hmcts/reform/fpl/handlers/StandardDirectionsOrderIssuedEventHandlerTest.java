package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
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
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
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
    void shouldNotifyCafcassOfIssuedSDOAndNoticeOfProceedingsWhenSDOIssued() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        CaseData caseData = caseData();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassEmailContentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        standardDirectionsOrderIssuedEventHandler.notifyCafcassOfIssuedSDOAndNoticeOfProceedings(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_CAFCASS,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedSDOAndNoticeOfProceedingsWhenSDOIssued() {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();
        CaseData caseData = caseData();
        given(localAuthorityEmailContentProvider.buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseData))
            .willReturn(expectedParameters);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .caseData(caseData)
                .build())
        ).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        standardDirectionsOrderIssuedEventHandler.notifyLocalAuthorityOfIssuedSDOAndNoticeOfProceedings(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyCTSCOfIssuedSDOAndNoticeOfProceedingsWhenSDOIssued() {
        final CaseData caseData = caseData();
        CTSCTemplateForSDO templateForSDO = getCTSCTemplateForSDO();

        given(standardDirectionOrderIssuedEmailContentProvider.buildNotificationParametersForCTSC(caseData))
            .willReturn(templateForSDO);

        standardDirectionsOrderIssuedEventHandler.notifyCTSCOfIssuedSDOandNoticeOfProceedings(
            new StandardDirectionsOrderIssuedEvent(caseData));

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_CTSC,
            CTSC_INBOX,
            templateForSDO,
            "12345");
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

    private CTSCTemplateForSDO getCTSCTemplateForSDO() {
        return new CTSCTemplateForSDO();
    }
}
