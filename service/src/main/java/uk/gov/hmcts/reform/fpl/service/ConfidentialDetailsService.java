package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class ConfidentialDetailsService {

    public <T extends ConfidentialParty<T>> List<Element<T>> addPartyMarkedConfidentialToList(
        List<Element<T>> details) {
        return details.stream()
            .filter(element -> element.getValue() != null)
            .filter(element -> element.getValue().containsConfidentialDetails())
            .collect(toList());
    }

    public <T> void addConfidentialDetailsToCase(CaseDetails caseDetails,
                                                 List<Element<T>> confidentialDetails,
                                                 ConfidentialPartyType type) {
        if (isNotEmpty(confidentialDetails)) {
            caseDetails.getData().put(type.getCaseDataKey(), confidentialDetails);
        } else {
            caseDetails.getData().remove(type.getCaseDataKey());
        }
    }

    public <T extends ConfidentialParty<T>> List<Element<T>> retainConfidentialDetails(
        List<Element<T>> list, Supplier<T> builder) {
        List<Element<T>> modified = new ArrayList<>();

        list.forEach(element -> {
            Party party = element.getValue().getConfidentialParty();
            T t = builder.get();
            t = t.setConfidentialParty(party);
            modified.add(element(element.getId(), t));
        });

        return modified;
    }
}
