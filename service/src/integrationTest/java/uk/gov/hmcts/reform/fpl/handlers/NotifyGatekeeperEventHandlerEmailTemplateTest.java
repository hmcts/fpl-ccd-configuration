package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    NotifyGatekeeperEventHandler.class, GatekeeperEmailContentProvider.class, CaseUrlService.class,
    EmailNotificationHelper.class
})
public class NotifyGatekeeperEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final String CHILD_LAST_NAME = "Holmes";
    private static final EmailAddress GATEKEEPING_EMAIL = EmailAddress.builder().email("gatekeeper@email.com").build();
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(123L)
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .orders(Orders.builder().orderType(List.of(CARE_ORDER, SUPERVISION_ORDER)).build())
        .hearing(Hearing.builder().hearingUrgencyType(HearingUrgencyType.SAME_DAY).build())
        .gatekeeperEmails(wrapElements(GATEKEEPING_EMAIL))
        .build();

    @Autowired
    private NotifyGatekeeperEventHandler underTest;

    @Test
    void notifyGatekeeper() {
        underTest.notifyGatekeeper(new NotifyGatekeepersEvent(CASE_DATA));

        assertThat(response())
            .hasSubject("Urgent application – same day hearing, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .start()
                .line(LOCAL_AUTHORITY_NAME + " has made a new application for:")
                .lines(3)
                .list("Care order", "Supervision order")
                .line()
                .callout("Hearing date requested: same day")
                .line()
                .line("Respondent’s surname: " + RESPONDENT_LAST_NAME + ".")
                .line("CCD case number: 123.")
                .line()
                .line("Cafcass has been notified about this application.")
                .line()
                .h1("Next steps")
                .line("You now need to:")
                .list("check the application", "assign the level of judge", "draft the gatekeeping order")
                .line()
                .line("You can review the application by signing into http://fake-url/cases/case-details/123")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                     + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
