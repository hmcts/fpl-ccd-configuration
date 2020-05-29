package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ComplyWithDirectionsControllerTest extends AbstractControllerTest {

    ComplyWithDirectionsControllerTest() {
        super("comply-with-directions");
    }

    @Test
    void shouldAddCorrectResponsesToCafcassAndCourtDirectionCollectionsWhenCourtIsResponding() {
        UUID uuid = randomUUID();
        DirectionResponse responseForCourt = directionResponseBuilder(uuid).build();
        DirectionResponse responseForCafcass = directionResponseBuilder(uuid)
            .respondingOnBehalfOf("CAFCASS")
            .build();

        List<Element<Direction>> directions = List.of(element(uuid, Direction.builder()
            .assignee(ALL_PARTIES)
            .responses(wrapElements(responseForCafcass, responseForCourt))
            .build()));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("standardDirectionOrder", Order.builder().directions(directions).build()))
            .build();

        CaseData caseData = getCaseData(postAboutToStartEvent(caseDetails));

        assertThat(unwrapElements(caseData.getCafcassDirections()))
            .containsOnly(Direction.builder()
                .assignee(ALL_PARTIES)
                .response(responseForCafcass)
                .build());

        assertThat(unwrapElements(caseData.getCourtDirectionsCustom()))
            .containsOnly(Direction.builder()
                .assignee(ALL_PARTIES)
                .response(responseForCourt)
                .build());
    }

    @Test
    void aboutToStartCallbackShouldAddAllPartiesDirectionsIntoSeparateRoleCollections() {
        List<Direction> directions = directionsForAllRoles();
        Order sdo = Order.builder().directions(buildDirections(directions)).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("standardDirectionOrder", sdo))
                .build())
            .build();

        CaseData caseData = getCaseData(postAboutToStartEvent(request));

        assertThat(collectionsContainDirectionsForRoleAndAllParties(caseData));
    }

    @Test
    void aboutToStartCallbackShouldReturnAllPartiesDirectionsWhenNoSpecificRoleDirections() {
        Direction direction = Direction.builder().assignee(ALL_PARTIES).build();
        Order sdo = Order.builder().directions(buildDirections(direction)).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("standardDirectionOrder", sdo))
                .build())
            .build();

        CaseData caseData = getCaseData(postAboutToStartEvent(request));

        assertThat(caseData.getAllParties()).isNull();
        assertThat(caseData.getLocalAuthorityDirections()).containsAll(sdo.getDirections());
        assertThat(caseData.getCafcassDirections()).containsAll(sdo.getDirections());
        assertThat(caseData.getRespondentDirections()).containsAll(sdo.getDirections());
        assertThat(caseData.getOtherPartiesDirections()).containsAll(sdo.getDirections());
        assertThat(caseData.getCourtDirectionsCustom()).containsAll(sdo.getDirections());
    }

    @SuppressWarnings("unchecked")
    @Test
    void aboutToSubmitShouldAddResponseToStandardDirectionOrderWhenEmptyServedCaseManagementOrders() {
        UUID uuid = randomUUID();
        List<Element<Direction>> directions = directions(uuid);
        Order sdo = order(uuid);

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(createCaseDataMap(directions)
                    .put("standardDirectionOrder", sdo)
                    .build())
                .build())
            .build();

        CaseData caseData = getCaseData(postAboutToSubmitEvent(request));

        assertThat(caseData.getStandardDirectionOrder().getDirections().get(0).getValue().getResponses()).isNotEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void aboutToSubmitShouldAddResponseToCaseManagementOrderWhenPopulatedServedCaseManagementOrders() {
        UUID uuid = randomUUID();
        List<Element<Direction>> directions = directions(uuid);
        Order sdo = order(uuid);

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(createCaseDataMap(directions)
                    .put("standardDirectionOrder", sdo)
                    .put("servedCaseManagementOrders", caseManagementOrders(uuid))
                    .build())
                .build())
            .build();

        CaseData caseData = getCaseData(postAboutToSubmitEvent(request));

        assertThat(getResponses(caseData.getServedCaseManagementOrders().get(0).getValue())).isNotEmpty();
    }

    private List<Element<DirectionResponse>> getResponses(CaseManagementOrder order) {
        return order.getDirections().get(0).getValue().getResponses();
    }

    private List<Element<CaseManagementOrder>> caseManagementOrders(UUID uuid) {
        return wrapElements(CaseManagementOrder.builder()
            .directions(directions(uuid))
            .build());
    }

    private Order order(UUID uuid) {
        return Order.builder()
            .directions(List.of(element(uuid, Direction.builder()
                .directionType("example direction")
                .build())))
            .build();
    }

    private List<Element<Direction>> directions(UUID uuid) {
        return List.of(element(uuid, Direction.builder()
            .response(DirectionResponse.builder()
                .complied("Yes")
                .build())
            .build()));
    }

    private List<Direction> directionsForAllRoles() {
        return Stream.of(DirectionAssignee.values())
            .map(directionAssignee -> Direction.builder().assignee(directionAssignee).build())
            .collect(toList());
    }

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(ElementUtils::element)
            .collect(toList());
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return wrapElements(direction);
    }


    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        return mapper.convertValue(callbackResponse.getData(), CaseData.class);
    }

    private boolean collectionsContainDirectionsForRoleAndAllParties(CaseData caseData) {
        return roleDirectionsContainExpectedDirections(caseData.getLocalAuthorityDirections(), LOCAL_AUTHORITY)
            && roleDirectionsContainExpectedDirections(caseData.getCafcassDirections(), CAFCASS)
            && roleDirectionsContainExpectedDirections(caseData.getCourtDirectionsCustom(), COURT)
            && roleDirectionsContainExpectedDirections(caseData.getOtherPartiesDirections(), OTHERS)
            && roleDirectionsContainExpectedDirections(caseData.getRespondentDirections(), PARENTS_AND_RESPONDENTS)
            && roleDirectionsContainExpectedDirections(emptyList(), ALL_PARTIES);
    }

    private boolean roleDirectionsContainExpectedDirections(List<Element<Direction>> roleDirections,
                                                            DirectionAssignee assignee) {
        final Direction allPartiesDirection = Direction.builder().assignee(ALL_PARTIES).build();
        final Direction directions = Direction.builder().assignee(assignee).build();

        return roleDirections.stream()
            .map(Element::getValue)
            .anyMatch(x -> x.equals(allPartiesDirection) && x.equals(directions));
    }

    @SuppressWarnings("unchecked")
    private ImmutableMap.Builder createCaseDataMap(List<Element<Direction>> directions) {
        ImmutableMap.Builder builder = ImmutableMap.<String, List<Element<Direction>>>builder();

        return builder
            .put(LOCAL_AUTHORITY.getValue(), directions)
            .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .put(PARENTS_AND_RESPONDENTS.getValue(),
                buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()));
    }

    private DirectionResponse.DirectionResponseBuilder directionResponseBuilder(UUID uuid) {
        return DirectionResponse.builder()
            .directionId(uuid)
            .assignee(COURT)
            .complied("Yes");
    }
}
