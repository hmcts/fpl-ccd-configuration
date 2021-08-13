package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

@Component
public class GatekeepingOrderDataFixer {

    /**
     * This is to create a copy of languageRequirement that can be routed
     * as a hidden field for the urgent hearing. Since there's no page in common,
     * the hidden field needs to be replicated, in order to be loaded by the ui
     * and used in the show-field conditions
     */
    public CaseDetailsMap fix(CaseDetailsMap caseDetailsMap) {

        return caseDetailsMap.putIfNotEmpty("languageRequirementUrgent",
            caseDetailsMap.getOrDefault("languageRequirement", null)
        );
    }
}
