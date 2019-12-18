package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_SDO;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

/**
 * A service that helps with the sorting and editing of directions.
 */
@Service
public class DirectionHelperService {
    private final UserDetailsService userDetailsService;

    public DirectionHelperService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Combines role directions into a single List of directions.
     *
     * @param caseData data containing all the directions by role.
     * @return directions.
     **/
    public List<Element<Direction>> combineAllDirections(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(caseData.getAllParties());

        directions.addAll(assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(caseData.getLocalAuthorityDirections());

        directions.addAll(assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY));

        directions.addAll(caseData.getRespondentDirections());

        directions.addAll(assignCustomDirections(caseData.getRespondentDirectionsCustom(), PARENTS_AND_RESPONDENTS));

        directions.addAll(caseData.getCafcassDirections());

        directions.addAll(assignCustomDirections(caseData.getCafcassDirectionsCustom(), CAFCASS));

        directions.addAll(caseData.getOtherPartiesDirections());

        directions.addAll(assignCustomDirections(caseData.getOtherPartiesDirectionsCustom(), OTHERS));

        directions.addAll(caseData.getCourtDirections());

        directions.addAll(assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

        return directions;
    }

    /**
     * Adds values that would otherwise be lost in CCD to directions.
     * Values include readOnly, directionRemovable and directionText.
     *
     * @param directionWithValues  an order object that should be generated using original case data.
     * @param directionToAddValues an order object that should be generated using case data edited through a ccd event.
     */
    public void persistHiddenDirectionValues(List<Element<Direction>> directionWithValues,
                                             List<Element<Direction>> directionToAddValues) {
        directionToAddValues.forEach(directionToAddValue -> directionWithValues
            .stream()
            .filter(direction -> hasSameDirectionType(directionToAddValue, direction))
            .forEach(direction -> {
                directionToAddValue.getValue().setReadOnly(direction.getValue().getReadOnly());
                directionToAddValue.getValue().setDirectionRemovable(direction.getValue().getDirectionRemovable());
                directionToAddValue.getValue().setAssignee(defaultIfNull(
                    directionToAddValue.getValue().getAssignee(), direction.getValue().getAssignee()));

                if (!direction.getValue().getReadOnly().equals("No")) {
                    directionToAddValue.getValue().setDirectionText(direction.getValue().getDirectionText());
                }
            }));
    }

    private boolean hasSameDirectionType(Element<Direction> directionToAddValue, Element<Direction> direction) {
        return direction.getValue().getDirectionType().equals(directionToAddValue.getValue().getDirectionType());
    }

    /**
     * Adds a response by an assignee to a direction where the direction ID of the response matches the direction ID
     * of the direction.
     *
     * @param responses  a list of single responses to directions.
     * @param directions a list of directions with combined responses.
     */
    public void addResponsesToDirections(List<DirectionResponse> responses, List<Element<Direction>> directions) {
        directions.forEach(direction -> responses.stream()
            .filter(response -> response.getDirectionId().equals(direction.getId()))
            .forEach(response -> {
                direction.getValue().getResponses().removeIf(element -> responseExists(response, element));

                direction.getValue().getResponses().add(Element.<DirectionResponse>builder()
                    .id(UUID.randomUUID())
                    .value(response)
                    .build());
            }));
    }

    private boolean responseExists(DirectionResponse response, Element<DirectionResponse> element) {
        return isNotEmpty(element.getValue()) && element.getValue().getAssignee().equals(response.getAssignee())
            && respondingOnBehalfIsEqual(response, element);
    }

    private boolean respondingOnBehalfIsEqual(DirectionResponse response, Element<DirectionResponse> element) {
        return defaultIfNull(element.getValue().getRespondingOnBehalfOf(), "")
            .equals(defaultIfNull(response.getRespondingOnBehalfOf(), ""));
    }

    /**
     * <b>To be used when court is complying on behalf of other parties</b>
     * <p></p>
     * Adds directions to case data for an assignee.
     *
     * @param caseDetails   the caseDetails to add the directions to.
     * @param directionsMap a map where the DirectionAssignee key corresponds to a list of directions elements.
     */
    public void addDirectionsToCaseDetails(CaseDetails caseDetails,
                                           Map<DirectionAssignee, List<Element<Direction>>> directionsMap,
                                           ComplyOnBehalfEvent eventId) {
        directionsMap.forEach((assignee, directions) -> {
            final List<Element<Direction>> clone = getClone(directionsMap.get(ALL_PARTIES));

            switch (assignee) {
                case PARENTS_AND_RESPONDENTS:
                    directions.addAll(clone);

                    if (eventId == COMPLY_ON_BEHALF_SDO) {
                        filterResponsesNotCompliedOnBehalfOfByTheCourt("RESPONDENT", directions);
                    } else {
                        filterResponsesNotCompliedBySolicitor(directions, PARENTS_AND_RESPONDENTS);
                    }

                    caseDetails.getData().put(assignee.toCustomDirectionField(), directions);

                    break;
                case OTHERS:
                    directions.addAll(clone);

                    if (eventId == COMPLY_ON_BEHALF_SDO) {
                        filterResponsesNotCompliedOnBehalfOfByTheCourt("OTHER", directions);
                    } else {
                        filterResponsesNotCompliedBySolicitor(directions, OTHERS);
                    }

                    caseDetails.getData().put(assignee.toCustomDirectionField(), directions);

                    break;
                case CAFCASS:
                    directions.addAll(clone);

                    List<Element<Direction>> cafcassDirections = extractPartyResponse(COURT, directions);

                    caseDetails.getData().put(assignee.toCustomDirectionField(), cafcassDirections);

                    break;
            }
        });
    }

    private List<Element<Direction>> getClone(List<Element<Direction>> elements) {
        return elements.stream()
            .map(directionElement -> Element.<Direction>builder()
                .id(directionElement.getId())
                .value(directionElement.getValue().deepCopy())
                .build())
            .collect(toList());
    }

    /**
     * Removes responses from a direction where they have not been complied on behalf of someone by the court.
     *
     * @param onBehalfOf a string matching part of the respondingOnBehalfOf variable.
     * @param directions a list of directions.
     */
    public void filterResponsesNotCompliedOnBehalfOfByTheCourt(String onBehalfOf, List<Element<Direction>> directions) {
        directions.forEach(directionElement -> directionElement.getValue().getResponses()
            .removeIf(element -> notCompliedWithByCourt(onBehalfOf, element)));
    }

    private void filterResponsesNotCompliedBySolicitor(List<Element<Direction>> directions,
                                                       DirectionAssignee assignee) {
        directions.forEach(directionElement -> directionElement.getValue().getResponses()
            .removeIf(element -> assignee != element.getValue().getAssignee()
                || isEmpty(element.getValue().getResponder())));
    }

    private boolean notCompliedWithByCourt(String onBehalfOf, Element<DirectionResponse> element) {
        return COURT != element.getValue().getAssignee() || isEmpty(element.getValue().getRespondingOnBehalfOf())
            || !element.getValue().getRespondingOnBehalfOf().contains(onBehalfOf);
    }

    /**
     * Extracts a specific response to a direction by a party.
     *
     * @param assignee   the role that responded to the direction.
     * @param directions a list of directions.
     * @return a list of directions with the correct response associated.
     */
    public List<Element<Direction>> extractPartyResponse(DirectionAssignee assignee,
                                                         List<Element<Direction>> directions) {
        return directions.stream()
            .map(element -> Element.<Direction>builder()
                .id(element.getId())
                .value(element.getValue().toBuilder()
                    .response(element.getValue().getResponses().stream()
                        .filter(response -> response.getValue().getDirectionId().equals(element.getId()))
                        .filter(response -> response.getValue().getAssignee().equals(assignee))
                        .map(Element::getValue)
                        .findFirst()
                        .orElse(null))
                    .responses(emptyList())
                    .build())
                .build())
            .collect(toList());
    }

    /**
     * Adds assignee directions key value pairs to caseDetails.
     *
     * <p></p>
     * courtDirectionsCustom is used here to stop giving C and D permissions on the CourtDirections object
     * in draft standard direction order for gatekeeper user when they also have the court admin role.
     *
     * @param assignee    the string value of the map key.
     * @param directions  a list of directions.
     * @param caseDetails the caseDetails to be updated.
     */
    public void addAssigneeDirectionKeyValuePairsToCaseData(DirectionAssignee assignee,
                                                            List<Element<Direction>> directions,
                                                            CaseDetails caseDetails) {
        if (assignee.equals(COURT)) {
            caseDetails.getData().put(assignee.getValue().concat("Custom"), extractPartyResponse(assignee, directions));
        } else {
            caseDetails.getData().put(assignee.getValue(), extractPartyResponse(assignee, directions));
        }
    }

    /**
     * Collects directions for all parties and places them into single map, where the key is the role direction,
     * and the value is the list of directions associated.
     *
     * <p></p>
     * courtDirectionsCustom is used due to the workaround in {@link #addAssigneeDirectionKeyValuePairsToCaseData}.
     *
     * @param caseData data from case.
     * @return Map of roles and directions.
     */
    public Map<DirectionAssignee, List<Element<Direction>>> collectDirectionsToMap(CaseData caseData) {
        return Map.of(
            ALL_PARTIES, defaultIfNull(caseData.getAllParties(), emptyList()),
            LOCAL_AUTHORITY, defaultIfNull(caseData.getLocalAuthorityDirections(), emptyList()),
            CAFCASS, defaultIfNull(caseData.getCafcassDirections(), emptyList()),
            COURT, defaultIfNull(caseData.getCourtDirectionsCustom(), emptyList()),
            PARENTS_AND_RESPONDENTS, defaultIfNull(caseData.getRespondentDirections(), emptyList()),
            OTHERS, defaultIfNull(caseData.getOtherPartiesDirections(), emptyList()));
    }

    /**
     * Collects custom directions for all parties and places them into single map, where the key is the role direction,
     * and the value is the list of directions associated.
     *
     * @param caseData data from case.
     * @return Map of roles and directions.
     */
    public Map<DirectionAssignee, List<Element<Direction>>> collectCustomDirectionsToMap(CaseData caseData) {
        return Map.of(
            ALL_PARTIES, defaultIfNull(caseData.getAllPartiesCustom(), emptyList()),
            LOCAL_AUTHORITY, defaultIfNull(caseData.getLocalAuthorityDirectionsCustom(), emptyList()),
            CAFCASS, defaultIfNull(caseData.getCafcassDirectionsCustom(), emptyList()),
            COURT, defaultIfNull(caseData.getCourtDirectionsCustom(), emptyList()),
            PARENTS_AND_RESPONDENTS, defaultIfNull(caseData.getRespondentDirectionsCustom(), emptyList()),
            OTHERS, defaultIfNull(caseData.getOtherPartiesDirectionsCustom(), emptyList()));
    }

    /**
     * Adds responses to directions in standard direction order {@link uk.gov.hmcts.reform.fpl.model.Order}.
     *
     * @param caseData caseData containing custom role collections and standard directions order.
     */
    public void addComplyOnBehalfResponsesToDirectionsInOrder(CaseData caseData,
                                                              ComplyOnBehalfEvent eventId,
                                                              String authorisation) {
        Map<DirectionAssignee, List<Element<Direction>>> customDirectionsMap = collectCustomDirectionsToMap(caseData);
        List<Element<Direction>> directionsToComplyWith = getDirectionsToComplyWith(caseData);

        customDirectionsMap.forEach((assignee, directions) -> {
            switch (assignee) {
                case CAFCASS:
                    List<DirectionResponse> responses = getResponses(ImmutableMap.of(COURT, directions)).stream()
                        .map(response -> response.toBuilder().respondingOnBehalfOf("CAFCASS").build())
                        .collect(toList());

                    addResponsesToDirections(responses, directionsToComplyWith);

                    break;

                case PARENTS_AND_RESPONDENTS:
                    List<Element<DirectionResponse>> respondentResponses = addValuesToListResponses(
                        directions, eventId, authorisation, PARENTS_AND_RESPONDENTS);

                    addResponseElementsToDirections(respondentResponses, directionsToComplyWith);

                    break;
                case OTHERS:
                    List<Element<DirectionResponse>> otherResponses = addValuesToListResponses(
                        directions, eventId, authorisation, OTHERS);

                    addResponseElementsToDirections(otherResponses, directionsToComplyWith);

                    break;
            }
        });
    }

    //TODO: refactor of addCourtAssigneeAndDirectionId would remove dependency on eventId.
    private List<Element<DirectionResponse>> addValuesToListResponses(List<Element<Direction>> directions,
                                                                      ComplyOnBehalfEvent eventId,
                                                                      String authorisation,
                                                                      DirectionAssignee assignee) {
        final UUID[] id = new UUID[1];

        return directions.stream()
            .map(directionElement -> {
                id[0] = directionElement.getId();

                return directionElement.getValue().getResponses();
            })
            .flatMap(List::stream)
            .map(element -> getDirectionResponse(eventId, authorisation, id[0], element, assignee))
            .collect(toList());
    }

    private Element<DirectionResponse> getDirectionResponse(ComplyOnBehalfEvent event,
                                                            String authorisation,
                                                            UUID id,
                                                            Element<DirectionResponse> response,
                                                            DirectionAssignee assignee) {
        if (event == COMPLY_ON_BEHALF_SDO) {
            return addCourtAssigneeAndDirectionId(id, response);
        } else {
            return addResponderAssigneeAndDirectionId(response, authorisation, assignee, id);
        }
    }

    //TODO: if name of user complying for court is added then this can be merged with logic from method below.
    private Element<DirectionResponse> addCourtAssigneeAndDirectionId(UUID id, Element<DirectionResponse> element) {
        return Element.<DirectionResponse>builder()
            .id(element.getId())
            .value(element.getValue().toBuilder()
                .assignee(COURT)
                .directionId(id)
                .build())
            .build();
    }

    private Element<DirectionResponse> addResponderAssigneeAndDirectionId(Element<DirectionResponse> response,
                                                                          String authorisation,
                                                                          DirectionAssignee assignee,
                                                                          UUID id) {
        return Element.<DirectionResponse>builder()
            .id(response.getId())
            .value(response.getValue().toBuilder()
                .assignee(assignee)
                .responder(getUsername(response, authorisation))
                .directionId(id)
                .build())
            .build();
    }

    private String getUsername(Element<DirectionResponse> element, String authorisation) {
        String userName;

        if (isEmpty(element.getValue().getResponder())) {
            userName = userDetailsService.getUserName(authorisation);
        } else {
            userName = element.getValue().getResponder();
        }

        return userName;
    }

    /**
     * Adds a direction response element to a direction where the direction id matches.
     *
     * @param responses  a list of direction response elements.
     * @param directions a list of directions.
     */
    public void addResponseElementsToDirections(List<Element<DirectionResponse>> responses,
                                                List<Element<Direction>> directions) {
        directions.forEach(direction -> responses.stream()
            .filter(response -> response.getValue().getDirectionId().equals(direction.getId()))
            .forEach(response -> {
                direction.getValue().getResponses().removeIf(x -> response.getId().equals(x.getId()));
                direction.getValue().getResponses().add(response);
            }));
    }

    /**
     * Gets responses with populated directionId and assignee wherever a response is present within a direction.
     *
     * @param directionMap a map of directions where assignee is key and value is a list of directions.
     * @return a list of responses with hidden variables added.
     */
    public List<DirectionResponse> getResponses(Map<DirectionAssignee, List<Element<Direction>>> directionMap) {
        return directionMap.entrySet()
            .stream()
            .map(entry -> addHiddenVariablesToResponseForAssignee(entry.getKey(), entry.getValue()))
            .flatMap(List::stream)
            .map(element -> element.getValue().getResponse())
            .collect(toList());
    }

    private List<Element<Direction>> addHiddenVariablesToResponseForAssignee(DirectionAssignee assignee,
                                                                             List<Element<Direction>> directions) {
        return directions.stream()
            .filter(elementsWithInvalidResponse())
            .map(element -> Element.<Direction>builder()
                .id(element.getId())
                .value(element.getValue().toBuilder()
                    .response(element.getValue().getResponse().toBuilder()
                        .assignee(assignee)
                        .directionId(element.getId())
                        .build())
                    .build())
                .build())
            .collect(Collectors.toList());
    }

    private Predicate<Element<Direction>> elementsWithInvalidResponse() {
        return element -> isNotEmpty(element.getValue().getResponse())
            && isNotEmpty(element.getValue().getResponse().getComplied());
    }

    /**
     * Splits a list of directions into a map where the key is the role of the direction assignee and the value is the
     * list of directions belonging to the role.
     *
     * @param directions a list of directions with various assignees.
     * @return Map of role name, list of directions.
     */
    public Map<DirectionAssignee, List<Element<Direction>>> sortDirectionsByAssignee(
        List<Element<Direction>> directions) {
        return directions.stream()
            .collect(groupingBy(directionElement -> directionElement.getValue().getAssignee()));
    }

    /**
     * Removes directions where custom is set to Yes.
     *
     * @param directions any list of directions.
     * @return a list of directions that are not custom.
     */
    public List<Element<Direction>> removeCustomDirections(List<Element<Direction>> directions) {
        return directions.stream()
            .filter(element -> !"Yes".equals(element.getValue().getCustom()))
            .collect(Collectors.toList());
    }

    /**
     * Filters a list of directions to return only the directions belonging to specific assignee.
     *
     * @param directions a list of directions.
     * @param assignee   the assignee of the directions to be returned.
     * @return a list of directions belonging to the assignee.
     */
    public List<Element<Direction>> getDirectionsForAssignee(List<Element<Direction>> directions,
                                                             DirectionAssignee assignee) {
        return directions.stream()
            .filter(element -> element.getValue().getAssignee().equals(assignee))
            .collect(Collectors.toList());
    }

    /**
     * Iterates over a list of directions and adds numbers to the directionType starting from 2.
     *
     * @param directions a list of directions.
     * @return a list of directions with numbered directionType.
     */
    public List<Element<Direction>> numberDirections(List<Element<Direction>> directions) {
        AtomicInteger at = new AtomicInteger(2);

        return directions.stream()
            .map(direction -> {
                Direction.DirectionBuilder directionBuilder = direction.getValue().toBuilder();

                return Element.<Direction>builder()
                    .id(direction.getId())
                    .value(directionBuilder
                        .directionType(at.getAndIncrement() + ". " + direction.getValue().getDirectionType())
                        .build())
                    .build();
            })
            .collect(toList());
    }

    /**
     * Takes a direction from a configuration file and builds a CCD direction.
     *
     * @param direction  the direction taken from json config.
     * @param completeBy the date to be completed by. Can be null.
     * @return Direction to be stored in CCD.
     */
    public Element<Direction> constructDirectionForCCD(DirectionConfiguration direction, LocalDateTime completeBy) {
        return Element.<Direction>builder()
            .id(randomUUID())
            .value(Direction.builder()
                .directionType(direction.getTitle())
                .directionText(direction.getText())
                .assignee(direction.getAssignee())
                .directionRemovable(booleanToYesOrNo(direction.getDisplay().isDirectionRemovable()))
                .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
                .dateToBeCompletedBy(completeBy)
                .build())
            .build();
    }

    /**
     * Iterates over a list of directions and sets properties assignee, custom and readOnly.
     *
     * @param directions a list of directions.
     * @param assignee   the assignee of the directions to be returned.
     * @return A list of custom directions.
     */
    public List<Element<Direction>> assignCustomDirections(List<Element<Direction>> directions,
                                                           DirectionAssignee assignee) {
        if (!isNull(directions)) {
            return directions.stream()
                .map(element -> Element.<Direction>builder()
                    .id(element.getId())
                    .value(element.getValue().toBuilder()
                        .assignee(assignee)
                        .custom("Yes")
                        .readOnly("No")
                        .build())
                    .build())
                .collect(toList());
        } else {
            return emptyList();
        }
    }

    /**
     * Returns a list of directions to comply with.
     *
     * @param caseData case data with standard directions and case management order directions.
     * @return most recent directions that need to be complied with.
     */
    public List<Element<Direction>> getDirectionsToComplyWith(CaseData caseData) {
        if (caseData.getServedCaseManagementOrders().isEmpty()) {
            return caseData.getStandardDirectionOrder().getDirections();
        } else {
            return caseData.getServedCaseManagementOrders().get(0).getValue().getDirections();
        }
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
