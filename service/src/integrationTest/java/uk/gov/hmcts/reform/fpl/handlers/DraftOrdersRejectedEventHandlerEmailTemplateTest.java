package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersRejectedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    DraftOrdersRejectedEventHandler.class, ReviewDraftOrdersEmailContentProvider.class, EmailNotificationHelper.class,
    CaseUrlService.class
})
class DraftOrdersRejectedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String CHILD_LAST_NAME = "Smith";
    private static final String RESPONDENT_LAST_NAME = "Baker";
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final long CASE_ID = 12345L;

    @Autowired
    private DraftOrdersRejectedEventHandler underTest;

    @Test
    void notifyLA() {
        UUID lastHearingId = UUID.randomUUID();

        List<Element<HearingBooking>> hearings = List.of(element(lastHearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.of(0, 0)))
            .build()));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()
            ))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()
            ))
            .lastHearingOrderDraftsHearingId(lastHearingId)
            .hearingDetails(hearings)
            .build();

        List<HearingOrder> rejectedOrders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .build());

        // TODO
        underTest.sendNotifications(new DraftOrdersRejected(caseData, rejectedOrders));

        assertThat(response())
            .hasSubject("Changes needed on draft orders, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The judge has requested changes to the draft orders for:")
                .line()
                .callout("Baker, FAM_NUM, case management hearing, 1 February 2021")
                .line()
                .h1("Changes requested")
                .line()
                .line("Within 2 working days, you must make changes to these orders: ")
                .line()
                .list("Order 1 - Missing information about XYZ", "Order 2 - Please change ABC")
                .line()
                .line("Sign in to upload revised orders at " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                     + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
