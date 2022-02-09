package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProviderTypeOfOrderCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderNotificationDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.ManageOrdersClosedCaseFieldGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraTitleGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryFinalMarker;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderLanguageRequirementGenerator;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.others.OthersNotifiedGenerator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    CaseManagementOrderIssuedEventHandler.class, CaseManagementOrderEmailContentProvider.class,
    EmailNotificationHelper.class, CaseUrlService.class, IssuedOrderAdminNotificationHandler.class,
    OrderIssuedEmailContentProvider.class, FixedTimeConfiguration.class, SealedOrderHistoryService.class,
    OrderIssuedEmailContentProviderTypeOfOrderCalculator.class, ChildrenSmartSelector.class,
    ChildSelectionUtils.class
})
@MockBeans({
    @MockBean(IdentityService.class), @MockBean(ChildrenService.class), @MockBean(OrderCreationService.class),
    @MockBean(ManageOrdersClosedCaseFieldGenerator.class), @MockBean(SealedOrderHistoryExtraTitleGenerator.class),
    @MockBean(SealedOrderHistoryTypeGenerator.class), @MockBean(SealedOrderHistoryFinalMarker.class),
    @MockBean(AppointedGuardianFormatter.class), @MockBean(SealedOrderLanguageRequirementGenerator.class),
    @MockBean(TranslationRequestService.class), @MockBean(OthersService.class), @MockBean(OtherRecipientsInbox.class),
    @MockBean(SendDocumentService.class), @MockBean(OthersNotifiedGenerator.class),
    @MockBean(OrderNotificationDocumentService.class), @MockBean(PlacementService.class)
})
class CaseManagementOrderIssuedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String RESPONDENT_LAST_NAME = "khorne";
    private static final String CHILD_LAST_NAME = "nurgle";
    private static final long CASE_ID = 123456L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";

    @Captor
    private ArgumentCaptor<OrderCafcassData> orderCafcassDataArgumentCaptor;
    @Captor
    private ArgumentCaptor<Set<DocumentReference>> documArgumentCaptor;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;
    @Autowired
    private CaseManagementOrderIssuedEventHandler underTest;

    public static final UUID HEARING_ID = UUID.randomUUID();
    public static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .hearingDetails(List.of(element(HEARING_ID, HearingBooking.builder()
            .startDate(LocalDateTime.of(2021, 6, 8, 0, 0, 0))
            .build())))
        .lastHearingOrderDraftsHearingId(HEARING_ID)
        .build();
    public static final HearingOrder CMO = HearingOrder.builder()
        .order(DocumentReference.builder().binaryUrl("/some-url/binary").filename("Test").build())
        .hearing("some hearing")
        .build();

    @Test
    void notifyLocalAuthority() {
        underTest.notifyLocalAuthority(new CaseManagementOrderIssuedEvent(CASE_DATA, CMO));

        assertThat(response())
            .hasSubject("CMO issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The case management order has been issued for:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", " + FAMILY_MAN_CASE_NUMBER + ", some hearing")
                .line()
                .h1("Next steps")
                .line()
                .line("You should now check the order to see your directions and compliance dates.")
                .line()
                .line("You can review the order by:")
                .list("signing into " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS))
                .line()
                .list("using this link http://fake-url/some-url/binary")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyParties() {
        underTest.notifyCafcass(new CaseManagementOrderIssuedEvent(CASE_DATA, CMO));

        assertThat(response())
            .hasSubject("CMO issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("The case management order has been issued for:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", " + FAMILY_MAN_CASE_NUMBER + ", some hearing")
                .line()
                .h1("Next steps")
                .line()
                .line("You should now check the order to see your directions and compliance dates.")
                .line()
                .line("You can review the order by:")
                .line()
                .line()
                .list("using this link " + GOV_NOTIFY_DOC_URL)
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyCtsc() {
        underTest.notifyAdmin(new CaseManagementOrderIssuedEvent(CASE_DATA, CMO));

        assertThat(response())
            .hasSubject("New case management order issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("A new case management order has been issued by Family Court")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM, hearing 8 Jun 2021")
                .line()
                .line("You should now check the order and do any required tasks or case updates.")
                .line()
                .line("You can review the order by:")
                .line()
                .list("signing into " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS),
                    "using this link http://fake-url/some-url/binary")
                .line()
                .line("HM Courts & Tribunal Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyCafcassViaSendGrid() {
        underTest.notifyCafcassViaSendGrid(new CaseManagementOrderIssuedEvent(CASE_DATA, CMO));

        verify(cafcassNotificationService).sendEmail(
                isA(CaseData.class),
                documArgumentCaptor.capture(),
                same(ORDER),
                orderCafcassDataArgumentCaptor.capture()
        );

        assertThat(documArgumentCaptor.getValue())
                .containsExactlyElementsOf(
                        Set.of(CMO.getOrder()));
        assertThat(orderCafcassDataArgumentCaptor.getValue()
                        .getDocumentName())
                .isEqualTo("Test");
    }
}
