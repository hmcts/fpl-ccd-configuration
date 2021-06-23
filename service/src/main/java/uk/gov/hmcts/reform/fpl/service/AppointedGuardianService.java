package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppointedGuardianService {

    public String getAppointedGuardiansLabel(CaseData caseData) {
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

    public String getAppointedGuardiansNames(CaseData caseData) {

        StringBuilder builder = new StringBuilder();
        boolean hasMultipleGuardiansGrammar = false;

        Stream<String> respondentsNames = caseData.getAllRespondents().stream()
            .map(respondent -> respondent.getValue().getParty().getFullName());

        Stream<String> othersNames = caseData.getAllOthers().stream()
            .map(other -> other.getValue().getName());

        List<String> respondentsAndOthersNames = Stream.concat(respondentsNames, othersNames).collect(
            Collectors.toList());

        List<String> selected = caseData.getAppointedGuardianSelector().getSelected().stream()
            .map(respondentsAndOthersNames::get)
            .collect(Collectors.toList());

        for (int i = 0; i < selected.size(); i++) {
            String name = selected.get(i);

            if (i >= 1) {
                hasMultipleGuardiansGrammar = true;
                builder.append(String.format(", %s", name));
            } else {
                builder.append(String.format("%s", name));
            }
        }

        appendChildGrammarVerb(builder, hasMultipleGuardiansGrammar);

        return builder.toString();
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
