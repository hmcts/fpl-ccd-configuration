package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {PlacementApplicationEventHandler.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
class PlacementApplicationEventHandlerTest {

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private PlacementApplicationContentProvider placementApplicationContentProvider;

    @Autowired
    private PlacementApplicationEventHandler placementApplicationEventHandler;

    @Test
    void shouldNotifyHmctsAdminOfPlacementApplicationUploadWhenCtscIsDisabled() {
        CaseData caseData = caseData();

        final BaseCaseNotifyData expectedParameters = getExpectedPlacementNotificationParameters();

        given(placementApplicationContentProvider.buildPlacementApplicationNotificationParameters(caseData))
            .willReturn(expectedParameters);

        placementApplicationEventHandler.notifyAdmin(new PlacementApplicationEvent(caseData));

        verify(notificationService).sendEmail(
            PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
            COURT_EMAIL_ADDRESS,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyCtscAdminOfPlacementApplicationUploadWhenCtscIsEnabled() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        final BaseCaseNotifyData expectedParameters = getExpectedPlacementNotificationParameters();

        given(placementApplicationContentProvider.buildPlacementApplicationNotificationParameters(caseData))
            .willReturn(expectedParameters);

        placementApplicationEventHandler.notifyAdmin(new PlacementApplicationEvent(caseData));

        verify(notificationService).sendEmail(
            PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

    private BaseCaseNotifyData getExpectedPlacementNotificationParameters() {
        return BaseCaseNotifyData.builder()
            .respondentLastName("Moley")
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }
}
