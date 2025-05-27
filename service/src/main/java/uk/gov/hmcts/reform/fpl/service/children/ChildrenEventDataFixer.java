package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.fromString;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInOpenState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInReturnedState;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ChildrenEventDataFixer {

    private static final String HAVE_SAME_REPRESENTATION_KEY = "childrenHaveSameRepresentation";
    private static final String ALL_HAVE_SAME_REPRESENTATION = "Yes";

    private final CaseConverter converter;

    public CaseDetails fixRepresentationDetails(final CaseDetails caseDetails) {
        if (isInOpenState(caseDetails) || isInReturnedState(caseDetails)) {
            return caseDetails;
        }

        final CaseData caseData = converter.convert(caseDetails);
        final List<Element<Child>> children = caseData.getAllChildren();
        final ChildrenEventData eventData = caseData.getChildrenEventData();
        final YesNo haveRepresentation = YesNo.fromString(eventData.getChildrenHaveRepresentation());

        if (YES == haveRepresentation && 1 == children.size()) {
            // directly adding to the map to ensure that it is persisted in the case data when returning to ccd
            caseDetails.getData().put(HAVE_SAME_REPRESENTATION_KEY, ALL_HAVE_SAME_REPRESENTATION);
        }

        return caseDetails;
    }

    public void fixPersistentChildDetails(CaseDetails caseDetails) {
        final CaseData caseData = converter.convert(caseDetails);

        List<Element<Child>> updatedChildren = caseData.getAllChildren().stream().map(childElement -> {
            ChildParty updatedParty = nullifyUnusedChildFields(childElement.getValue().getParty());
            Child updatedChild = childElement.getValue().toBuilder().party(updatedParty).build();

            return childElement.toBuilder().value(updatedChild).build();
        }).toList();

        caseDetails.getData().put("children1", updatedChildren);
    }

    public ChildParty nullifyUnusedChildFields(ChildParty childParty) {
        // Auto nullify all fields persisting in the living situations section
        ChildParty.ChildPartyBuilder childPartyBuilder = childParty.toBuilder()
            .dischargeDate(null)
            .datePowersEnd(null)
            .careStartDate(null)
            .livingSituationDetails(null)
            .addressChangeDate(null)
            .livingWithDetails(null)
            .address(null);

        // Only add back details that are required for the selected situation
        switch (fromString(defaultIfNull(childParty.getLivingSituation(), ""))) {
            case HOSPITAL_SOON_TO_BE_DISCHARGED:
                childPartyBuilder.dischargeDate(childParty.getDischargeDate());
                break;
            case REMOVED_BY_POLICE_POWER_ENDS:
                childPartyBuilder.datePowersEnd(childParty.getDatePowersEnd());
                break;
            case VOLUNTARILY_SECTION_CARE_ORDER, UNDER_CARE_OF_LA:
                childPartyBuilder.careStartDate(childParty.getCareStartDate());
                break;
            case LIVE_WITH_FAMILY_OR_FRIENDS:
                childPartyBuilder.livingWithDetails(childParty.getLivingWithDetails())
                        .addressChangeDate(childParty.getAddressChangeDate())
                        .address(childParty.getAddress());
                break;
            case LIVE_IN_REFUGE, LIVING_WITH_RESPONDENTS:
                childPartyBuilder.address(childParty.getAddress());
                break;
            case NOT_SPECIFIED:
                childPartyBuilder.livingSituationDetails(childParty.getLivingSituationDetails());
                break;
            default:
                break;
        }

        return childPartyBuilder.build();
    }
}
