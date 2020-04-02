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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCaseManagementOrderControllerTest extends AbstractControllerTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final UUID ID = randomUUID();

    private final DocumentReference cmoDocument = buildFromDocument(document());
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);
    @Autowired
    private DraftCMOService draftCMOService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private Time time;

    ActionCaseManagementOrderControllerTest() {
        super("action-cmo");
    }

    //TODO TECHDEBT refactor into separate files for each callback endpoint FPLA-1467
    @BeforeEach
    void setup() {
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", PDF);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());
    }

    @Test
    void aboutToStartShouldAddCurrentTimeAsDateOfIssuedWhenNotInCaseManagementOrder() {
        Map<String, Object> data = new HashMap<>();
        data.put(HEARING_DETAILS_KEY, createHearingBookings(time.now()));
        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), createCaseManagementOrder(SEND_TO_JUDGE));

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).containsEntry("dateOfIssue", time.now().toLocalDate().toString());
    }

    @Test
    void aboutToStartShouldAddPreviousTimeAsDateOfIssuedWhenInCaseManagementOrder() {
        Map<String, Object> data = new HashMap<>();
        data.put(HEARING_DETAILS_KEY, createHearingBookings(time.now()));
        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), createCaseManagementOrder(SEND_TO_JUDGE).toBuilder()
            .dateOfIssue("20 March 2019")
            .build());

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).containsEntry("dateOfIssue", LocalDate.of(2019, 3, 20).toString());
    }

    @Test
    void aboutToStartShouldExtractIndividualCaseManagementOrderFieldsWithFutureHearingDates() {
        Map<String, Object> data = new HashMap<>();
        final CaseManagementOrder order = createCaseManagementOrder(SEND_TO_JUDGE);

        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
        data.put(HEARING_DETAILS_KEY, createHearingBookings(LocalDateTime.now()));

        CaseDetails caseDetails = buildCaseDetails(data);
        List<String> expected = List.of(
            NOW.plusDays(5).format(dateTimeFormatter),
            NOW.plusDays(2).format(dateTimeFormatter));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(getHearingDates(response)).isEqualTo(expected);
        assertThat(getHearingDates(response)).doesNotContain(NOW.format(dateTimeFormatter));
        assertThat(caseData.getOrderAction()).isNull();
        assertThat(caseData.getSchedule()).isEqualTo(order.getSchedule());
        assertThat(caseData.getRecitals()).isEqualTo(order.getRecitals());
    }

    @Test
    void aboutToStartShouldNotProgressOrderWhenOrderActionIsNotSet() {
        CaseDetails caseDetails = createCaseDetailsWithEmptyCMO();
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).isEqualTo(CaseManagementOrder.builder().build());
    }

    @Test
    void midEventShouldAddDocumentReferenceToOrderAction() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(buildCaseDetails(emptyMap()));

        verify(uploadDocumentService).uploadPDF(PDF, "draft-case-management-order.pdf");

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
            HEARING_DETAILS_KEY, hearingBookingWithStartDatePlus(-1),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES),
            NEXT_HEARING_DATE_LIST.getKey(), hearingDateList());

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCaseDetails(data));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(PDF, "case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder()).isEqualTo(expectedCaseManagementOrder());
    }

    @Test
    void aboutToSubmitShouldErrorIfHearingDateInFutureWhenSendToAllParties() {
        Map<String, Object> data = ImmutableMap.of(
            HEARING_DETAILS_KEY, hearingBookingWithStartDatePlus(1),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCaseDetails(data));

        assertThat(response.getErrors()).containsOnly(HEARING_NOT_COMPLETED.getValue());
    }

    @Test
    void aboutToSubmitShouldReturnCaseManagementOrderWithDraftDocumentWhenNotSendToAllParties() {
        Map<String, Object> data = Map.of(
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(JUDGE_REQUESTED_CHANGE));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCaseDetails(data));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(PDF, "draft-case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder().getAction()).isEqualTo(getOrderAction(JUDGE_REQUESTED_CHANGE));
    }

    @Test
    void aboutToSubmitShouldRemoveOrderWhenOrderActionIsNotJudgeReview() {
        CaseDetails caseDetails = createCaseDetailsWithEmptyCMO();
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).isEqualTo(null);
    }

    private CaseDetails createCaseDetailsWithEmptyCMO() {
        Map<String, Object> data = new HashMap<>();
        final CaseManagementOrder order = CaseManagementOrder.builder().build();

        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
        return buildCaseDetails(data);
    }

    private CaseManagementOrder expectedCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .orderDoc(cmoDocument)
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

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .id(12345L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(data)
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
            .status(SEND_TO_JUDGE)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .directions(createCmoDirections())
            .orderDoc(DocumentReference.builder().build())
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
}
