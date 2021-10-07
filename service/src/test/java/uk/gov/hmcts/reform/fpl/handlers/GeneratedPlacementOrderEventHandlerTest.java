package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedPlacementOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementOrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_ADMIN_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.PRIVATE_SOLICITOR_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FAMILY_MAN_NUMBER;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_FIRST;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_SECOND;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentedRespondentWithAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRespondent;

@ExtendWith(MockitoExtension.class)
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
    void shouldEmailPlacementOrderToRelevantParties() {
        when(localAuthorityRecipients.getRecipients(RecipientsRequest.builder().caseData(basicCaseData).build()))
            .thenReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        Child child = Child.builder().build();
        when(sealedOrderHistoryService.lastGeneratedOrder(any()))
            .thenReturn(GeneratedOrder.builder().children(wrapElements(child)).build());
        PlacementOrderIssuedNotifyData notifyData = mock(PlacementOrderIssuedNotifyData.class);
        when(orderIssuedEmailContentProvider.getNotifyDataForPlacementOrder(basicCaseData, ORDER_DOCUMENT, child))
            .thenReturn(notifyData);
        when(courtService.getCourtEmail(basicCaseData)).thenReturn(DEFAULT_ADMIN_EMAIL);
        when(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_1_CODE))
            .thenReturn(new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS));

        underTest.sendPlacementOrderEmail(
            new GeneratedPlacementOrderEvent(basicCaseData,
                ORDER_DOCUMENT,
                ORDER_NOTIFICATION_DOCUMENT));

        verify(notificationService).sendEmail(
            PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS, DEFAULT_ADMIN_EMAIL, CAFCASS_EMAIL_ADDRESS),
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
                        .noticeDocuments(wrapElements(
                            PlacementNoticeDocument.builder()
                                .respondentId(anotherRespondent.getId())
                                .type(CAFCASS)
                                .build(),
                            PlacementNoticeDocument.builder()
                                .respondentId(mother.getId())
                                .type(PARENT_SECOND)
                                .build(),
                            PlacementNoticeDocument.builder()
                                .respondentId(father.getId())
                                .type(PARENT_FIRST)
                                .build()
                        )).build()
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
        Element<Respondent> anotherRespondent = testRespondent("Someone", "Else");
        Element<Respondent> father = testRespondent("Father", "Jones");
        Element<Respondent> mother = testRespondent("Mother", "Jones");
        Element<Child> child = element(Child.builder().build());
        CaseData caseData = basicCaseData.toBuilder()
            .respondents1(List.of(anotherRespondent, father, mother))
            .children1(List.of(child))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(
                    Placement.builder()
                        .childId(child.getId())
                        .noticeDocuments(wrapElements(
                            PlacementNoticeDocument.builder()
                                .respondentId(anotherRespondent.getId())
                                .type(CAFCASS)
                                .build(),
                            PlacementNoticeDocument.builder()
                                .respondentId(mother.getId())
                                .type(PARENT_SECOND)
                                .build(),
                            PlacementNoticeDocument.builder()
                                .respondentId(father.getId())
                                .type(PARENT_FIRST)
                                .build()
                        )).build()
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

}
