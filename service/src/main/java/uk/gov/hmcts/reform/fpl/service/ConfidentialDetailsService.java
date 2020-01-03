package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.getCaseDataKeyFromClass;

@Service
public class ConfidentialDetailsService {
    private final ObjectMapper mapper;

    public ConfidentialDetailsService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> List<Element<T>> addPartyMarkedConfidentialToList(Class<T> personType, List<Element<T>> details) {
        List<Element<T>> confidentialList = new ArrayList<>();

        details.stream()
            .filter(element -> element.getValue() != null)
            .forEach(value -> {
                if (personType == Respondent.class) {
                    if (respondentDetailsAreHidden(value)) {
                        confidentialList.add(value);
                    }
                }
                if (personType == Child.class) {
                    if (childDetailsAreHidden(value)) {
                        confidentialList.add(value);
                    }
                }
            });

        return confidentialList;
    }

    private <T> boolean respondentDetailsAreHidden(Element<T> element) {
        Respondent respondent = mapper.convertValue(element.getValue(), Respondent.class);
        return respondent.containsConfidentialDetails();
    }

    private <T> boolean childDetailsAreHidden(Element<T> element) {
        Child child = mapper.convertValue(element.getValue(), Child.class);
        return child.getParty().getDetailsHidden() != null && child.containsConfidentialDetails();
    }

    public <T> void addConfidentialDetailsToCaseDetails(CaseDetails caseDetails,
                                                        List<Element<T>> confidentialDetails,
                                                        Class<T> personType) {
        String key = getCaseDataKeyFromClass(personType);

        if (isNotEmpty(confidentialDetails)) {
            caseDetails.getData().put(key, confidentialDetails);
        } else {
            caseDetails.getData().remove(key);
        }
    }
}
