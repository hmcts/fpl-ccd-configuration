package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftOrdersControllerAboutToStartTest extends AbstractControllerTest {
    private static final Long CASE_ID = 1L;
    private static final String SEND_DOCUMENT_EVENT = "internal-change:SEND_DOCUMENT";
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.builder().build();

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private Time time;

    DraftOrdersControllerAboutToStartTest() {
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
            Map.of("documentToBeSent", DOCUMENT_REFERENCE));
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

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(ElementUtils::element).collect(toList());
    }

    private List<Direction> extractDirections(List<Element<Direction>> directions) {
        return directions.stream().map(Element::getValue).collect(toList());
    }

    private CallbackRequest buildCallbackRequest(OrderStatus status) {
        Order order = Order.builder()
            .orderStatus(status)
            .orderDoc(DOCUMENT_REFERENCE)
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

}
