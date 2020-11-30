package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Disabled;
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
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerSubmittedTest extends AbstractControllerTest {

    private static final long CASE_ID = 12345L;
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    private static final DocumentReference DOCUMENT_REFERENCE = TestDataHelper.testDocumentReference();
    private static final String REMOVAL_REASON = "The order was removed because incorrect data was entered";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    public static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
        .build();

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    RemoveOrderControllerSubmittedTest() {
        super("remove-order");
    }

    @Test
    void shouldNotSendNotificationsWhenBlankOrderIsRemoved() {
        Element<CaseManagementOrder> cmo = buildApprovedCMOElement(false);

        Element<GeneratedOrder> blankOrder = element(buildBlankOrder());

        CaseDetails caseDetailsBefore = buildCaseDetails(singletonList(blankOrder), singletonList(cmo), null);
        CaseDetails caseDetailsAfter = buildCaseDetails(emptyList(), singletonList(cmo), null);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetailsAfter)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(notificationClient, never()).sendEmail(any(), any(), any(), any()));
    }

    @ParameterizedTest
    @EnumSource(
        value = GeneratedOrderType.class,
        names = {"EMERGENCY_PROTECTION_ORDER", "CARE_ORDER", "SUPERVISION_ORDER"}
    )
    void shouldNotSendNotificationsWhenGeneratedOrderIsRemoved(GeneratedOrderType generatedOrderType) {
        Element<GeneratedOrder> order = element(buildOrder(generatedOrderType));
        Element<GeneratedOrder> blankOrder = element(buildBlankOrder());

        CaseDetails caseDetailsBefore = buildCaseDetails(List.of(blankOrder, order), emptyList(), null);
        CaseDetails caseDetailsAfter = buildCaseDetails(singletonList(blankOrder), emptyList(), null);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetailsAfter)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(notificationClient, never()).sendEmail(any(), any(), any(), any()));
    }

    @Test
    void shouldSendNotificationsToLocalAuthorityWhenCMOIsRemoved() throws NotificationClientException {
        Element<CaseManagementOrder> cmo = buildApprovedCMOElement(true);
        Element<GeneratedOrder> blankOrder = element(buildBlankOrder());

        CaseDetails caseDetailsBefore = buildCaseDetails(singletonList(blankOrder), singletonList(cmo), null);
        CaseDetails caseDetailsAfter = buildCaseDetails(singletonList(blankOrder), emptyList(), null);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetailsAfter)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(CMO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eqJson(expectedOrderRemovalTemplateParameters()),
            eq(NOTIFICATION_REFERENCE));
    }

    @Test
    void shouldSendNotificationsToLocalAuthorityWhenMultipleCMOsExistAndOneOfThemIsRemoved()
        throws NotificationClientException {
        Element<CaseManagementOrder> cmo1 = buildApprovedCMOElement(false);
        Element<CaseManagementOrder> cmo2 = buildApprovedCMOElement(true);

        Element<GeneratedOrder> blankOrder = element(buildBlankOrder());

        CaseDetails caseDetailsBefore = buildCaseDetails(singletonList(blankOrder), List.of(cmo1, cmo2), null);
        CaseDetails caseDetailsAfter = buildCaseDetails(singletonList(blankOrder), singletonList(cmo1), null);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetailsAfter)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(CMO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eqJson(expectedOrderRemovalTemplateParameters()),
            eq(NOTIFICATION_REFERENCE));
    }

    @Disabled
    @Test
    void shouldSendNotificationsToGatekeeperWhenSDOIsRemoved() throws NotificationClientException {
        Element<CaseManagementOrder> cmo = buildApprovedCMOElement(false);
        StandardDirectionOrder sdo = buildSDOElement();

        Element<GeneratedOrder> blankOrder = element(buildBlankOrder());

        CaseDetails caseDetailsBefore = buildCaseDetails(singletonList(blankOrder), singletonList(cmo), sdo);
        CaseDetails caseDetailsAfter = buildCaseDetails(singletonList(blankOrder), singletonList(cmo), null);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetailsAfter)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(SDO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(GATEKEEPER_EMAIL_ADDRESS),
            eqJson(expectedOrderRemovalTemplateParameters()),
            eq(NOTIFICATION_REFERENCE));
    }

    private Map<String, Object> expectedOrderRemovalTemplateParameters() {
        return ImmutableMap.of(
            "caseReference", String.valueOf(CASE_ID),
            "caseUrl", "http://fake-url/cases/case-details/12345",
            "respondentLastName", RESPONDENT_LAST_NAME,
            "returnedNote", REMOVAL_REASON
        );
    }

    private Element<CaseManagementOrder> buildApprovedCMOElement(boolean buildCmoWithRemovalReason) {
        UUID removedOrderId = UUID.randomUUID();

        return element(removedOrderId, CaseManagementOrder.builder()
            .status(APPROVED)
            .removalReason(buildCmoWithRemovalReason ? REMOVAL_REASON : null)
            .build());
    }

    private StandardDirectionOrder buildSDOElement() {
        return StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .orderDoc(DOCUMENT_REFERENCE)
            //.removalReason(REMOVAL_REASON) TODO: add reason
            .build();
    }

    private GeneratedOrder buildBlankOrder() {
        return GeneratedOrder.builder()
            .type("Blank order (C21)")
            .title("order")
            .dateOfIssue("12 March 1234")
            .removalReason(REMOVAL_REASON)
            .build();
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

    private CaseDetails buildCaseDetails(
        List<Element<GeneratedOrder>> orders,
        List<Element<CaseManagementOrder>> cmoList,
        StandardDirectionOrder sdo
    ) {
        return asCaseDetails(
            CaseData.builder()
                .id(CASE_ID)
                .orderCollection(orders)
                .sealedCMOs(cmoList)
                .standardDirectionOrder(sdo)
                .reasonToRemoveOrder(REMOVAL_REASON)
                .respondents1(singletonList(element(RESPONDENT)))
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .gatekeeperEmails(
                    singletonList(element(EmailAddress.builder().email(GATEKEEPER_EMAIL_ADDRESS).build()))
                ).build()
        );
    }
}
