package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;

import java.util.List;

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
}
