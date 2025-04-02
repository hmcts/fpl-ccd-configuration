package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@ContextConfiguration(classes = {PlacementEventsHandler.class, PlacementContentProvider.class,
    EmailNotificationHelper.class, CaseUrlService.class
})
@MockBeans({
    @MockBean(UserService.class),
    @MockBean(PaymentService.class),
    @MockBean(CoreCaseDataService.class),
    @MockBean(EventService.class),
    @MockBean(Time.class),
    @MockBean(SendDocumentService.class),
    @MockBean(CafcassNotificationService.class)
})
class PlacementApplicationEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final long CASE_ID = 12345L;

    private final Element<Child> child = testChild("Alex", "Green");

    private final DocumentReference noticeDocument = DocumentReference.builder()
        .filename("doc.pdf")
        .url("http://dm-store/100")
        .binaryUrl("http://dm-store/100/binary")
        .build();


    private final Placement placement = Placement.builder()
        .childId(child.getId())
        .childName(child.getValue().asLabel())
        .placementNotice(noticeDocument)
        .build();

    private final CaseData caseData = CaseData.builder()
        .id(CASE_ID)
        .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
        .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
        .children1(List.of(child))
        .build();

    @Autowired
    private PlacementEventsHandler underTest;

    @Test
    void courtNotification() {

        underTest.notifyCourtOfNewApplication(new PlacementApplicationSubmitted(caseData, placement));

        assertThat(response())
            .hasSubject("New placement application, Alex Green")
            .hasBody(emailContent()
                .line(format("A new or amended placement application has been issued by the %s, "
                    + "or a response has been provided.", LOCAL_AUTHORITY_1_NAME))
                .line()
                .line(format("To view it, sign into %s", caseDetailsUrl(CASE_ID, PLACEMENT)))
                .line()
                .line("You should now:")
                .list("check the application",
                    "send it to the judge or legal advisor",
                    "send documents to relevant parties")
                .line()
                .line(FOOTER_HEADER)
                .line()
                .end(FOOTER_CONTACT_DETAILS));
    }

    @Test
    void localAuthorityNotification() {

        underTest.notifyLocalAuthorityOfNewNotice(new PlacementNoticeAdded(caseData, placement));

        assertThat(response())
            .hasSubject("New notice of placement order, Alex Green")
            .hasBody(emailContent()
                .line(format("A new or amended notice of placement has been issued by the %s, "
                    + "or a response has been provided.", LOCAL_AUTHORITY_1_NAME))
                .line()
                .line(format("To view it, sign into %s", caseDetailsUrl(CASE_ID, PLACEMENT)))
                .line()
                .line(FOOTER_HEADER)
                .line()
                .end(FOOTER_CONTACT_DETAILS));
    }

    @Test
    void cafcassNotification() {

        underTest.notifyCafcassOfNewNotice(new PlacementNoticeAdded(caseData, placement));

        assertThat(response())
            .hasSubject("New notice of placement order, Alex Green")
            .hasBody(emailContent()
                .line(format("A new or amended notice of placement has been issued by the %s, "
                    + "or a response has been provided.", LOCAL_AUTHORITY_1_NAME))
                .line()
                .line("CCD case number: " + CASE_ID)
                .line()
                .h1("Next steps")
                .list("You can now start to prepare for the hearing",
                    "You can review the application by using link http://fake-url/100/binary")
                .line("or")
                .line(GOV_NOTIFY_DOC_URL)
                .line()
                .line(FOOTER_HEADER)
                .line()
                .end(FOOTER_CONTACT_DETAILS));
    }
}
