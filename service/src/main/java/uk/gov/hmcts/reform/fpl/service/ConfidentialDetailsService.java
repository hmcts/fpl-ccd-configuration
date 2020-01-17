package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class ConfidentialDetailsService {

    public <T extends ConfidentialParty> List<Element<T>> addPartyMarkedConfidentialToList(List<Element<T>> details) {
        return details.stream()
            .filter(element -> element.getValue() != null)
            .filter(element -> element.getValue().containsConfidentialDetails())
            .collect(toList());
    }

    public <T> void addConfidentialDetailsToCaseDetails(CaseDetails caseDetails,
                                                        List<Element<T>> confidentialDetails,
                                                        ConfidentialPartyType type) {
        if (isNotEmpty(confidentialDetails)) {
            caseDetails.getData().put(type.getCaseDataKey(), confidentialDetails);
        } else {
            caseDetails.getData().remove(type.getCaseDataKey());
        }
    }
}
