package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.events.ManagingOrganisationRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.service.ApplicantLocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ManagingOrganisationRemovedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.rd.model.Organisation;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    ObjectMapper.class,
    NotificationService.class,
    ManagingOrganisationRemovedEventHandler.class,
    ManagingOrganisationRemovedContentProvider.class,
    ApplicantLocalAuthorityService.class
})
class ManagingOrganisationRemovedEmailTemplateTest extends EmailTemplateTest {

    @MockBean
    private ValidateEmailService emailService;

    @MockBean
    private PbaNumberService pbaNumberService;

    @Autowired
    private ManagingOrganisationRemovedEventHandler underTest;

    @Test
    void notifyManagingOrganisation() {
        CaseData caseData = CaseData.builder()
            .id(111L)
            .caseName("Smith case")
            .caseLocalAuthorityName("Swansea City Council")
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .colleagues(wrapElements(Colleague.builder()
                    .email("colleague1@test.com")
                    .notificationRecipient("Yes")
                    .build()))
                .build()))
            .build();

        Organisation organisation = Organisation.builder().name("London Solicitors").build();

        ManagingOrganisationRemoved event = new ManagingOrganisationRemoved(caseData, organisation);

        underTest.notifyManagingOrganisation(event);

        assertThat(response())
            .hasSubject("FPL case access revoked")
            .hasBody(emailContent()
                .line("Dear London Solicitors")
                .line()
                .line("Swansea City Council has removed your organisation from the case:")
                .line()
                .line("Smith case 111")
                .line()
                .line("Your organisation no longer has online access to the case.")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email."
                    + " If you need to contact us, call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

}
