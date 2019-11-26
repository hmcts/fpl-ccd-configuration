package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
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
                direction.getValue().getResponses().removeIf(x ->
                    responseExists(response, direction) && x.getValue().getAssignee().equals(response.getAssignee()));

                direction.getValue().getResponses()
                    .add(Element.<DirectionResponse>builder()
                        .id(UUID.randomUUID())
                        .value(response)
                        .build());
            }));
    }

    private boolean responseExists(DirectionResponse response, Element<Direction> direction) {
        return isNotEmpty(direction.getValue().getResponses()) && response.getDirectionId().equals(direction.getId())
            && direction.getValue().getResponses().stream()
            .anyMatch(directionResponse -> directionResponse.getValue().getAssignee().equals(response.getAssignee()));
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
     * @param directions  a list of directions.
     * @param assignee    the assignee of the directions to be returned.
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

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
