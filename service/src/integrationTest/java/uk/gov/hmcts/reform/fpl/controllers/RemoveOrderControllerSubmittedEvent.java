package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerSubmittedEvent extends AbstractControllerTest {
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String CASE_ID = "12345";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    private static final String REMOVAL_REASON = "The order was removed because incorrect data was entered";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
        .build();

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    RemoveOrderControllerSubmittedEvent() {
        super("remove-order");
    }

    @Test
    void shouldPublishPopulateSDOAndSDORemovedEventsIfNewSDOHasBeenRemoved() throws NotificationClientException {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().removalReason(REMOVAL_REASON).build();

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(
            emptyList(), singletonList(element(previousSDO)), emptyList());
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
            eq(SDO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(GATEKEEPER_EMAIL_ADDRESS),
            eqJson(expectedOrderRemovalTemplateParameters()),
            eq(NOTIFICATION_REFERENCE));
    }

    @Test
    void shouldNotPublishPopulateSDOAndSDORemovedEventsIfASDOHasNotBeenRemoved() {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().build();

        List<Element<StandardDirectionOrder>> hiddenSDOs = singletonList(element(previousSDO));
        List<Element<CaseManagementOrder>> hiddenCMOs = singletonList(element(CaseManagementOrder.builder().build()));

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
        List<Element<StandardDirectionOrder>> hiddenSDOs =
            singletonList(element(StandardDirectionOrder.builder().build()));
        List<Element<CaseManagementOrder>> hiddenCMOs = singletonList(element(CaseManagementOrder.builder().build()));

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
        Element<CaseManagementOrder> previousCMO =
            element(CaseManagementOrder.builder().removalReason(REMOVAL_REASON).build());

        List<Element<CaseManagementOrder>> hiddenCMOs = singletonList(previousCMO);

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, emptyList(), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), emptyList(), emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(CMO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(expectedOrderRemovalTemplateParameters()),
            eq(NOTIFICATION_REFERENCE));
    }

    @Test
    void shouldNotifyLocalAuthorityIfAnAdditionalCMOHasBeenRemoved() throws NotificationClientException {
        Element<CaseManagementOrder> removedCMO =
            element(CaseManagementOrder.builder().removalReason(REMOVAL_REASON).build());
        Element<CaseManagementOrder> previousCMO =
            element(CaseManagementOrder.builder().removalReason("test reason").build());

        List<Element<CaseManagementOrder>> hiddenCMOs = List.of(previousCMO, removedCMO);
        List<Element<CaseManagementOrder>> previouHiddenCMOs = singletonList(previousCMO);

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, emptyList(), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(previouHiddenCMOs, emptyList(), emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(CMO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(expectedOrderRemovalTemplateParameters()),
            eq(NOTIFICATION_REFERENCE));

        verifyNoMoreInteractions(coreCaseDataService);
    }

    private Map<String, Object> expectedOrderRemovalTemplateParameters() {
        OrderRemovalTemplate orderRemovalTemplate = OrderRemovalTemplate.builder()
            .caseReference(CASE_ID)
            .caseUrl("http://fake-url/cases/case-details/12345")
            .respondentLastName(RESPONDENT_LAST_NAME)
            .removalReason(REMOVAL_REASON)
            .build();
        return mapper.convertValue(orderRemovalTemplate, new TypeReference<>() {
        });
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type) {
        List<Element<Child>> childrenList = List.of(
            element(UUID.randomUUID(), Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build()));

        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .children(childrenList)
            .removalReason(REMOVAL_REASON)
            .build();
    }

    private CaseDetails caseDetailsWithRemovableOrders(
        List<Element<CaseManagementOrder>> hiddenCMOs,
        List<Element<StandardDirectionOrder>> hiddenSDOs,
        List<Element<GeneratedOrder>> hiddenOrders
    ) {
        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_ID))
            .hiddenOrders(hiddenOrders)
            .hiddenStandardDirectionOrders(hiddenSDOs)
            .hiddenCaseManagementOrders(hiddenCMOs)
            .respondents1(singletonList(element(RESPONDENT)))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .gatekeeperEmails(
                singletonList(element(EmailAddress.builder().email(GATEKEEPER_EMAIL_ADDRESS).build()))
            ).build();

        return CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(caseData.getId())
            .data(mapper.convertValue(caseData, new TypeReference<>() {
            })).build();
    }
}
