package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedPlacementOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementOrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_ADMIN_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.PRIVATE_SOLICITOR_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FAMILY_MAN_NUMBER;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentedRespondentWithAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRespondent;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class GeneratedPlacementOrderEventHandlerTest {

    private static final DocumentReference ORDER_DOCUMENT = testDocumentReference();
    private static final DocumentReference ORDER_NOTIFICATION_DOCUMENT = testDocumentReference();

    private static final String FATHER_SOLICITOR_EMAIL = "father-solicitor@example.com";
    private static final String MOTHER_SOLICITOR_EMAIL = "mother-solicitor@example.com";

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @Mock
    private SealedOrderHistoryService sealedOrderHistoryService;

    @Mock
    private CourtService courtService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private SendDocumentService sendDocumentService;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private UserService userService;

    @Mock
    private WorkAllocationTaskService workAllocationTaskService;

    @Mock
    private JudicialService judicialService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private GeneratedPlacementOrderEventHandler underTest;

    private CaseData basicCaseData;

    @BeforeEach
    void setUp() {
        basicCaseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .familyManCaseNumber(TEST_FAMILY_MAN_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .build();
    }

    @Test
    void shouldCleanupRolesIfCaseClosed() {
        CaseData data = basicCaseData.toBuilder().state(State.CLOSED).build();
        underTest.cleanupRoles(new GeneratedPlacementOrderEvent(data, ORDER_DOCUMENT, ORDER_NOTIFICATION_DOCUMENT));
        verify(judicialService).deleteAllRolesOnCase(TEST_CASE_ID);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = { "CLOSED" }, mode = EnumSource.Mode.EXCLUDE)
    void shouldNotCleanupRolesIfCaseIsNotClosed(State state) {
        CaseData data = basicCaseData.toBuilder().state(state).build();
        underTest.cleanupRoles(new GeneratedPlacementOrderEvent(data, ORDER_DOCUMENT, ORDER_NOTIFICATION_DOCUMENT));
        verify(judicialService, never()).deleteAllRolesOnCase(TEST_CASE_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldEmailPlacementOrderToRelevantParties(boolean isCtscEmailEnabled) {
        when(localAuthorityRecipients.getRecipients(RecipientsRequest.builder().caseData(basicCaseData).build()))
            .thenReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        Child child = Child.builder().build();
        when(sealedOrderHistoryService.lastGeneratedOrder(any()))
            .thenReturn(GeneratedOrder.builder().children(wrapElements(child)).build());
        PlacementOrderIssuedNotifyData notifyData = mock(PlacementOrderIssuedNotifyData.class);
        when(orderIssuedEmailContentProvider.getNotifyDataForPlacementOrder(basicCaseData, ORDER_DOCUMENT, child))
            .thenReturn(notifyData);
        when(courtService.getCourtEmail(basicCaseData)).thenReturn(DEFAULT_ADMIN_EMAIL);
        when(cafcassLookupConfiguration.getCafcassWelsh(LOCAL_AUTHORITY_1_CODE))
            .thenReturn(Optional.of(new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE,
                CAFCASS_EMAIL_ADDRESS)));
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(isCtscEmailEnabled);

        underTest.sendPlacementOrderEmail(
            new GeneratedPlacementOrderEvent(basicCaseData,
                ORDER_DOCUMENT,
                ORDER_NOTIFICATION_DOCUMENT));

        Set<String> expectedRecipients = Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS, CAFCASS_EMAIL_ADDRESS);
        if (isCtscEmailEnabled) {
            expectedRecipients =  Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS, DEFAULT_ADMIN_EMAIL, CAFCASS_EMAIL_ADDRESS);
        }

        verify(notificationService).sendEmail(
            PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE,
            expectedRecipients,
            notifyData,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendOrderNotification_ToRepresentedParentsSolicitorByEmail_And_ToRepresentedChildSolicitorByEmail() {
        Element<Respondent> anotherRespondent = testRespondent("Someone", "Else");
        Element<Respondent> father = testRepresentedRespondentWithAddress("Father", "Jones", FATHER_SOLICITOR_EMAIL);
        Element<Respondent> mother = testRepresentedRespondentWithAddress("Mother", "Jones", MOTHER_SOLICITOR_EMAIL);
        Element<Child> child = element(Child.builder()
            .solicitor(RespondentSolicitor.builder().email(PRIVATE_SOLICITOR_USER_EMAIL).build())
            .build());
        CaseData caseData = basicCaseData.toBuilder()
            .respondents1(List.of(anotherRespondent, father, mother))
            .children1(List.of(child))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(
                    Placement.builder()
                        .childId(child.getId())
                        .placementRespondentsToNotify(List.of(anotherRespondent, father, mother))
                        .build()
                )).build()
            ).build();
        when(sealedOrderHistoryService.lastGeneratedOrder(any()))
            .thenReturn(GeneratedOrder.builder().children(List.of(child)).build());
        PlacementOrderIssuedNotifyData notifyData = mock(PlacementOrderIssuedNotifyData.class);
        when(orderIssuedEmailContentProvider.getNotifyDataForPlacementOrder(caseData,
            ORDER_NOTIFICATION_DOCUMENT,
            child.getValue())).thenReturn(notifyData);

        underTest.sendPlacementOrderNotification(new GeneratedPlacementOrderEvent(caseData,
            ORDER_DOCUMENT,
            ORDER_NOTIFICATION_DOCUMENT));

        verify(notificationService).sendEmail(
            PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE,
            Set.of(FATHER_SOLICITOR_EMAIL, MOTHER_SOLICITOR_EMAIL, PRIVATE_SOLICITOR_USER_EMAIL),
            notifyData,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendOrderNotification_ToUnrepresentedParentsByPost_And_NotToUnrepresentedChildSolicitorByEmail() {
        Element<Respondent> father = testRespondent("Father", "Jones");
        Element<Respondent> mother = testRespondent("Mother", "Jones");
        Element<Child> child = element(Child.builder().build());
        CaseData caseData = basicCaseData.toBuilder()
            .respondents1(List.of(father, mother))
            .children1(List.of(child))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(
                    Placement.builder()
                        .childId(child.getId())
                        .placementRespondentsToNotify(List.of(mother, father))
                        .build()
                )).build()
            ).build();
        when(sealedOrderHistoryService.lastGeneratedOrder(any()))
            .thenReturn(GeneratedOrder.builder().children(List.of(child)).build());
        PlacementOrderIssuedNotifyData notifyData = mock(PlacementOrderIssuedNotifyData.class);
        when(orderIssuedEmailContentProvider.getNotifyDataForPlacementOrder(caseData,
            ORDER_NOTIFICATION_DOCUMENT,
            child.getValue())).thenReturn(notifyData);

        underTest.sendPlacementOrderNotification(new GeneratedPlacementOrderEvent(caseData,
            ORDER_DOCUMENT,
            ORDER_NOTIFICATION_DOCUMENT));

        verify(sendDocumentService).sendDocuments(caseData,
            List.of(ORDER_NOTIFICATION_DOCUMENT),
            List.of(mother.getValue().getParty(), father.getValue().getParty()));
        verify(notificationService).sendEmail(
            PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE,
            emptySet(),
            notifyData,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendCafcassEnglandOrderOverSendgrid() {
        Element<Respondent> father = testRespondent("Father", "Jones");
        Element<Respondent> mother = testRespondent("Mother", "Jones");
        Element<Child> child = element(Child.builder().build());
        CaseData caseData = basicCaseData.toBuilder()
            .respondents1(List.of(father, mother))
            .children1(List.of(child))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(
                    Placement.builder()
                        .childId(child.getId())
                        .placementRespondentsToNotify(List.of(mother, father))
                        .build()
                )).build()
            ).build();

        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        underTest.sendPlacementOrderEmailToCafcassEngland(new GeneratedPlacementOrderEvent(caseData,
            ORDER_DOCUMENT,
            ORDER_NOTIFICATION_DOCUMENT));

        verify(cafcassNotificationService).sendEmail(caseData,
            Set.of(ORDER_DOCUMENT, ORDER_NOTIFICATION_DOCUMENT),
            CafcassRequestEmailContentProvider.ORDER,
            OrderCafcassData.builder()
                .documentName(ORDER_DOCUMENT.getFilename())
                .build());
    }

    @Test
    void shouldCreateWorkAllocationTaskWhenJudgeUploadsOrder() {
        given(userService.isJudiciaryUser()).willReturn(true);
        underTest.createWorkAllocationTask(new GeneratedPlacementOrderEvent(basicCaseData,
            ORDER_DOCUMENT,
            ORDER_NOTIFICATION_DOCUMENT));

        verify(workAllocationTaskService).createWorkAllocationTask(basicCaseData,
            WorkAllocationTaskType.ORDER_UPLOADED);
    }

    @Test
    void shouldNotCreateWorkAllocationTaskWhenNonJudgeUserUploadsOrder() {
        given(userService.isJudiciaryUser()).willReturn(false);
        underTest.createWorkAllocationTask(new GeneratedPlacementOrderEvent(basicCaseData,
            ORDER_DOCUMENT,
            ORDER_NOTIFICATION_DOCUMENT));

        verify(workAllocationTaskService, never()).createWorkAllocationTask(any(),
            any());
    }
}
