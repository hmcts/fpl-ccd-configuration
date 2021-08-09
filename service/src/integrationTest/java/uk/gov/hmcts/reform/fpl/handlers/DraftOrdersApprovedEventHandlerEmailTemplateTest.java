package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersApprovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    DraftOrdersApprovedEventHandler.class, ReviewDraftOrdersEmailContentProvider.class, CaseUrlService.class,
    RepresentativeNotificationService.class, EmailNotificationHelper.class, OtherRecipientsInbox.class
})
@MockBeans({
    @MockBean(OtherRecipientsInbox.class), @MockBean(SendDocumentService.class)})
class DraftOrdersApprovedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String CHILD_LAST_NAME = "Jones";
    private static final String RESPONDENT_LAST_NAME = "Smith";

    @Autowired
    private DraftOrdersApprovedEventHandler underTest;

    @Test
    void notifyLAAndAdmin() {
        underTest.sendNotificationToAdminAndLA(buildEvent());

        allResponses().forEach(response ->
            assertThat(response)
                .hasSubject("New orders issued, " + CHILD_LAST_NAME)
                .hasBody(emailContent()
                    .line("New orders have been issued for:")
                    .line()
                    .callout("Smith, case management hearing, 1 February 2020")
                    .line()
                    .line("The orders are:")
                    .line()
                    .callout("Agreed CMO discussed at hearing\nTest order")
                    .line()
                    .h1("Next steps")
                    .line()
                    .line("You should now check the orders to see if you have any directions and compliance dates.")
                    .line()
                    .line("You can review the orders by:")
                    .list("signing into http://fake-url/cases/case-details/100#Orders", "using these links: ")
                    .line()
                    .list("http://fake-url/54321", "http://fake-url/99999")
                    .lines(23)
                    .line("HM Courts & Tribunals Service")
                    .line()
                    .end("Do not reply to this email. If you need to contact us, "
                        + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
                )
        );
    }

    @Test
    void notifyCafcass() {
        underTest.sendNotificationToCafcass(buildEvent());

        assertThat(response())
            .hasSubject("New orders issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("New orders have been issued for:")
                .line()
                .callout("Smith, case management hearing, 1 February 2020")
                .line()
                .line("The orders are:")
                .line()
                .callout("Agreed CMO discussed at hearing\nTest order")
                .line()
                .h1("Next steps")
                .line()
                .line("You should now check the orders to see if you have any directions and compliance dates.")
                .line()
                .line("You can review the orders by:")
                .line(" ")
                .list("using these links: ")
                .line()
                .line(GOV_NOTIFY_DOC_URL)
                .line()
                .line(GOV_NOTIFY_DOC_URL)
                .lines(19)
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyDigitalRepresentatives() {
        underTest.sendNotificationToDigitalRepresentatives(buildEvent());

        assertThat(response())
            .hasSubject("New orders issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("New orders have been issued for:")
                .line()
                .callout("Smith, case management hearing, 1 February 2020")
                .line()
                .line("The orders are:")
                .line()
                .callout("Agreed CMO discussed at hearing\nTest order")
                .line()
                .h1("Next steps")
                .line()
                .line("You should now check the orders to see if you have any directions and compliance dates.")
                .line()
                .line("You can review the orders by:")
                .list("signing into http://fake-url/cases/case-details/100#Orders", "using these links: ")
                .line()
                .list("http://fake-url/54321", "http://fake-url/99999")
                .lines(23)
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    private DraftOrdersApproved buildEvent() {
        Element<HearingBooking> hearingBooking = element(HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2020, Month.FEBRUARY, 1, 11, 11, 11))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Smith")
                .judgeEmailAddress("test@test.com")
                .build())
            .build());

        HearingOrder cmo = HearingOrder.builder()
            .type(AGREED_CMO)
            .title("Agreed CMO discussed at hearing")
            .order(DocumentReference.builder().binaryUrl("/54321").build())
            .build();

        HearingOrder c21 = HearingOrder.builder()
            .type(C21)
            .title("Test order")
            .order(DocumentReference.builder().binaryUrl("/99999").build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(100L)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .hearingDetails(List.of(hearingBooking))
            .ordersToBeSent(List.of(element(cmo), element(c21)))
            .lastHearingOrderDraftsHearingId(hearingBooking.getId())
            .build();

        return new DraftOrdersApproved(caseData, List.of(cmo, c21));
    }
}
