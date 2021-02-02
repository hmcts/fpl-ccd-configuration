package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersRejectedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    DraftOrdersRejectedEventHandler.class,
    ReviewDraftOrdersEmailContentProvider.class,
    NotificationService.class,
    ObjectMapper.class,
    CaseUrlService.class
})
class DraftOrdersRejectedEmailTemplateTest extends EmailTemplateTest {

    @Autowired
    private DraftOrdersRejectedEventHandler underTest;

    @Test
    void notifyLA() {

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
            .requestedChanges("Please make these changes")
            .build();

        CaseData caseData = CaseData.builder()
            .id(100L)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .lastName("Smith")
                    .build())
                .build()))
            .hearingDetails(List.of(hearingBooking))
            .ordersToBeSent(List.of(element(c21)))
            .lastHearingOrderDraftsHearingId(hearingBooking.getId())
            .build();

        DraftOrdersRejected event = new DraftOrdersRejected(caseData, List.of(c21));

        underTest.sendNotificationToLA(event);

        assertThat(response())
            .hasSubject("Changes needed on draft orders, Smith")
            .hasBody(emailContent()
                .line("The judge has requested changes to the draft orders for:")
                .line()
                .callout("Smith, case management hearing, 1 February 2020")
                .line()
                .h1("Changes requested")
                .line()
                .line("Within 2 working days, you must make changes to these orders: "
                    + "\n\n* Test order - Please make these changes")
                .line()
                .line("Sign in to upload revised orders at http://fake-url/cases/case-details/100#Orders")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, "
                    + "call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
