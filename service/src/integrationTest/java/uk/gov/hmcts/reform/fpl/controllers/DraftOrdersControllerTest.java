package uk.gov.hmcts.reform.fpl.controllers;

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
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class DraftOrdersControllerTest extends AbstractControllerTest {
    private static final Long CASE_ID = 1L;
    private static final String SEND_DOCUMENT_EVENT = "internal-change:SEND_DOCUMENT";

    private final DocumentReference documentReference = DocumentReference.builder().build();

    @Mock
    ApplicationEventPublisher applicationEventPublisher;
    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private InboxLookupService inboxLookupService;

    @Autowired
    private Time time;

    DraftOrdersControllerTest() {
        super("draft-standard-directions");
    }

    @Test
    void aboutToStartCallbackShouldSplitDirectionsIntoSeparateCollectionsAndShowEmptyPlaceHolderForHearingDate() {
        List<Direction> directions = createDirections();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("standardDirectionOrder", Order.builder().directions(buildDirections(directions)).build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(extractDirections(caseData.getAllParties())).containsOnly(directions.get(0));
        assertThat(extractDirections(caseData.getLocalAuthorityDirections())).containsOnly(directions.get(1));
        assertThat(extractDirections(caseData.getRespondentDirections())).containsOnly(directions.get(2));
        assertThat(extractDirections(caseData.getCafcassDirections())).containsOnly(directions.get(3));
        assertThat(extractDirections(caseData.getOtherPartiesDirections())).containsOnly(directions.get(4));
        assertThat(extractDirections(caseData.getCourtDirections())).containsOnly(directions.get(5)).hasSize(1);
        assertThat(caseData.getDateOfIssue()).isEqualTo(time.now().toLocalDate());

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            assertThat(callbackResponse.getData().get(assignee.toHearingDateField()))
                .isEqualTo("Please enter a hearing date"));
    }

    @Test
    void aboutToStartCallbackShouldPopulateCorrectHearingDate() {
        LocalDateTime date = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("hearingDetails", wrapElements(createHearingBooking(date, date.plusDays(1)))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            assertThat(callbackResponse.getData().get(assignee.toHearingDateField()))
                .isEqualTo("1 January 2020, 12:00am"));
    }

    @Test
    void shouldNotTriggerSDOEventWhenDraft() {
        postSubmittedEvent(buildCallbackRequest(DRAFT));

        verify(applicationEventPublisher, never()).publishEvent(StandardDirectionsOrderIssuedEvent.class);
    }

    @Test
    void shouldNotTriggerSendDocumentEventWhenDraft() {
        postSubmittedEvent(buildCallbackRequest(DRAFT));

        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), eq(SEND_DOCUMENT_EVENT), any());
    }

    @Test
    void shouldTriggerSDOEventWhenSubmitted() throws Exception {
        postSubmittedEvent(buildCallbackRequest(SEALED));

        verify(notificationClient).sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            "cafcass@cafcass.com",
            cafcassParameters(),
            String.valueOf(CASE_ID)
        );
    }

    @Test
    void shouldTriggerSendDocumentEventWhenSubmitted() {
        postSubmittedEvent(buildCallbackRequest(SEALED));

        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", documentReference));
    }

    private List<Direction> createDirections() {
        String title = "example direction";

        return List.of(
            Direction.builder().directionType(title).assignee(ALL_PARTIES).build(),
            Direction.builder().directionType(title).assignee(LOCAL_AUTHORITY).build(),
            Direction.builder().directionType(title).assignee(PARENTS_AND_RESPONDENTS).build(),
            Direction.builder().directionType(title).assignee(CAFCASS).build(),
            Direction.builder().directionType(title).assignee(OTHERS).build(),
            Direction.builder().directionType(title).assignee(COURT).build(),
            Direction.builder().directionType(title).custom("Yes").assignee(COURT).build()
        );
    }

    private Map<String, Object> cafcassParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", "cafcass")
            .put("familyManCaseNumber", "")
            .put("leadRespondentsName", "Moley,")
            .put("hearingDate", "20 October 2020")
            .put("reference", String.valueOf(CASE_ID))
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, CASE_ID))
            .build();
    }

    private ImmutableMap.Builder createCaseDataMap(List<Element<Direction>> directions) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

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
        return directions.stream().map(ElementUtils::element).collect(toList());
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return wrapElements(direction.toBuilder().directionType("Direction").build());
    }

    private List<Direction> extractDirections(List<Element<Direction>> directions) {
        return directions.stream().map(Element::getValue).collect(toList());
    }

    private CallbackRequest buildCallbackRequest(OrderStatus status) {
        Order order = Order.builder()
            .orderStatus(status)
            .orderDoc(documentReference)
            .build();

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of(
                    HEARING_DETAILS_KEY, List.of(
                        Element.builder()
                            .value(HearingBooking.builder()
                                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                                .endDate(LocalDateTime.of(2020, 11, 20, 11, 11, 11))
                                .build())
                            .build()),
                    "respondents1", List.of(
                        Map.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(LocalDate.now().plusDays(1))
                                    .lastName("Moley")
                                    .relationshipToChild("Uncle")
                                    .build())
                                .build()
                        )
                    ),
                    "standardDirectionOrder", order,
                    "caseLocalAuthority", "example"))
                .build())
            .build();
    }

    @Nested
    class StateChangeTests {
        private static final String PREPARE_FOR_HEARING_EVENT = "internal-changeState:Gatekeeping->PREPARE_FOR_HEARING";

        @Test
        void submittedCallbackShouldTriggerStateChangeWhenOrderIsMarkedAsFinal() {
            makeRequestWithOrderStatus(OrderStatus.SEALED);

            verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, PREPARE_FOR_HEARING_EVENT);
        }

        @Test
        void submittedCallbackShouldNotTriggerStateChangeWhenOrderIsStillInDraftState() {
            makeRequestWithOrderStatus(OrderStatus.DRAFT);

            verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), eq(PREPARE_FOR_HEARING_EVENT));
        }

        private void makeRequestWithOrderStatus(OrderStatus status) {
            Order order = Order.builder().orderStatus(status).orderDoc(documentReference).build();

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .id(CASE_ID)
                    .jurisdiction(JURISDICTION)
                    .caseTypeId(CASE_TYPE)
                    .data(Map.of("standardDirectionOrder", order,
                        "caseLocalAuthority", "example",
                        "respondents1", List.of(
                            Map.of(
                                "id", "",
                                "value", Respondent.builder()
                                    .party(RespondentParty.builder()
                                        .dateOfBirth(LocalDate.now().plusDays(1))
                                        .lastName("Moley")
                                        .build())
                                    .build()))))
                    .build())
                .build();
            postSubmittedEvent(request);
        }
    }

    @Nested
    class DocumentTests {
        byte[] pdf = {1, 2, 3, 4, 5};
        Document document = document();

        private static final String SEALED_ORDER_FILE_NAME = "standard-directions-order.pdf";
        private static final String DRAFT_ORDER_FILE_NAME = "draft-standard-directions-order.pdf";

        @BeforeEach
        void setup() {
            DocmosisDocument docmosisDocument = new DocmosisDocument(SEALED_ORDER_FILE_NAME, pdf);

            given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        }

        @Test
        void midEventShouldGenerateDraftStandardDirectionDocument() {
            given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, DRAFT_ORDER_FILE_NAME))
                .willReturn(document);

            List<Element<Direction>> directions = buildDirections(
                List.of(Direction.builder()
                    .directionType("direction 1")
                    .directionText("example")
                    .assignee(LOCAL_AUTHORITY)
                    .readOnly("No")
                    .build()));

            CaseDetails caseDetails = CaseDetails.builder()
                .data(createCaseDataMap(directions)
                    .put("dateOfIssue", time.now().toLocalDate().toString())
                    .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                    .put("caseLocalAuthority", "example")
                    .put("dateSubmitted", time.now().toLocalDate().toString())
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

            assertThat(callbackResponse.getData().get("standardDirectionOrder"))
                .extracting("orderDoc").isEqualTo(Map.of(
                "document_binary_url", document().links.binary.href,
                "document_filename", document().originalDocumentName,
                "document_url", document().links.self.href
            ));
        }

        @Test
        void aboutToSubmitShouldPopulateHiddenCCDFieldsInStandardDirectionOrderToPersistData() {
            given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, SEALED_ORDER_FILE_NAME))
                .willReturn(document);

            UUID uuid = UUID.randomUUID();

            List<Element<Direction>> fullyPopulatedDirection = List.of(
                element(uuid, Direction.builder()
                    .directionType("Identify alternative carers")
                    .directionText("Contact the parents to make sure there is a complete family tree showing family"
                        + " members who could be alternative carers.")
                    .assignee(LOCAL_AUTHORITY)
                    .directionRemovable("Yes")
                    .readOnly("Yes")
                    .build()));

            List<Element<Direction>> directionWithShowHideValuesRemoved = buildDirectionWithShowHideValuesRemoved(uuid);

            CaseDetails caseDetails = CaseDetails.builder()
                .data(createCaseDataMap(directionWithShowHideValuesRemoved)
                    .put("dateOfIssue", time.now().toLocalDate().toString())
                    .put("standardDirectionOrder", Order.builder().orderStatus(SEALED).build())
                    .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                    .put("allocatedJudge", Judge.builder().build())
                    .put(HEARING_DETAILS_KEY, wrapElements(HearingBooking.builder()
                        .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                        .endDate(LocalDateTime.of(2020, 11, 20, 11, 11, 11))
                        .venue("EXAMPLE")
                        .build()))
                    .put("caseLocalAuthority", "example")
                    .put("dateSubmitted", time.now().toLocalDate().toString())
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

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

        @Test
        void aboutToSubmitShouldReturnErrorsWhenNoHearingDetailsExistsForSealedOrder() {
            given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, SEALED_ORDER_FILE_NAME))
                .willReturn(document());

            UUID uuid = UUID.randomUUID();

            List<Element<Direction>> directionWithShowHideValuesRemoved = buildDirectionWithShowHideValuesRemoved(uuid);

            CaseDetails caseDetails = CaseDetails.builder()
                .data(createCaseDataMap(directionWithShowHideValuesRemoved)
                    .put("standardDirectionOrder", Order.builder().orderStatus(SEALED).build())
                    .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                    .put("allocatedJudge", Judge.builder().build())
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            assertThat(response.getErrors())
                .containsOnly("You need to enter a hearing date.");
        }

        @Test
        void aboutToSubmitShouldReturnErrorsWhenNoAllocatedJudgeExistsForSealedOrder() {
            given(uploadDocumentService.uploadPDF(userId, userAuthToken, pdf, SEALED_ORDER_FILE_NAME))
                .willReturn(document());

            CallbackRequest request = buildCallbackRequest(SEALED);

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

            assertThat(response.getErrors())
                .containsOnly("You need to enter the allocated judge.");
        }

        private List<Element<Direction>> buildDirectionWithShowHideValuesRemoved(UUID uuid) {
            return List.of(element(uuid, Direction.builder()
                .directionType("Identify alternative carers")
                .assignee(LOCAL_AUTHORITY)
                .readOnly("Yes")
                .build()));
        }
    }
}
