package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Guardian;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiGuardianService {
    private final CaseConverter caseConverter;
    private final CoreCaseDataService coreCaseDataService;

    public boolean checkIfAnyGuardianUpdated(CaseData caseData, List<Guardian> guardianUpdateList) {
        List<Element<Guardian>> existingGuardians = caseData.getGuardians();

        if (!isEmpty(guardianUpdateList)) {
            return !(new HashSet<>(guardianUpdateList).equals(new HashSet<>(unwrapElements(existingGuardians))));
        } else {
            return !isEmpty(existingGuardians);
        }
    }

    public void updateGuardians(CaseData caseData, List<Guardian> guardianUpdateList) {
        coreCaseDataService.performPostSubmitCallback(caseData.getId(), "internal-update-guardians",
            caseDetails -> Map.of("guardians", wrapElementsWithUUIDs(guardianUpdateList)));

        // TODO publish event to send notification
    }
}
