package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;

import static java.util.Collections.emptyMap;
import static java.util.UUID.fromString;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.PARTIES_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftCMOControllerTest extends AbstractControllerTest {
    private static final long ID = 1L;
    private List<Element<HearingBooking>> hearingDetails;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);

    @Autowired
    private Time time;

    @Autowired
    private DraftCMOService draftCMOService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    DraftCMOControllerTest() {
        super("draft-cmo");
    }

    @PostConstruct
    void init() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now());
    }

    @Test
    void aboutToStartCallbackShouldPrepareCaseForCMO() {
        Map<String, Object> data = ImmutableMap.of(
            HEARING_DETAILS_KEY, hearingDetails,
            "respondents1", createRespondents(),
            "others", createOthers());

        List<String> expected = Arrays.asList(
            time.now().plusDays(5).format(dateTimeFormatter),
            time.now().plusDays(2).format(dateTimeFormatter));

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(getHearingDates(callbackResponse)).isEqualTo(expected);
        assertThat(getHearingDates(callbackResponse)).doesNotContain(time.now().format(dateTimeFormatter));

        assertThat(callbackResponse.getData().get("respondents_label")).isEqualTo(
            "Respondent 1 - Timothy Jones\nRespondent 2 - Sarah Simpson\n");

        assertThat(callbackResponse.getData().get("others_label")).isEqualTo(
            "Person 1 - Kyle Stafford\nOther person 1 - Sarah Simpson\n");

        assertThat(callbackResponse.getData()).doesNotContainKey("allPartiesCustom");
        assertThat(callbackResponse.getData()).doesNotContainKey("localAuthorityDirectionsCustom");
        assertThat(callbackResponse.getData()).doesNotContainKey("cafcassDirectionsCustom");
        assertThat(callbackResponse.getData()).doesNotContainKey("courtDirectionsCustom");
        assertThat(callbackResponse.getData()).doesNotContainKey("respondentDirectionsCustom");
        assertThat(callbackResponse.getData()).doesNotContainKey("otherPartiesDirectionsCustom");
    }

    @Test
    void midEventShouldGenerateDraftCaseManagementOrderDocument() {
        byte[] pdf = {1, 2, 3, 4, 5};
        final Document document = document();
        final DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", pdf);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);

        CaseDetails caseDetails = buildCaseDetails(emptyMap());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        verify(uploadDocumentService).uploadPDF(pdf, "draft-case-management-order.pdf");

        final Map<String, Object> responseCaseData = callbackResponse.getData();

        assertThat(responseCaseData).containsKey(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey());

        final CaseManagementOrder caseManagementOrder = mapper.convertValue(responseCaseData.get(
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey()), CaseManagementOrder.class);

        assertThat(caseManagementOrder.getOrderDoc()).isEqualTo(
            DocumentReference.builder()
                .binaryUrl(document().links.binary.href)
                .filename(document().originalDocumentName)
                .url(document().links.self.href)
                .build());
    }

    @Test
    void aboutToSubmitShouldPopulateCaseManagementOrder() {
        DynamicList dynamicHearingDates = draftCMOService.buildDynamicListFromHearingDetails(hearingDetails);

        dynamicHearingDates.setValue(DynamicListElement.builder()
            .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .label(time.now().plusDays(5).toString())
            .build());

        Map<String, Object> data = new HashMap<>();

        Stream.of(DirectionAssignee.values()).forEach(direction ->
            data.put(direction.toCustomDirectionField().concat("CMO"),
                createElementCollection(createUnassignedDirection()))
        );

        data.put(HEARING_DATE_LIST.getKey(), dynamicHearingDates);
        data.put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), CaseManagementOrder.builder()
            .orderDoc(DocumentReference.builder().filename("draft-case-management-order.pdf").build())
            .status(SELF_REVIEW)
            .action(OrderAction.builder().changeRequestedByJudge("Changes").build())
            .build());

        CaseDetails caseDetails = buildCaseDetails(data);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();

        assertThat(caseManagementOrder.getDirections()).containsAll(createCmoDirections());
        assertThat(caseManagementOrder.getId()).isEqualTo(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"));
        assertThat(caseManagementOrder.getHearingDate()).isEqualTo(time.now().plusDays(5).toString());
        assertThat(caseManagementOrder.getStatus()).isEqualTo(SELF_REVIEW);
        assertThat(caseManagementOrder.getOrderDoc().getFilename()).isEqualTo("draft-case-management-order.pdf");
        assertThat(caseManagementOrder.getAction().getChangeRequestedByJudge()).isEqualTo("Changes");
    }

    //TODO: caseDetails before is in this test as a start for conditional call to submitted code.
    @Test
    void submittedShouldTriggerCMOProgressionEvent() {
        String event = "internal-change:CMO_PROGRESSION";
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(ImmutableMap.of(
                    CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(),
                    CaseManagementOrder.builder().status(SELF_REVIEW).build()))
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .id(ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(ImmutableMap.of(
                    CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(),
                    CaseManagementOrder.builder().status(PARTIES_REVIEW).build()))
                .build())
            .build();

        postSubmittedEvent(request);

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, ID, event);
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> cmoHearingResponse = mapper.convertValue(
            callbackResponse.getData().get(HEARING_DATE_LIST.getKey()), new TypeReference<>() {});

        List<Map<String, Object>> listItemMap = mapper.convertValue(cmoHearingResponse.get("list_items"),
            new TypeReference<>() {});

        return listItemMap.stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel).collect(Collectors.toList());
    }

    private CaseDetails buildCaseDetails(final Map<String, Object> data) {
        return CaseDetails.builder()
            .id(ID)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(data)
            .build();
    }

}
