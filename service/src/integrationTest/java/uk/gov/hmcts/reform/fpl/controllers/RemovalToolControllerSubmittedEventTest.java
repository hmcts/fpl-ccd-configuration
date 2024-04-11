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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.CtscTeamLeadLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.RemovedApplicationForm;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationFormRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_FORM_REMOVED_CTSC_LEAD_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_REMOVED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.ASYNC_MAX_TIMEOUT;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@WebMvcTest(RemovalToolController.class)
@OverrideAutoConfiguration(enabled = true)
class RemovalToolControllerSubmittedEventTest extends AbstractCallbackTest {
    private static final Long CASE_ID = 12345L;
    private static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String REMOVAL_REASON = "The order was removed because incorrect data was entered";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
        .build();
    private static final String CHILD_LAST_NAME = "Jones";
    private static final Child CHILD = Child.builder()
        .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
        .build();
    private static final String CTSC_TEAM_LEAD_EMAIL = "teamlead@test.com";
    public static final String CONFIDENTIAL_INFORMATION_WAS_DISCLOSED = "Confidential information was disclosed.";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CCDConcurrencyHelper helper;

    @MockBean
    private CtscTeamLeadLookupConfiguration ctscTeamLeadLookupConfiguration;

    @MockBean
    private Time time;

    RemovalToolControllerSubmittedEventTest() {
        super("remove-order");
    }

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(LocalDateTime.of(2010, 3, 20, 20, 20, 0));
    }

    @Test
    void shouldPublishPopulateSDOAndSDORemovedEventsIfNewSDOHasBeenRemoved() throws NotificationClientException {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().removalReason(REMOVAL_REASON).build();
        CaseDetails caseDetails = caseDetailsWithRemovableOrders(emptyList(), wrapElements(previousSDO), emptyList(),
            emptyList());
        when(helper.startEvent(eq(CASE_ID), eq("populateSDO")))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), emptyList(), emptyList(),
            emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(helper, timeout(ASYNC_MAX_TIMEOUT)).startEvent(
            eq(CASE_ID),
            eq("populateSDO"));

        verify(helper, timeout(ASYNC_MAX_TIMEOUT)).submitEvent(
            any(),
            eq(CASE_ID),
            anyMap());

        verify(notificationClient).sendEmail(
            eq(SDO_REMOVAL_NOTIFICATION_TEMPLATE),
            eq(GATEKEEPER_EMAIL_ADDRESS),
            eqJson(expectedOrderRemovalTemplateParameters()),
            eq(notificationReference(CASE_ID)));
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

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(emptyList(), hiddenSDOs, emptyList(), emptyList());
        when(helper.startEvent(eq(CASE_ID), eq("populateSDO")))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), previousHiddenSDOs, emptyList(),
            emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(helper, timeout(ASYNC_MAX_TIMEOUT)).startEvent(
            eq(CASE_ID),
            eq("populateSDO"));

        verify(helper, timeout(ASYNC_MAX_TIMEOUT)).submitEvent(
            any(),
            eq(CASE_ID),
            anyMap());

        verify(notificationClient).sendEmail(
            SDO_REMOVAL_NOTIFICATION_TEMPLATE,
            GATEKEEPER_EMAIL_ADDRESS,
            expectedOrderRemovalTemplateParameters(),
            notificationReference(CASE_ID)
        );
    }

    @Test
    void shouldNotPublishPopulateSDOAndSDORemovedEventsIfASDOHasNotBeenRemoved() {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().build();

        List<Element<StandardDirectionOrder>> hiddenSDOs = singletonList(element(previousSDO));
        List<Element<HearingOrder>> hiddenCMOs = singletonList(element(HearingOrder.builder().build()));

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, hiddenSDOs, emptyList(), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), hiddenSDOs, emptyList(),
            emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verifyNoMoreInteractions(helper);
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
        List<Element<AdditionalApplicationsBundle>> hiddenApplications = wrapElements(AdditionalApplicationsBundle
            .builder().build());

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, hiddenSDOs, hiddenOrders,
            hiddenApplications);
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(hiddenCMOs, hiddenSDOs, previousHiddenOrders,
            hiddenApplications);

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

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, emptyList(), emptyList(), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), emptyList(), emptyList(),
            emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            CMO_REMOVAL_NOTIFICATION_TEMPLATE,
            LOCAL_AUTHORITY_1_INBOX,
            expectedOrderRemovalTemplateParameters(),
            notificationReference(CASE_ID)
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

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(hiddenCMOs, emptyList(), emptyList(), emptyList());
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(previouHiddenCMOs, emptyList(), emptyList(),
            emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            CMO_REMOVAL_NOTIFICATION_TEMPLATE,
            LOCAL_AUTHORITY_1_INBOX,
            expectedOrderRemovalTemplateParameters(),
            notificationReference(CASE_ID)
        );

        verifyNoMoreInteractions(helper);
    }

    @Test
    void shouldNotifyCTSCTeamLeadIfAnAdditionalApplicationIsRemoved() throws NotificationClientException {
        given(ctscTeamLeadLookupConfiguration.getEmail())
            .willReturn(CTSC_TEAM_LEAD_EMAIL);

        Element<AdditionalApplicationsBundle> previousApplication =
            element(AdditionalApplicationsBundle.builder().removalReason(REMOVAL_REASON)
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .applicantName("Jim Byrne")
                    .document(testDocumentReference("Filename"))
                    .build())
                .build());

        List<Element<AdditionalApplicationsBundle>> hiddenApplications = singletonList(previousApplication);

        CaseDetails caseDetails = caseDetailsWithRemovableOrders(emptyList(), emptyList(), emptyList(),
            hiddenApplications);
        CaseDetails caseDetailsBefore = caseDetailsWithRemovableOrders(emptyList(), emptyList(), emptyList(),
            emptyList());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore).build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            APPLICATION_REMOVED_NOTIFICATION_TEMPLATE,
            CTSC_TEAM_LEAD_EMAIL,
            expectedApplicationRemovalTemplateParameters(),
            notificationReference(CASE_ID)
        );
    }

    @Test
    void shouldNotifyCTSCTeamLeadIfAnApplicationFormIsRemoved() throws NotificationClientException {
        given(ctscTeamLeadLookupConfiguration.getEmail())
            .willReturn(CTSC_TEAM_LEAD_EMAIL);

        DocumentReference c110a = testDocumentReference();

        CaseData caseDataBefore = CaseData.builder()
            .respondents1(wrapElements(RESPONDENT))
            .id(CASE_ID)
            .familyManCaseNumber(CASE_ID.toString())
            .c110A(C110A.builder()
                .submittedForm(c110a)
                .build())
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .removalToolData(RemovalToolData.builder()
                .hiddenApplicationForm(RemovedApplicationForm.builder()
                    .removalReason(CONFIDENTIAL_INFORMATION_WAS_DISCLOSED)
                    .submittedForm(c110a)
                    .build())
                .build())
            .c110A(C110A.builder()
                .submittedForm(null)
                .build())
            .build();

        CaseDetails caseDetailsBefore = convertDataToDetails(caseDataBefore);
        CaseDetails caseDetails = convertDataToDetails(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(APPLICATION_FORM_REMOVED_CTSC_LEAD_TEMPLATE),
            eq(CTSC_TEAM_LEAD_EMAIL),
            eq(expectedApplicationFormRemovalTemplateParameters()),
            eq(notificationReference(CASE_ID))
        ));

    }

    private CaseDetails convertDataToDetails(CaseData caseData) {
        return CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(caseData.getId())
            .data(caseConverter.toMap(caseData))
            .build();
    }

    private Map<String, Object> expectedOrderRemovalTemplateParameters() {
        OrderRemovalTemplate orderRemovalTemplate = OrderRemovalTemplate.builder()
            .caseReference(CASE_ID.toString())
            .caseUrl(caseUrl(CASE_ID))
            .lastName(CHILD_LAST_NAME)
            .removalReason(REMOVAL_REASON)
            .build();
        return caseConverter.toMap(orderRemovalTemplate);
    }

    private Map<String, Object> expectedApplicationRemovalTemplateParameters() {
        ApplicationRemovedNotifyData data = ApplicationRemovedNotifyData.builder()
            .caseId(CASE_ID.toString())
            .applicantName("Jim Byrne")
            .c2Filename("Filename")
            .caseUrl("http://fake-url/cases/case-details/12345")
            .removalDate("20 March 2010 at 8:20pm")
            .reason("The order was removed because incorrect data was entered")
            .applicationFeeText("An application fee needs to be refunded.")
            .childLastName("Jones").build();

        return mapper.convertValue(data, new TypeReference<>() {
        });
    }

    private Map<String, Object> expectedApplicationFormRemovalTemplateParameters() {
        ApplicationFormRemovedNotifyData data = ApplicationFormRemovedNotifyData.builder()
            .caseUrl("http://fake-url/cases/case-details/12345")
            .removalReason(CONFIDENTIAL_INFORMATION_WAS_DISCLOSED)
            .familyManCaseNumber(CASE_ID.toString())
            .lastName("Smith")
            .build();

        return mapper.convertValue(data, new TypeReference<>() {});
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
                                                       List<Element<GeneratedOrder>> hiddenOrders,
                                                       List<Element<AdditionalApplicationsBundle>> hiddenApplications) {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .removalToolData(RemovalToolData.builder()
                .hiddenOrders(hiddenOrders)
                .hiddenStandardDirectionOrders(hiddenSDOs)
                .hiddenCaseManagementOrders(hiddenCMOs)
                .hiddenApplicationsBundle(hiddenApplications)
                .build())
            .respondents1(wrapElements(RESPONDENT))
            .children1(wrapElements(CHILD))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_1_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_1_INBOX)
                .build()))
            .gatekeeperEmails(wrapElements(EmailAddress.builder().email(GATEKEEPER_EMAIL_ADDRESS).build()))
            .build();

        return CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(caseData.getId())
            .data(caseConverter.toMap(caseData))
            .build();
    }
}
