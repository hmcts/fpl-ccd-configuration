package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.GATEKEEPER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NotifyGatekeeperEventHandler.class, LookupTestConfig.class,
    GatekeeperEmailContentProvider.class})
class NotifyGatekeeperEventHandlerTest {
    private static final String CASE_ID = "12345";

    @Captor
    private ArgumentCaptor<NotifyGatekeeperTemplate> captor;
    @MockBean
    private NotificationService notificationService;
    @Autowired
    private NotifyGatekeeperEventHandler notifyGatekeeperEventHandler;
    @MockBean
    private CaseUrlService caseUrlService;
    @MockBean
    private DocumentDownloadService documentDownloadService;

    @BeforeEach
    void init() {
        when(caseUrlService.getCaseUrl(Long.valueOf(CASE_ID))).thenReturn("http://case/url");
    }

    @Test
    void shouldSendEmailToMultipleGatekeepers() {
        CaseData caseData = caseData();

        notifyGatekeeperEventHandler.notifyGatekeeper(new NotifyGatekeepersEvent(caseData));

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(GATEKEEPER_EMAIL_ADDRESS),
            captor.capture(), eq(caseData.getId()));

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq("Cafcass+gatekeeper@gmail.com"),
            captor.capture(), eq(caseData.getId()));

        NotifyGatekeeperTemplate firstTemplate = getExpectedTemplate();
        NotifyGatekeeperTemplate secondTemplate = getExpectedTemplate();

        assertThat(captor.getAllValues()).usingFieldByFieldElementComparator()
            .containsOnly(firstTemplate, secondTemplate);
    }

    private NotifyGatekeeperTemplate getExpectedTemplate() {
        return NotifyGatekeeperTemplate.builder()
            .caseUrl("http://case/url")
            .dataPresent(YES.getValue())
            .firstRespondentName("Smith")
            .fullStop(NO.getValue())
            .reference(CASE_ID)
            .nonUrgentHearing(NO.getValue())
            .timeFramePresent(YES.getValue())
            .timeFrameValue("same day")
            .urgentHearing(YES.getValue())
            .ordersAndDirections(List.of("Emergency protection order", "Contact with any named person"))
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .build();
    }
}
