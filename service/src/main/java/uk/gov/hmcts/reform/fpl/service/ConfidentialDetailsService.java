package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

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
}
