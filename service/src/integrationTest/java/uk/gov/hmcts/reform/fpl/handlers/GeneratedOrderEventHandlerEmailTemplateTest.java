package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
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
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    IssuedOrderAdminNotificationHandler.class, OrderIssuedEmailContentProvider.class, FixedTimeConfiguration.class,
    OrderIssuedEmailContentProviderTypeOfOrderCalculator.class, EmailNotificationHelper.class,
    SealedOrderHistoryService.class, CaseUrlService.class, GeneratedOrderEventHandler.class,
    RepresentativeNotificationService.class, ChildrenSmartSelector.class, ChildSelectionUtils.class
})
@MockBeans({
    // All but the feature toggle service are only mocked because they are dependencies that aren't used
    @MockBean(ChildrenService.class), @MockBean(IdentityService.class), @MockBean(OrderCreationService.class),
    @MockBean(SendDocumentService.class), @MockBean(SealedOrderHistoryExtraTitleGenerator.class),
    @MockBean(SealedOrderLanguageRequirementGenerator.class),
    @MockBean(TranslationRequestService.class),
    @MockBean(SealedOrderHistoryTypeGenerator.class), @MockBean(SealedOrderHistoryFinalMarker.class),
    @MockBean(ManageOrdersClosedCaseFieldGenerator.class), @MockBean(AppointedGuardianFormatter.class),
    @MockBean(OthersService.class), @MockBean(OthersNotifiedGenerator.class), @MockBean(OtherRecipientsInbox.class),
    @MockBean(OrderNotificationDocumentService.class), @MockBean(PlacementService.class),
    @MockBean(CafcassNotificationService.class), @MockBean(UserService.class),
    @MockBean(WorkAllocationTaskService.class), @MockBean(JudicialService.class)
})
class GeneratedOrderEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final GeneratedOrder ORDER = mock(GeneratedOrder.class);
    private static final DocumentReference ORDER_DOCUMENT = mock(DocumentReference.class);
    private static final String BINARY_URL = "/documents/some-random-string/binary";
    private static final long CASE_ID = 12345L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FAM_NUM";
    private static final String CHILD_LAST_NAME = "Smith";
    private static final String RESPONDENT_LAST_NAME = "Jones";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder()
                .lastName(CHILD_LAST_NAME)
                .dateOfBirth(LocalDate.now())
                .build())
            .build()))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder()
                .lastName(RESPONDENT_LAST_NAME)
                .build())
            .build()))
        .orderCollection(wrapElements(ORDER))
        .build();
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENT =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;
    private static final String ORDER_TITLE = "orderTitle";

    @Autowired
    private GeneratedOrderEventHandler underTest;

    @BeforeEach
    void mocks() {
        when(ORDER.isNewVersion()).thenReturn(true);
        when(ORDER.getType()).thenReturn("Care order");
        when(ORDER_DOCUMENT.getBinaryUrl()).thenReturn(BINARY_URL);
    }

    @Test
    void notifyParties() {
        underTest.notifyParties(new GeneratedOrderEvent(CASE_DATA, ORDER_DOCUMENT, TRANSLATION_REQUIREMENT,
            ORDER_TITLE, LocalDate.now()));

        SendEmailResponse adminResponse = response();
        SendEmailResponse laResponse = response();
        SendEmailResponse notifyRepResponse = response();
        SendEmailResponse emailRepResponse = response();

        assertThat(adminResponse)
            .hasSubject("New care order issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("A new care order has been issued by " + COURT_NAME)
                .line()
                .callout("Jones, FAM_NUM")
                .line()
                .line("You should now check the order and do any required tasks or case updates.")
                .line()
                .line("You can review the order by:")
                .line()
                .list("signing into http://fake-url/cases/case-details/12345#Orders",
                    "using this link http://fake-url/documentsv2/some-random-string/binary")
                .line()
                .line("HM Courts & Tribunal Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );

        assertThat(laResponse)
            .hasSubject("New care order issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("A new care order has been issued by " + COURT_NAME + " for:")
                .line()
                .callout("Jones, FAM_NUM")
                .line()
                .line("You can review the order by:")
                .line()
                .list("signing into http://fake-url/cases/case-details/12345#Orders")
                .line()
                .line("HM Courts & Tribunal Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .line("For legal representation guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-respond-to-a-family-public-law-order-application")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );

        assertThat(notifyRepResponse)
            .hasSubject("New care order issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("A new care order has been issued by " + COURT_NAME + " for:")
                .line()
                .callout("Jones, FAM_NUM")
                .line()
                .line("You can review the order by:")
                .line()
                .list("signing into http://fake-url/cases/case-details/12345#Orders")
                .line()
                .line("HM Courts & Tribunal Service")
                .line()
                .line("For local authority guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-apply-for-a-family-public-law-order")
                .line()
                .line("For legal representation guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-respond-to-a-family-public-law-order-application")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );

        assertThat(emailRepResponse)
            .hasSubject("New care order issued, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("A new care order has been issued by " + COURT_NAME)
                .line()
                .callout("Jones, FAM_NUM")
                .line()
                .line("Download it at " + GOV_NOTIFY_DOC_URL)
                .line()
                .line("HM Courts & Tribunal Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }
}
