package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    PlacementApplicationEventHandler.class, PlacementApplicationContentProvider.class, EmailNotificationHelper.class,
    CaseUrlService.class
})
class PlacementApplicationEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final long CASE_ID = 12345L;
    private static final String CHILD_LAST_NAME = "should ask for other names";

    @Autowired
    private PlacementApplicationEventHandler underTest;

    @Test
    void notifyData() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .build();

        underTest.notifyAdmin(new PlacementApplicationEvent(caseData));

        assertThat(response())
            .hasSubject("New placement application, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("A placement application has been made for " + caseDetailsUrl(CASE_ID, TabUrlAnchor.PLACEMENT))
                .line()
                .line("You should now:")
                .line()
                .list("check the application", "send it to the judge or legal advisor",
                    "send documents to relevant parties")
                .line()
                .line("Her Majesty’s Courts & Tribunal Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }
}
