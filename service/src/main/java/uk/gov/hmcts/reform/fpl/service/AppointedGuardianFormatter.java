package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppointedGuardianFormatter {

    public String getGuardiansLabel(CaseData caseData) {
        List<Element<Respondent>> respondents = caseData.getAllRespondents();
        List<Element<Other>> others = caseData.getAllOthers();
        if (isEmpty(respondents) && isEmpty(others)) {
            return "No respondents or others to be given notice on the case";
        }

        Stream<String> respondentsNames = respondents.stream()
            .map(respondent -> "Respondent - " + respondent.getValue().getParty().getFullName());

        Stream<String> othersNames = others.stream()
            .map(other -> "Other - " + other.getValue().getName());

        List<String> respondentsAndOthersNames = Stream.concat(respondentsNames, othersNames).collect(
            Collectors.toList());
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < respondentsAndOthersNames.size(); i++) {
            builder.append(String.format("Person %d: %s", i + 1, respondentsAndOthersNames.get(i)));
            builder.append("\n");
        }

        return builder.toString();
    }

    public String getGuardiansNamesForDocument(CaseData caseData) {
        StringBuilder builder = new StringBuilder();
        List<String> selected = getSelectedApplicants(caseData);

        selected.forEach(builder::append);
        appendChildGrammarVerb(builder, selected.size() > 1);

        return builder.toString();
    }

    public String getGuardiansNamesForTab(CaseData caseData) {
        StringBuilder builder = new StringBuilder();
        List<String> selected = getSelectedApplicants(caseData);

        if (selected.isEmpty()) {
            return null;
        }

        selected.forEach(builder::append);

        return builder.toString();
    }

    private List<String> getSelectedApplicants(CaseData caseData) {
        List<String> selectedApplicants = new ArrayList<>();

        Stream<String> respondentsNames = caseData.getAllRespondents().stream()
            .map(respondent -> respondent.getValue().getParty().getFullName());

        Stream<String> othersNames = caseData.getAllOthers().stream()
            .map(other -> other.getValue().getName());

        List<String> respondentsAndOthersNames = Stream.concat(respondentsNames, othersNames).collect(
            Collectors.toList());

        List<String> selected = defaultIfNull(caseData.getAppointedGuardianSelector(), Selector.builder().build())
            .getSelected().stream()
            .map(respondentsAndOthersNames::get)
            .collect(Collectors.toList());

        for (int i = 0; i < selected.size(); i++) {
            String name = selected.get(i);

            if (i >= 1) {
                selectedApplicants.add(String.format(", %s", name));
            } else {
                selectedApplicants.add(String.format("%s", name));
            }
        }
        return selectedApplicants;
    }

    private static void appendChildGrammarVerb(StringBuilder builder, boolean hasMultipleGuardiansGrammar) {
        if (builder.toString().isEmpty()) {
            return;
        }
        if (hasMultipleGuardiansGrammar) {
            builder.append(" are");
        } else {
            builder.append(" is");
        }
    }

}
