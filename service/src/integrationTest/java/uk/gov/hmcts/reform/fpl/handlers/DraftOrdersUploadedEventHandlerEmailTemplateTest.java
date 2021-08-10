package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersUploadedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.AgreedCMOUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {
    DraftOrdersUploadedEventHandler.class, DraftOrdersUploadedContentProvider.class,
    AgreedCMOUploadedContentProvider.class, CaseUrlService.class, EmailNotificationHelper.class
})
class DraftOrdersUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_LAST_NAME = "Smithson";
    private static final String CHILD_LAST_NAME = "Jones";

    @Autowired
    private DraftOrdersUploadedEventHandler underTest;

    @Test
    void notifyJudge() {
        CaseData caseData = getCaseData();

        underTest.sendNotificationToJudge(new DraftOrdersUploaded(caseData));

        assertThat(response())
            .hasSubject("New draft orders received, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("Dear Her Honour Judge Smith,")
                .line()
                .line("Draft orders have been received for:")
                .line()
                .callout("Smithson, case management hearing, 1 February 2020")
                .line()
                .line("The draft orders are:")
                .line()
                .callout("Agreed CMO discussed at hearing\nTest order")
                .line()
                .line("You should now check the orders by signing in to:")
                .line()
                .end("http://fake-url/cases/case-details/100#Draft%20orders")
            );
    }

    @Test
    void notifyAdmin() {
        CaseData caseData = getCaseData();

        underTest.sendNotificationToAdmin(new DraftOrdersUploaded(caseData));

        assertThat(response())
            .hasSubject("CMO sent for approval, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("Her Honour Judge Smith has been notified to approve the CMO for:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", case management hearing, 1 February 2020")
                .line()
                .line("To view the order, sign in to:")
                .end("http://fake-url/cases/case-details/100#Draft%20orders")
            );
    }

    private CaseData getCaseData() {
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
            .order(testDocumentReference())
            .build();

        HearingOrder c21 = HearingOrder.builder()
            .type(C21)
            .title("Test order")
            .order(testDocumentReference())
            .build();

        HearingOrdersBundle bundle = HearingOrdersBundle.builder()
            .hearingId(hearingBooking.getId())
            .orders(ElementUtils.wrapElements(cmo, c21))
            .build();

        return CaseData.builder()
            .id(100L)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().lastName(CHILD_LAST_NAME).dateOfBirth(LocalDate.now()).build())
                .build()))
            .hearingOrdersBundlesDrafts(wrapElements(bundle))
            .hearingDetails(List.of(hearingBooking))
            .lastHearingOrderDraftsHearingId(hearingBooking.getId())
            .build();
    }
}
