package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class DraftOrdersControllerTest {

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private NotificationClient notificationClient;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Test
    void aboutToStartCallbackShouldSplitDirectionsIntoSeparateCollections() throws Exception {
        String title = "example direction";

        List<Direction> directions = ImmutableList.of(
            Direction.builder().directionType(title).assignee(ALL_PARTIES).build(),
            Direction.builder().directionType(title).assignee(LOCAL_AUTHORITY).build(),
            Direction.builder().directionType(title).assignee(PARENTS_AND_RESPONDENTS).build(),
            Direction.builder().directionType(title).assignee(CAFCASS).build(),
            Direction.builder().directionType(title).assignee(OTHERS).build(),
            Direction.builder().directionType(title).assignee(COURT).build(),
            Direction.builder().directionType(title).custom("Yes").assignee(COURT).build()
        );

        Order sdo = Order.builder().directions(buildDirections(directions)).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("standardDirectionOrder", sdo))
                .build())
            .build();

        MvcResult response = makeRequest(request, "about-to-start");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(extractDirections(caseData.getAllParties())).containsOnly(directions.get(0));
        assertThat(extractDirections(caseData.getLocalAuthorityDirections())).containsOnly(directions.get(1));
        assertThat(extractDirections(caseData.getRespondentDirections())).containsOnly(directions.get(2));
        assertThat(extractDirections(caseData.getCafcassDirections())).containsOnly(directions.get(3));
        assertThat(extractDirections(caseData.getOtherPartiesDirections())).containsOnly(directions.get(4));
        assertThat(extractDirections(caseData.getCourtDirections())).containsOnly(directions.get(5)).hasSize(1);
    }

    @Nested
    class StateChangeTests {
        private final String event = "internal-changeState:Gatekeeping->PREPARE_FOR_HEARING";
        private final Long caseId = 1L;

        @Test
        void submittedCallbackShouldTriggerStateChangeWhenOrderIsMarkedAsFinal() throws Exception {
            makeRequestWithOrderStatus(OrderStatus.SEALED);

            verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, caseId, event);
        }

        @Test
        void submittedCallbackShouldNotTriggerStateChangeWhenOrderIsStillInDraftState() throws Exception {
            makeRequestWithOrderStatus(OrderStatus.DRAFT);

            verify(coreCaseDataService, never()).triggerEvent(JURISDICTION, CASE_TYPE, caseId, event);
        }

        private void makeRequestWithOrderStatus(OrderStatus status) throws Exception {
            Order order = Order.builder().orderStatus(status).build();

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .id(caseId)
                    .jurisdiction(JURISDICTION)
                    .caseTypeId(CASE_TYPE)
                    .data(ImmutableMap.of("standardDirectionOrder", order,
                        "caseLocalAuthority", "example",
                        "respondents1", ImmutableList.of(
                            ImmutableMap.of(
                                "id", "",
                                "value", Respondent.builder()
                                    .party(RespondentParty.builder()
                                        .dateOfBirth(LocalDate.now().plusDays(1))
                                        .lastName("Moley")
                                        .build())
                                    .build()))))
                    .build())
                .build();
            String callbackType = "submitted";
            makeRequest(request, callbackType);
        }
    }

    @Nested
    class DocumentTests {
        byte[] pdf = {1, 2, 3, 4, 5};
        Document document = document();

        DocumentTests() throws IOException {
            //NO - OP
        }

        @BeforeEach
        void setup() {
            DocmosisDocument docmosisDocument = new DocmosisDocument("standard-directions-order.pdf", pdf);

            given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        }

        @Test
        void midEventShouldGenerateDraftStandardDirectionDocument() throws Exception {
            given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, "draft-standard-directions-order.pdf"))
                .willReturn(document);

            List<Element<Direction>> directions = buildDirections(
                ImmutableList.of(Direction.builder()
                    .directionText("example")
                    .assignee(LOCAL_AUTHORITY)
                    .readOnly("No")
                    .build()));

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .data(createCaseDataMap(directions)
                        .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                        .build())
                    .build())
                .build();

            MvcResult response = makeRequest(request, "mid-event");

            AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
                .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

            Map<String, Object> sdo = (Map<String, Object>) callbackResponse.getData().get("standardDirectionOrder");

            assertThat(sdo).containsEntry(
                "orderDoc", ImmutableMap.builder()
                    .put("document_binary_url", document().links.binary.href)
                    .put("document_filename", document().originalDocumentName)
                    .put("document_url", document().links.self.href)
                    .build());
        }

        @Test
        void aboutToSubmitShouldPopulateHiddenCCDFieldsInStandardDirectionOrderToPersistData() throws Exception {
            given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, "standard-directions-order.pdf"))
                .willReturn(document);

            UUID uuid = UUID.randomUUID();

            List<Element<Direction>> fullyPopulatedDirection = ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .directionType("Identify alternative carers")
                    .directionText("Contact the parents to make sure there is a complete family tree showing family"
                        + " members who could be alternative carers.")
                    .assignee(LOCAL_AUTHORITY)
                    .directionRemovable("Yes")
                    .readOnly("Yes")
                    .directionRemovable("No")
                    .build())
                .build());

            List<Element<Direction>> directionWithShowHideValuesRemoved = ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .directionType("Identify alternative carers")
                    .assignee(LOCAL_AUTHORITY)
                    .readOnly("Yes")
                    .build())
                .build());

            Order order = Order.builder()
                .orderStatus(OrderStatus.SEALED)
                .build();

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .data(createCaseDataMap(directionWithShowHideValuesRemoved)
                        .put("standardDirectionOrder", order)
                        .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                        .build())
                    .build())
                .build();

            MvcResult response = makeRequest(request, "about-to-submit");

            AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
                .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

            CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            List<Element<Direction>> localAuthorityDirections =
                caseData.getStandardDirectionOrder().getDirections().stream()
                    .filter(direction -> direction.getValue().getAssignee() == LOCAL_AUTHORITY)
                    .collect(toList());

            assertThat(localAuthorityDirections).isEqualTo(fullyPopulatedDirection);
            assertThat(caseData.getStandardDirectionOrder().getOrderDoc()).isNotNull();
            assertThat(caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor()).isNotNull();
            assertThat(caseData.getJudgeAndLegalAdvisor()).isNull();
        }
    }

    @Test
    void shouldNotTriggerSDOEventWhenDraft() throws Exception {
        makeRequest(buildCallbackRequest(DRAFT), "submitted");

        verify(applicationEventPublisher, times(0)).publishEvent(StandardDirectionsOrderIssuedEvent.class);
    }

    @Test
    void shouldTriggerSDOEventWhenSubmitted() throws Exception {
        makeRequest(buildCallbackRequest(SEALED), "submitted");

        verify(notificationClient, times(1)).sendEmail(
            eq(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE), eq("cafcass@cafcass.com"),
            eq(cafcassParameters()), eq("12345")
        );
    }

    private Map<String, Object> cafcassParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", "cafcass")
            .put("familyManCaseNumber", "")
            .put("leadRespondentsName", "Moley,")
            .put("hearingDate", "20 October 2020")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }

    private MvcResult makeRequest(CallbackRequest request, String endpoint) throws Exception {
        return mockMvc
            .perform(post("/callback/draft-standard-directions/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private ImmutableMap.Builder createCaseDataMap(List<Element<Direction>> directions) {
        ImmutableMap.Builder builder = ImmutableMap.<String, Object>builder();

        return builder
            .put(LOCAL_AUTHORITY.getValue(), directions)
            .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .put(PARENTS_AND_RESPONDENTS.getValue(),
                buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()));
    }

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(direction -> Element.<Direction>builder()
            .id(UUID.randomUUID())
            .value(direction)
            .build())
            .collect(toList());
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return ImmutableList.of(Element.<Direction>builder()
            .id(UUID.randomUUID())
            .value(direction)
            .build());
    }

    private List<Direction> extractDirections(List<Element<Direction>> directions) {
        return directions.stream().map(Element::getValue).collect(toList());
    }

    private CallbackRequest buildCallbackRequest(OrderStatus status) {
        Order order = Order.builder()
            .orderStatus(status)
            .build();

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "hearingDetails", ImmutableList.of(
                        Element.builder()
                            .value(HearingBooking.builder()
                                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                                .endDate(LocalDateTime.of(2020, 11, 20, 11, 11, 11))
                                .build())
                            .build()),
                    "respondents1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(LocalDate.now().plusDays(1))
                                    .lastName("Moley")
                                    .build())
                                .build()
                        )
                    ),
                    "standardDirectionOrder", order,
                    "caseLocalAuthority", "example"))
                .build())
            .build();
    }
}
