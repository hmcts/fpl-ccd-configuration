package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Guardian;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiGuardianService {
    private final CoreCaseDataService coreCaseDataService;

    public boolean validateGuardians(List<Guardian> guardianUpdateList) {
        return guardianUpdateList.stream()
            .noneMatch(guardian -> isEmpty(guardian.getGuardianName())
                                   || isEmpty(guardian.getChildren())
                                   || guardian.getChildren().stream().anyMatch(String::isEmpty));
    }

    public boolean checkIfAnyGuardianUpdated(CaseData caseData, List<Guardian> guardianUpdateList) {
        List<Element<Guardian>> existingGuardians = caseData.getGuardians();

        return !(new HashSet<>(nullSafeCollection(guardianUpdateList))
            .equals(new HashSet<>(unwrapElements(existingGuardians))));
    }

    public CaseDetails updateGuardians(CaseData caseData, List<Guardian> guardianUpdateList) {
        return coreCaseDataService.performPostSubmitCallback(caseData.getId(), "internal-update-guardians",
            caseDetails -> Map.of("guardians", wrapElementsWithUUIDs(guardianUpdateList)));
    }
}
