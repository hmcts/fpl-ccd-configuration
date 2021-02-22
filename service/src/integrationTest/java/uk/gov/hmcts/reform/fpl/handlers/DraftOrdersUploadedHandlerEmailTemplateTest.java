package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.handlers.cmo.DraftOrdersUploadedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.AgreedCMOUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersUploadedContentProvider;
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
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@SpringBootTest(classes = {
    DraftOrdersUploadedEventHandler.class,
    DraftOrdersUploadedContentProvider.class,
    AgreedCMOUploadedContentProvider.class,
    NotificationService.class,
    ObjectMapper.class,
    CaseUrlService.class
})
class DraftOrdersUploadedHandlerEmailTemplateTest extends EmailTemplateTest {

    @Autowired
    private DraftOrdersUploadedEventHandler underTest;

    @Test
    void notifyJudge() {

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

        CaseData caseData = CaseData.builder()
            .id(100L)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .lastName("Smith")
                    .build())
                .build()))
            .hearingOrdersBundlesDrafts(wrapElements(bundle))
            .hearingDetails(List.of(hearingBooking))
            .lastHearingOrderDraftsHearingId(hearingBooking.getId())
            .build();

        DraftOrdersUploaded event = new DraftOrdersUploaded(caseData);

        underTest.sendNotificationToJudge(event);

        assertThat(response())
            .hasSubject("New draft orders received, Smith")
            .hasBody(emailContent()
                .line("Dear Her Honour Judge Smith,")
                .line()
                .line("Draft orders have been received for:")
                .line()
                .callout("Smith, case management hearing, 1 February 2020")
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
}
