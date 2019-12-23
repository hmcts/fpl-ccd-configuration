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

    private <T> boolean respondentDetailsAreHidden(Element<T> x) {
        Respondent respondent = mapper.convertValue(x.getValue(), Respondent.class);
        return respondent.getParty() != null && respondent.getParty().getContactDetailsHidden() != null
            && respondent.getParty().getContactDetailsHidden().equals("Yes");
    }

    private <T> boolean childDetailsAreHidden(Element<T> x) {
        Child child = mapper.convertValue(x.getValue(), Child.class);
        return child.getParty() != null && child.getParty().getDetailsHidden() != null
            && child.getParty().getDetailsHidden().equals("Yes");
    }
}
