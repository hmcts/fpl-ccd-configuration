package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@Service
public class DirectionHelperService {

    public Order createOrder(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(filterDirectionsNotRequired(caseData.getAllParties()));

        if (!isNull(caseData.getAllPartiesCustom())) {
            directions.addAll(assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getLocalAuthorityDirections()));

        if (!isNull(caseData.getLocalAuthorityDirectionsCustom())) {
            directions.addAll(assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getParentsAndRespondentsDirections()));

        if (!isNull(caseData.getParentsAndRespondentsCustom())) {
            directions.addAll(
                assignCustomDirections(caseData.getParentsAndRespondentsCustom(), PARENTS_AND_RESPONDENTS));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getCafcassDirections()));

        if (!isNull(caseData.getCafcassDirectionsCustom())) {
            directions.addAll(assignCustomDirections(caseData.getCafcassDirectionsCustom(), CAFCASS));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getOtherPartiesDirections()));

        if (!isNull(caseData.getOtherPartiesDirectionsCustom())) {
            directions.addAll(assignCustomDirections(caseData.getOtherPartiesDirectionsCustom(), OTHERS));
        }

        directions.addAll(filterDirectionsNotRequired(caseData.getCourtDirections()));

        if (!isNull(caseData.getCourtDirectionsCustom())) {
            directions.addAll(assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));
        }

        return Order.builder().directions(directions).build();
    }

    public Order persistHiddenDirectionValues(Order orderWithHiddenValues, Order orderToAddValues) {
        orderToAddValues.getDirections()
            .forEach(directionToAddValue -> orderWithHiddenValues.getDirections()
                .stream()
                .filter(direction -> {
                    System.out.println("direction = " + direction);

                    return direction.getId().equals(directionToAddValue.getId());
                })
                .forEach(direction -> {
                    directionToAddValue.getValue().setReadOnly(direction.getValue().getReadOnly());
                    directionToAddValue.getValue().setDirectionRemovable(direction.getValue().getDirectionRemovable());

                    if (!direction.getValue().getReadOnly().equals("No")) {
                        directionToAddValue.getValue().setText(direction.getValue().getText());
                    }
                }));

        return orderToAddValues;
    }

    public Map<String, List<Element<Direction>>> orderDirectionsByAssignee(List<Element<Direction>> directions) {
        AtomicInteger at = new AtomicInteger(2);
        return directions.stream()
            .filter(x -> x.getValue().getCustom() == null)
            .map(direction -> {
                Direction.DirectionBuilder directionBuilder = direction.getValue().toBuilder();

                return Element.<Direction>builder()
                    .id(direction.getId())
                    .value(directionBuilder.type(at.getAndIncrement() + ". " + direction.getValue().getType()).build())
                    .build();
            })
            .collect(groupingBy(directionElement -> directionElement.getValue().getAssignee().getValue()));
    }

    private List<Element<Direction>> assignCustomDirections(List<Element<Direction>> directions,
                                                            DirectionAssignee assignee) {
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
    }

    // TODO: what do we do with directions where a user has said it is not needed?
    @SuppressWarnings("LineLength")
    private List<Element<Direction>> filterDirectionsNotRequired(List<Element<Direction>> directions) {
        return directions.stream()
            .filter(directionElement -> directionElement.getValue().getDirectionNeeded() == null || directionElement.getValue().getDirectionNeeded().equals("Yes"))
            .collect(toList());
    }

}
