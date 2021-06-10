package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionTemplate;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

/**
 * A service that helps with the sorting and editing of directions.
 */
@Service
public class CommonDirectionService {

    /**
     * Combines role directions into a single List of directions.
     *
     * @param caseData data containing all the directions by role.
     * @return directions.
     **/
    public List<Element<StandardDirectionTemplate>> combineAllDirections(CaseData caseData) {
        return
            Stream.of(caseData.getAllParties(),
                getElements(caseData.getAllPartiesCustom(), ALL_PARTIES),
                caseData.getLocalAuthorityDirections(),
                getElements(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY),
                caseData.getRespondentDirections(),
                getElements(caseData.getRespondentDirectionsCustom(), PARENTS_AND_RESPONDENTS),
                caseData.getCafcassDirections(),
                getElements(caseData.getCafcassDirectionsCustom(), CAFCASS),
                caseData.getOtherPartiesDirections(),
                getElements(caseData.getOtherPartiesDirectionsCustom(), OTHERS),
                caseData.getCourtDirections(),
                getElements(caseData.getCourtDirectionsCustom(), COURT))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<Element<StandardDirectionTemplate>> getElements(List<Element<StandardDirectionTemplate>> directions, DirectionAssignee assignee) {
        return ofNullable(directions)
            .map(values -> values.stream()
                .map(element -> element(element.getId(), element.getValue().toBuilder()
                    .assignee(assignee)
                    .custom("Yes")
                    .readOnly("No")
                    .build()))
                .collect(toList()))
            .orElseGet(Collections::emptyList);
    }

    /**
     * Removes directions where custom is set to Yes.
     *
     * @param directions any list of directions.
     * @return a list of directions that are not custom.
     */
    public List<Element<StandardDirectionTemplate>> removeCustomDirections(List<Element<StandardDirectionTemplate>> directions) {
        return directions.stream()
            .filter(element -> !"Yes".equals(element.getValue().getCustom()))
            .collect(toList());
    }

    /**
     * Removes directions where directionNeeded is set to No. Does not remove custom directions.
     *
     * @param directions any list of directions.
     * @return a list of directions that are marked as needed.
     */
    public List<Element<StandardDirectionTemplate>> removeUnnecessaryDirections(List<Element<StandardDirectionTemplate>> directions) {
        return directions.stream()
            .filter(this::removeDirection)
            .collect(toList());
    }

    private boolean removeDirection(Element<StandardDirectionTemplate> element) {
        return "Yes".equals(element.getValue().getDirectionNeeded()) || "Yes".equals(element.getValue().getCustom());
    }
}
