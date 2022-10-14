package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartyListGenerator {
    private final DynamicListService dynamicLists;

    public DynamicList buildPartyList(CaseData caseData) {

        List<Element<String>> labels = new ArrayList<>();

        labels.addAll(buildRespondentElements(caseData.getAllRespondents()));
        labels.addAll(buildChildElement(caseData.getAllChildren()));
        labels.addAll(buildApplicantElements(caseData.getLocalAuthorities()));

        return dynamicLists.asDynamicList(
            labels,
            el -> el.getId().toString(),
            Element::getValue);
    }

    private List<Element<String>> buildChildElement(List<Element<Child>> children) {
        return children.stream()
            .map(child -> element(child.getId(), "Child - " + child.getValue().toParty().getFullName()))
            .collect(Collectors.toList());
    }

    private List<Element<String>> buildRespondentElements(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(respondent -> element(respondent.getId(),
                "Respondent - " + respondent.getValue().toParty().getFullName()))
            .collect(Collectors.toList());
    }

    private List<Element<String>> buildApplicantElements(List<Element<LocalAuthority>> localAuthorities) {
        return localAuthorities.stream()
            .map(localAuthority -> element(localAuthority.getId(),
                "Applicant - " + localAuthority.getValue().getName()))
            .collect(Collectors.toList());
    }
}
