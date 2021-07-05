package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProviderTypeOfOrderCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.ManageOrdersClosedCaseFieldGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraOthersNotifiedGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryExtraTitleGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryFinalMarker;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    OrderIssuedEmailContentProvider.class, LocalAuthorityEmailContentProvider.class,
    IssuedOrderAdminNotificationHandler.class, NoticeOfPlacementOrderUploadedEventHandler.class,
    EmailNotificationHelper.class, CaseUrlService.class, FixedTimeConfiguration.class,
    OrderIssuedEmailContentProviderTypeOfOrderCalculator.class, SealedOrderHistoryService.class,
    RepresentativeNotificationService.class, ChildrenSmartSelector.class
})
@MockBeans({
    @MockBean(IdentityService.class), @MockBean(ChildrenService.class), @MockBean(OrderCreationService.class),
    @MockBean(ManageOrdersClosedCaseFieldGenerator.class),
    @MockBean(SealedOrderHistoryExtraTitleGenerator.class),
    @MockBean(SealedOrderHistoryTypeGenerator.class),
    @MockBean(AppointedGuardianFormatter.class),
    @MockBean(SealedOrderHistoryFinalMarker.class),
    @MockBean(OthersService.class),
    @MockBean(SealedOrderHistoryExtraOthersNotifiedGenerator.class)
})
public class NoticeOfPlacementOrderUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String RESPONDENT_LAST_NAME = "Bill";
    private static final String CHILD_LAST_NAME = "Ben";
    private static final DocumentReference NOTICE = DocumentReference.builder().binaryUrl("/blah/binary").build();
    private static final long CASE_ID = 12345L;
    @MockBean
    private FeatureToggleService toggleService;
    @Autowired
    private Time time;
    @Autowired
    private NoticeOfPlacementOrderUploadedEventHandler underTest;

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void notifyParties(boolean toggle, String name) {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .familyManCaseNumber("FAM_NUM")
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(time.now().plusDays(1)).build()))
            .build();
        underTest.notifyParties(new NoticeOfPlacementOrderUploadedEvent(caseData, NOTICE));

        List<SendEmailResponse> responses = allResponses();
        SendEmailResponse noticeOfPlacementResponse = responses.get(0);
        assertThat(noticeOfPlacementResponse)
            .hasSubject("New notice of placement order, " + name)
            .hasBody(emailContent()
                .line("A new notice of placement order has been issued.")
                .line()
                .line("To view it, sign into " + caseDetailsUrl(CASE_ID, TabUrlAnchor.PLACEMENT))
                .line()
                .line("Her Majesty’s Courts & Tribunal Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );

        SendEmailResponse orderIssuedResponse = responses.get(1);
        assertThat(orderIssuedResponse)
            .hasSubject("New notice of placement order issued, " + name)
            .hasBody(emailContent()
                .line("A new notice of placement order has been issued by Family Court")
                .lines(3)
                .line("You should now check the order and do any required tasks or case updates.")
                .line()
                .line("You can review the order by:")
                .line()
                .list("signing into " + caseDetailsUrl(CASE_ID, TabUrlAnchor.ORDERS),
                    "using this link http://fake-url/blah/binary")
                .line()
                .line("HM Courts & Tribunal Service")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    private static Stream<Arguments> subjectLineSource() {
        return Stream.of(
            Arguments.of(true, CHILD_LAST_NAME),
            Arguments.of(false, RESPONDENT_LAST_NAME)
        );
    }
}
