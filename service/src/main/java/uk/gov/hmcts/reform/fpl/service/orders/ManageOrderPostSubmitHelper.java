package uk.gov.hmcts.reform.fpl.service.orders;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier.ManageOrdersCaseDataFixer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrderPostSubmitHelper {

    private final ManageOrdersCaseDataFixer manageOrdersCaseDataFixer;
    private final CaseConverter caseConverter;
    private final OrderProcessingService orderProcessing;

    public Map<String, Object> getPostSubmitUpdates(CaseDetails caseDetails) {
        CaseDetails updatedDetails = manageOrdersCaseDataFixer.fixAndRetriveCaseDetails(caseDetails);
        CaseData fixedCaseData = manageOrdersCaseDataFixer.fix(caseConverter.convert(updatedDetails));

        Map<String, Object> caseDataUpdates = new HashMap<>();
        try {
            caseDataUpdates = orderProcessing.postProcessDocument(fixedCaseData);
        } catch (Exception exception) {
            log.error("Error while processing manage orders document for case id {}.",
                updatedDetails.getId(), exception);
        }
        return caseDataUpdates;
    }
}
