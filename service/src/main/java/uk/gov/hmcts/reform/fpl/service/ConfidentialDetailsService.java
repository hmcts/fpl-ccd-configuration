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
            .filter(x -> x.getValue() != null)
            .forEach(x -> {
                if (personType == Respondent.class) {
                    if (respondentDetailsHidden(x)) {
                        confidentialList.add(x);
                    }
                }
                if (personType == Child.class) {
                    if (childDetailsHidden(x)) {
                        confidentialList.add(x);
                    }
                }
            });

        return confidentialList;
    }

    private <T> boolean respondentDetailsHidden(Element<T> x) {
        Respondent respondent = mapper.convertValue(x.getValue(), Respondent.class);
        return respondent.getParty() != null && respondent.getParty().getContactDetailsHidden() != null
            && respondent.getParty().getContactDetailsHidden().equals("Yes");
    }

    private <T> boolean childDetailsHidden(Element<T> x) {
        Child child = mapper.convertValue(x.getValue(), Child.class);
        return child.getParty() != null && child.getParty().getDetailsHidden() != null
            && child.getParty().getDetailsHidden().equals("Yes");
    }
}
