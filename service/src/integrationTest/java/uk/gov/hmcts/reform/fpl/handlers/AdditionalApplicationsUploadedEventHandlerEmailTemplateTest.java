package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@SpringBootTest(classes = {
    AdditionalApplicationsUploadedEventHandler.class,
    NotificationService.class,
    ObjectMapper.class
})

public class AdditionalApplicationsUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private final C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().build();
    private final String respondentLastName = "Smith";
    private final String calloutText = "Smith, SACCCCCCCC5676576567";
    private final String caseUrl = "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345#C2";
    private final List<String> applicationTypes = Arrays.asList("C2", "C13A - Special guardianship order");

    @Autowired
    private AdditionalApplicationsUploadedEventHandler underTest;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @Test
    void notifyAdmin() {
        CaseData caseData = caseData();

        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub("hmcts-non-admin@test.com").roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(additionalApplicationsUploadedEmailContentProvider.getNotifyData(caseData,
            c2DocumentBundle.getDocument()))
            .willReturn(
                AdditionalApplicationsUploadedTemplate.builder()
                    .callout(calloutText)
                    .respondentLastName(respondentLastName)
                    .caseUrl(caseUrl)
                    .applicationTypes(applicationTypes)
                    .build()
            );

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, c2DocumentBundle));

        assertThat(response())
            .hasSubject("New application uploaded, " + respondentLastName)
            .hasBody(emailContent()
                .line("New applications have been made for the case:")
                .line()
                .line(calloutText)
                .line()
                .h1("Applications")
                .line()
                .line()
                .list("C2")
                .list("C13A - Special guardianship order")
                .line()
                .h1("Next steps")
                .line("You need to:")
                .list("check the orders",
                    "check payment has been taken",
                    "confirm the judge or legal adviser has received their notification",
                    "send a copy to relevant parties")
                .line()
                .end("To review the application, sign in to " + caseUrl)
            );
    }

}
