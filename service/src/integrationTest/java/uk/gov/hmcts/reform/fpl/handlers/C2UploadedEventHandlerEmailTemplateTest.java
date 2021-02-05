package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.mockito.BDDMockito.given;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@SpringBootTest(classes = {
    C2UploadedEventHandler.class,
    NotificationService.class,
    ObjectMapper.class
})

public class C2UploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private final C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().build();
    private final String respondentLastName = "Smith";
    private final String calloutText = "Smith, SACCCCCCCC5676576567";
    private final String caseUrl = "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345#C2";

    @Autowired
    private C2UploadedEventHandler underTest;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @Test
    void notifyAdmin() {
        CaseData caseData = caseData();

        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub("hmcts-non-admin@test.com").roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(c2UploadedEmailContentProvider.getNotifyData(caseData,
            c2DocumentBundle.getDocument()))
            .willReturn(
                C2UploadedTemplate.builder()
                    .callout(calloutText)
                    .respondentLastName(respondentLastName)
                    .caseUrl(caseUrl)
                    .build()
            );

        underTest.notifyAdmin(new C2UploadedEvent(caseData, c2DocumentBundle));

        assertThat(response())
            .hasSubject("C2 application received, " + respondentLastName)
            .hasBody(emailContent()
                .line("A C2 application has been received for the case:")
                .line()
                .callout(calloutText)
                .line()
                .h1("Next steps")
                .line("You need to:")
                .list("check the C2",
                    "check payment has been taken",
                    "send a message to the judge or legal adviser",
                    "send a copy to relevant parties")
                .line()
                .end("To review the application, sign in to " + caseUrl)
            );
    }

}
