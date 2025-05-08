package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
public class RespondentsCommonFormatHelper {

    private RespondentsCommonFormatHelper() {
        // NO-OP
    }

    public static String getRespondentsLabel(CaseData caseData) {
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

    public static String getRespondentsForTab(CaseData caseData, Selector orderSelector) {
        StringBuilder builder = new StringBuilder();
        List<String> selected = getSelectedARespondents(caseData, orderSelector, caseData.getManageOrdersEventData()
            .getAdditionalAppointedSpecialGuardians());

        if (selected.isEmpty()) {
            return null;
        }

        selected.forEach(builder::append);

        return builder.toString();
    }

    public static List<String> getSelectedARespondents(CaseData caseData, Selector orderSelector) {
        return getSelectedARespondents(caseData, orderSelector, null);
    }

    public static List<String> getSelectedARespondents(CaseData caseData, Selector orderSelector,
                                                       String additionalNamesSeparatedByNewline) {
        List<String> selectedApplicants = new ArrayList<>();
        List<String> selected;

        Stream<String> respondentsNames = caseData.getAllRespondents().stream()
            .map(respondent -> respondent.getValue().getParty().getFullName());

        Stream<String> othersNames = caseData.getAllOthers().stream()
            .map(other -> other.getValue().getName());

        List<String> respondentsAndOthersNames = Stream.concat(respondentsNames, othersNames).collect(
            Collectors.toList());

        selected = defaultIfNull(orderSelector, Selector.builder().build())
                .getSelected().stream()
                .map(respondentsAndOthersNames::get)
                .collect(Collectors.toList());

        if (isNotEmpty(additionalNamesSeparatedByNewline)) {
            selected.addAll(Arrays.asList(additionalNamesSeparatedByNewline.split("\n")));
        }

        for (int i = 0; i < selected.size(); i++) {
            String name = selected.get(i);
            if (i >= 1) {
                String text = (selected.size() - 1) == i ? " and %s" : ", %s";
                selectedApplicants.add(String.format(text, name));
            } else {
                selectedApplicants.add(String.format("%s", name));
            }
        }
        return selectedApplicants;
    }
}
