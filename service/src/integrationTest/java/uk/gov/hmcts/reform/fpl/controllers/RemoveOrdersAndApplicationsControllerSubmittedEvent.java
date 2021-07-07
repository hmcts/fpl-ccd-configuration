package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@WebMvcTest(RemoveOrdersAndApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrdersAndApplicationsControllerSubmittedEvent extends AbstractCallbackTest {
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String CASE_ID = "12345";
    private static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    private static final String REMOVAL_REASON = "The order was removed because incorrect data was entered";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
        .build();
    private static final String CHILD_LAST_NAME = "Jones";
    private static final Child CHILD = Child.builder()
        .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
        .build();

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private FeatureToggleService toggleService;

    RemoveOrdersAndApplicationsControllerSubmittedEvent() {
        super("remove-order");
    }

    @BeforeEach
    void setUp() {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(true);
    }

    @Test
    void shouldPublishPopulateSDOAndSDORemovedEventsIfNewSDOHasBeenRemoved() throws NotificationClientException {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().removalReason(REMOVAL_REASON).build();

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(emptyList(), wrapElements(previousSDO), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), emptyList(), emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(12345L),
            eq("populateSDO"),
            anyMap());

        verify(notificationClient).sendEmail(
            eq(SDO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(GATEKEEPER_EMAIL_ADDRESS),
            eqJson(expectedOrderRemovalTemplateParameters()),
            eq(NOTIFICATION_REFERENCE));
    }

    @Test
    void shouldPublishPopulateSDOAndSDORemovedEventsIfAnAdditionalSDOHasBeenRemoved()
        throws NotificationClientException {
        Element<StandardDirectionOrder> newSDO =
            element(StandardDirectionOrder.builder().removalReason(REMOVAL_REASON).build());
        Element<StandardDirectionOrder> previousSDO =
            element(StandardDirectionOrder.builder().removalReason("test reason").build());

        List<Element<StandardDirectionOrder>> previousHiddenSDOs = singletonList(previousSDO);
        List<Element<StandardDirectionOrder>> hiddenSDOs = List.of(previousSDO, newSDO);

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(emptyList(), hiddenSDOs, emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), previousHiddenSDOs, emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(12345L),
            eq("populateSDO"),
            anyMap());

        verify(notificationClient).sendEmail(
            SDO_REMOVAL_NOTIFICATION_TEMPLATE,
            GATEKEEPER_EMAIL_ADDRESS,
            expectedOrderRemovalTemplateParameters(),
            NOTIFICATION_REFERENCE
        );
    }

    @Test
    void shouldNotPublishPopulateSDOAndSDORemovedEventsIfASDOHasNotBeenRemoved() {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().build();

        List<Element<StandardDirectionOrder>> hiddenSDOs = singletonList(element(previousSDO));
        List<Element<HearingOrder>> hiddenCMOs = singletonList(element(HearingOrder.builder().build()));

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, hiddenSDOs, emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), hiddenSDOs, emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verifyNoMoreInteractions(coreCaseDataService);
        checkThat(() -> verify(notificationClient, never())
            .sendEmail(eq(SDO_REMOVAL_NOTIFICATION_TEMPLATE), any(), any(), any()));
    }

    @ParameterizedTest
    @EnumSource(
        value = GeneratedOrderType.class,
        names = {"BLANK_ORDER", "EMERGENCY_PROTECTION_ORDER", "CARE_ORDER", "SUPERVISION_ORDER"}
    )
    void shouldNotSendNotificationsIfAGeneratedOrderIsRemoved(GeneratedOrderType generatedOrderType) {
        Element<GeneratedOrder> removedOrder = element(buildOrder(generatedOrderType));
        Element<GeneratedOrder> order = element(buildOrder(generatedOrderType));

        List<Element<GeneratedOrder>> hiddenOrders = List.of(order, removedOrder);
        List<Element<GeneratedOrder>> previousHiddenOrders = singletonList(order);
        List<Element<StandardDirectionOrder>> hiddenSDOs = wrapElements(StandardDirectionOrder.builder().build());
        List<Element<HearingOrder>> hiddenCMOs = wrapElements(HearingOrder.builder().build());

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, hiddenSDOs, hiddenOrders);
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(hiddenCMOs, hiddenSDOs, previousHiddenOrders);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldNotifyLocalAuthorityIfACMOIsRemoved() throws NotificationClientException {
        Element<HearingOrder> previousCMO =
            element(HearingOrder.builder().removalReason(REMOVAL_REASON).build());

        List<Element<HearingOrder>> hiddenCMOs = singletonList(previousCMO);

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, emptyList(), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), emptyList(), emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            CMO_REMOVAL_NOTIFICATION_TEMPLATE,
            LOCAL_AUTHORITY_1_INBOX,
            expectedOrderRemovalTemplateParameters(),
            NOTIFICATION_REFERENCE
        );
    }

    @Test
    void shouldNotifyLocalAuthorityIfAnAdditionalCMOHasBeenRemoved() throws NotificationClientException {
        Element<HearingOrder> removedCMO =
            element(HearingOrder.builder().removalReason(REMOVAL_REASON).build());
        Element<HearingOrder> previousCMO =
            element(HearingOrder.builder().removalReason("test reason").build());

        List<Element<HearingOrder>> hiddenCMOs = List.of(previousCMO, removedCMO);
        List<Element<HearingOrder>> previouHiddenCMOs = singletonList(previousCMO);

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, emptyList(), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(previouHiddenCMOs, emptyList(), emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            CMO_REMOVAL_NOTIFICATION_TEMPLATE,
            LOCAL_AUTHORITY_1_INBOX,
            expectedOrderRemovalTemplateParameters(),
            NOTIFICATION_REFERENCE
        );

        verifyNoMoreInteractions(coreCaseDataService);
    }

    private Map<String, Object> expectedOrderRemovalTemplateParameters() {
        OrderRemovalTemplate orderRemovalTemplate = OrderRemovalTemplate.builder()
            .caseReference(CASE_ID)
            .caseUrl("http://fake-url/cases/case-details/12345")
            .lastName(CHILD_LAST_NAME)
            .removalReason(REMOVAL_REASON)
            .build();
        return mapper.convertValue(orderRemovalTemplate, new TypeReference<>() {});
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type) {
        List<Element<Child>> childrenList = wrapElements(Child.builder()
            .finalOrderIssued("Yes")
            .finalOrderIssuedType("Some type")
            .party(ChildParty.builder().build())
            .build()
        );

        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .children(childrenList)
            .removalReason(REMOVAL_REASON)
            .build();
    }

    private CaseDetails caseDetailsWithRemovableOrders(List<Element<HearingOrder>> hiddenCMOs,
                                                       List<Element<StandardDirectionOrder>> hiddenSDOs,
                                                       List<Element<GeneratedOrder>> hiddenOrders) {
        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_ID))
            .hiddenOrders(hiddenOrders)
            .hiddenStandardDirectionOrders(hiddenSDOs)
            .hiddenCaseManagementOrders(hiddenCMOs)
            .respondents1(wrapElements(RESPONDENT))
            .children1(wrapElements(CHILD))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .gatekeeperEmails(wrapElements(EmailAddress.builder().email(GATEKEEPER_EMAIL_ADDRESS).build()))
            .build();

        return CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(caseData.getId())
            .data(mapper.convertValue(caseData, new TypeReference<>() {})).build();
    }
}
