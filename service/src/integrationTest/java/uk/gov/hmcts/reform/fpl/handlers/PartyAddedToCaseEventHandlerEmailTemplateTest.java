package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeCaseRoleService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    PartyAddedToCaseEventHandler.class, PartyAddedToCaseContentProvider.class, EmailNotificationHelper.class,
    CaseUrlService.class, RepresentativeNotificationService.class, RepresentativeService.class,
    OtherRecipientsInbox.class
})
@MockBeans({
    @MockBean(CaseService.class), @MockBean(OrganisationService.class), @MockBean(RepresentativeCaseRoleService.class),
    @MockBean(ValidateEmailService.class), @MockBean(CaseRoleLookupService.class)
})
class PartyAddedToCaseEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String RESPONDENT_LAST_NAME = "Perturabo";
    private static final String CHILD_LAST_NAME = "Angron";

    @Autowired
    private PartyAddedToCaseEventHandler underTest;

    @Test
    void notifyParties() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("FAM_NUM")
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .representatives(wrapElements(
                Representative.builder().email("fake@email.com").servingPreferences(EMAIL).build(),
                Representative.builder().email("fake@email.com").servingPreferences(DIGITAL_SERVICE).build()
            ))
            .build();
        CaseData caseDataBefore = CaseData.builder().build();

        underTest.notifyParties(new PartyAddedToCaseEvent(caseData, caseDataBefore));

        List<SendEmailResponse> responses = allResponses();
        SendEmailResponse emailRepResponse = responses.get(0);
        SendEmailResponse digitalRepResponse = responses.get(1);

        assertThat(emailRepResponse)
            .hasSubject("You’ve been added to a case, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("You’ve been added to this case:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM")
                .line()
                .line("As requested, we'll email you when orders are issued.")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );

        assertThat(digitalRepResponse)
            .hasSubject("You’ve been added to a case, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("You’ve been added to this case:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM")
                .line()
                .line("To view the case, sign in to:")
                .line("http://fake-url/cases/case-details/12345")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For legal representation guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-respond-to-a-family-public-law-order-application")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }
}
