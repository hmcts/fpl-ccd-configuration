package uk.gov.hmcts.reform.fpl.service;

import com.mchange.v2.util.CollectionUtils;
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

    public String getAppointedGuardiansLabel(List<Element<Respondent>> respondents,
                                             List<Element<Other>> others) {
        if (isEmpty(respondents) && isEmpty(others)) {
            return "No respondents or others on the case";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < CollectionUtils.size(respondents); i++) {
            Respondent respondent = respondents.get(i).getValue();

            builder.append(String.format("Respondent %d: %s", i + 1, respondent.getParty().getFullName()));
            builder.append("\n");
        }

        for (int i = 0; i < CollectionUtils.size(others); i++) {
            Other other = others.get(i).getValue();

            builder.append(String.format("Other %d: %s", i + 1, other.getName()));
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
