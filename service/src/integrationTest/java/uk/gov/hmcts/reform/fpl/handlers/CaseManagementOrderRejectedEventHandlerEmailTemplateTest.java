package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    CaseManagementOrderRejectedEventHandler.class, CaseManagementOrderEmailContentProvider.class,
    EmailNotificationHelper.class, CaseUrlService.class
})
class CaseManagementOrderRejectedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String CHILD_LAST_NAME = "slaanesh";
    private static final String RESPONDENT_LAST_NAME = "tzeentch";

    @Autowired
    private CaseManagementOrderRejectedEventHandler underTest;

    @Test
    void notifyLocalAuthority() {
        String familyManCaseNumber = "fam_num";
        long caseId = 12345L;
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .familyManCaseNumber(familyManCaseNumber)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .build();

        String hearing = "some hearing here";
        String changes = "something must be changed";
        HearingOrder cmo = HearingOrder.builder()
            .requestedChanges(changes)
            .hearing(hearing)
            .build();

        underTest.notifyLocalAuthority(new CaseManagementOrderRejectedEvent(caseData, cmo));

        assertThat(response())
            .hasSubject("Changes needed on CMO, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The judge has requested changes to the CMO for:")
                .line()
                .callout(String.format("%s, %s, %s", RESPONDENT_LAST_NAME, familyManCaseNumber, hearing))
                .line()
                .line("Within 2 working days, you must make these changes:")
                .line()
                .line(changes)
                .line()
                .line("Sign in to upload a revised CMO: " + caseDetailsUrl(caseId, TabUrlAnchor.ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }
}
