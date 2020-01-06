package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCaseManagementOrderControllerTest extends AbstractControllerTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String CASE_ID = "12345";
    private static final String REPRESENTATIVES = "representatives";
    private static final String CAFCASS_EMAIL_ADDRESS = "cafcass@cafcass.com";
    private static final String EVENT_KEY = "internal-change:CMO_PROGRESSION";
    private static final UUID ID = randomUUID();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);
    @Autowired
    private DraftCMOService draftCMOService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    private Document document;

    ActionCaseManagementOrderControllerTest() {
        super("action-cmo");
    }

    @BeforeEach
    void setup() {
        document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", PDF);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document);

        given(documentDownloadService.downloadDocument(anyString(), anyString(), anyString()))
            .willReturn(PDF);
    }

    @Test
    void aboutToStartShouldExtractIndividualCaseManagementOrderFieldsWithFutureHearingDates() {
        Map<String, Object> data = new HashMap<>();
        final CaseManagementOrder order = createCaseManagementOrder();

        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
        data.put("hearingDetails", createHearingBookings(LocalDateTime.now()));

        CallbackRequest request = buildCallbackRequest(data);
        List<String> expected = List.of(
            NOW.plusDays(5).format(dateTimeFormatter),
            NOW.plusDays(2).format(dateTimeFormatter));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(request);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(getHearingDates(response)).isEqualTo(expected);
        assertThat(getHearingDates(response)).doesNotContain(NOW.format(dateTimeFormatter));
        assertThat(caseData.getOrderAction()).isNull();
        assertThat(caseData.getSchedule()).isEqualTo(order.getSchedule());
        assertThat(caseData.getRecitals()).isEqualTo(order.getRecitals());
    }

    @Test
    void midEventShouldAddDocumentReferenceToOrderAction() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(buildCallbackRequest(emptyMap()));

        verify(uploadDocumentService).uploadPDF(userId, userAuthToken, PDF, "draft-case-management-order.pdf");

        Map<String, Object> responseCaseData = callbackResponse.getData();

        OrderAction action = mapper.convertValue(responseCaseData.get(ORDER_ACTION.getKey()), OrderAction.class);

        assertThat(action.getDocument()).isEqualTo(
            DocumentReference.builder()
                .binaryUrl(document().links.binary.href)
                .filename(document().originalDocumentName)
                .url(document().links.self.href)
                .build());
    }

    @Test
    void aboutToSubmitShouldReturnCaseManagementOrderWithFinalDocumentWhenSendToAllParties() {
        Map<String, Object> data = Map.of(
            "hearingDetails", hearingBookingWithStartDatePlus(-1),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES),
            NEXT_HEARING_DATE_LIST.getKey(), hearingDateList());

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(data));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(userId, userAuthToken, PDF, "case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder()).isEqualTo(expectedCaseManagementOrder());
    }

    @Test
    void aboutToSubmitShouldErrorIfHearingDateInFutureWhenSendToAllParties() {
        Map<String, Object> data = ImmutableMap.of(
            "hearingDetails", hearingBookingWithStartDatePlus(1),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(data));

        assertThat(response.getErrors()).containsOnly(HEARING_NOT_COMPLETED.getValue());
    }

    @Test
    void aboutToSubmitShouldReturnCaseManagementOrderWithDraftDocumentWhenNotSendToAllParties() {
        Map<String, Object> data = Map.of(
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(JUDGE_REQUESTED_CHANGE));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(data));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(userId, userAuthToken, PDF, "draft-case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder().getAction()).isEqualTo(getOrderAction(JUDGE_REQUESTED_CHANGE));
    }

    @Test
    void submittedShouldTriggerCMOProgressionEventAndSendCaseLinkNotificationsWhenIssuedOrderApproved()
        throws Exception {
        List<Element<Representative>> representativesServedByDigitalService =
            buildRepresentativesServedByDigitalService();

        CallbackRequest callbackRequest =
            populateRepresentativesByServedPreferenceData(representativesServedByDigitalService);

        postSubmittedEvent(callbackRequest);

        verifyCMOTriggerEventAndNotificationSentToLocalAuthorityOnApprovedCMO();

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq("abc@example.com"),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters("Jon Snow")), eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq("xyz@example.com"),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters("Hodo")), eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS),
            anyMap(), eq(CASE_ID));

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void submittedShouldTriggerCMOProgressionEventAndSendDocumentLinkNotificationsWhenIssuedOrderApproved()
        throws Exception {
        List<Element<Representative>> representativesServedByEmail = buildRepresentativesServedByEmail();

        CallbackRequest callbackRequest = populateRepresentativesByServedPreferenceData(representativesServedByEmail);

        postSubmittedEvent(callbackRequest);

        verifyCMOTriggerEventAndNotificationSentToLocalAuthorityOnApprovedCMO();

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS),
            anyMap(), eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq("jamie@example.com"),
            anyMap(), eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq("ragnar@example.com"),
            anyMap(), eq(CASE_ID));

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void submittedShouldTriggerCMOProgressionAndSendNotificationsToOnlyLocalAuthorityAndCafcass() throws Exception {
        List<Element<Representative>> representativeServedByPost = wrapElements(Representative.builder()
            .email("bien@example.com")
            .fullName("Bien")
            .servingPreferences(POST)
            .build());

        CallbackRequest callbackRequest = populateRepresentativesByServedPreferenceData(representativeServedByPost);

        postSubmittedEvent(callbackRequest);

        verifyCMOTriggerEventAndNotificationSentToLocalAuthorityOnApprovedCMO();

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS),
            anyMap(), eq(CASE_ID));

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void submittedShouldNotSendNotificationsWhenIssuedOrderNotApproved() {
        CaseManagementOrder caseManagementOrder = getCaseManagementOrder();

        Map<String, Object> data = Map.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), caseManagementOrder.toBuilder()
                .action(getOrderAction(JUDGE_REQUESTED_CHANGE))
                .build());

        CallbackRequest callbackRequest = buildCallbackRequest(data);

        postSubmittedEvent(callbackRequest);

        verifyZeroInteractions(notificationClient);
    }

    private CaseManagementOrder expectedCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .orderDoc(buildFromDocument(document()))
            .id(ID)
            .directions(emptyList())
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .nextHearingType(ISSUES_RESOLUTION_HEARING)
                .build())
            .nextHearing(NextHearing.builder()
                .id(ID)
                .date(NOW.toString())
                .build())
            .status(SEND_TO_JUDGE)
            .build();
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(data)
                .build())
            .build();
    }

    private OrderAction getOrderAction(ActionType type) {
        return OrderAction.builder()
            .type(type)
            .nextHearingType(ISSUES_RESOLUTION_HEARING)
            .build();
    }

    private CaseManagementOrder getCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .id(ID)
            .status(CMOStatus.SEND_TO_JUDGE)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .directions(createCmoDirections())
            .build();
    }

    private List<Element<HearingBooking>> hearingBookingWithStartDatePlus(int days) {
        return List.of(Element.<HearingBooking>builder()
            .id(ID)
            .value(HearingBooking.builder()
                .startDate(NOW.plusDays(days))
                .endDate(NOW.plusDays(days))
                .venue("venue")
                .build())
            .build());
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        return caseData.getNextHearingDateList().getListItems().stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel)
            .collect(toList());
    }

    private DynamicList hearingDateList() {
        DynamicList dynamicHearingDates = draftCMOService
            .buildDynamicListFromHearingDetails(hearingBookingWithStartDatePlus(0));

        dynamicHearingDates.setValue(DynamicListElement.builder()
            .code(ID)
            .label(NOW.toString())
            .build());
        return dynamicHearingDates;
    }

    private Map<String, Object> getExpectedCMOIssuedCaseLinkNotificationParameters(String recipientName) {
        final String subjectLine = "Jones, SACCCCCCCC5676576567";
        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", recipientName)
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", CASE_ID)
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/12345", JURISDICTION, CASE_TYPE))
            .build();
    }

    private Map<String, Object> buildSubmittedRequestData(final List<Element<Representative>>
                                                              representatives) {
        return Map.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            REPRESENTATIVES, representatives,
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), CaseManagementOrder.builder()
                .status(SEND_TO_JUDGE)
                .orderDoc(DocumentReference.buildFromDocument(document))
                .action(OrderAction.builder()
                    .type(SEND_TO_ALL_PARTIES)
                    .build())
                .build());
    }

    private List<Element<Representative>> buildRepresentativesServedByDigitalService() {
        return wrapElements(Representative.builder()
            .email("abc@example.com")
            .fullName("Jon Snow")
            .servingPreferences(DIGITAL_SERVICE)
            .build(), Representative.builder()
            .build(), Representative.builder()
            .email("xyz@example.com")
            .fullName("Hodo")
            .servingPreferences(DIGITAL_SERVICE)
            .build());
    }

    private CallbackRequest populateRepresentativesByServedPreferenceData(
        List<Element<Representative>> representativesServedByDigitalService) {
        Map<String, Object> data = buildSubmittedRequestData(representativesServedByDigitalService);

        return buildCallbackRequest(data);
    }

    private void verifyCMOTriggerEventAndNotificationSentToLocalAuthorityOnApprovedCMO()
        throws NotificationClientException {
        verify(coreCaseDataService)
            .triggerEvent(JURISDICTION, CASE_TYPE, 12345L, EVENT_KEY);

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters(LOCAL_AUTHORITY_NAME)), eq(CASE_ID));
    }

    private List<Element<Representative>> buildRepresentativesServedByEmail() {
        return wrapElements(Representative.builder()
            .email("jamie@example.com")
            .fullName("Jamie Lannister")
            .servingPreferences(EMAIL)
            .build(), Representative.builder()
            .email("ragnar@example.com")
            .fullName("Ragnar")
            .servingPreferences(EMAIL)
            .build());
    }
}
