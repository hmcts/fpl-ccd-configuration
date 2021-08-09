package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor
public class PeopleInCaseService {
    private static final String COMMA_DELIMITER = ", ";

    public String buildPeopleInCaseLabel(List<Element<Respondent>> respondents,
                                         Others others) {
        boolean hasNoOthers = isNull(others) || !others.hasOthers();
        if (isEmpty(respondents) && hasNoOthers) {
            return "No respondents and others on the case";
        } else {
            StringBuilder sb = new StringBuilder();

            if (isNotEmpty(respondents)) {
                String respondentLabel = buildRespondentsLabel(respondents);
                sb.append(respondentLabel);
            }

            if (!hasNoOthers) {
                int othersStartIndex = respondents.size() + 1;
                String othersLabel = buildOthersLabel(others, othersStartIndex);
                sb.append(othersLabel);
            }
            return sb.toString();
        }
    }

    private String buildOthersLabel(Others others, int personIndex) {
        int otherIndex = 1;
        StringBuilder sb = new StringBuilder();
        if (others.getFirstOther() != null) {
            sb.append(String.format("Person %d: Other %d - %s",
                personIndex, otherIndex, getOtherPersonName(others.getFirstOther()))).append("\n");
            personIndex++;
            otherIndex++;
        }

        if (others.getAdditionalOthers() != null) {
            for (int i = 0; i < others.getAdditionalOthers().size(); i++) {
                Other other = others.getAdditionalOthers().get(i).getValue();

                sb.append(String.format("Person %d: Other %d - %s",
                    personIndex, otherIndex, getOtherPersonName(other))).append("\n");
                personIndex++;
                otherIndex++;
            }
        }

        return sb.toString();
    }

    private String buildRespondentsLabel(List<Element<Respondent>> respondents) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < respondents.size(); i++) {
            RespondentParty respondentParty = respondents.get(i).getValue().getParty();

            sb.append(String.format("Person %d: Respondent %d - %s", i + 1, i + 1,
                isNotEmpty(respondentParty) ? respondentParty.getFullName() : EMPTY)).append("\n");
        }
        return sb.toString();
    }

    public List<Element<Other>> getSelectedOthers(CaseData caseData) {
        final List<Element<Respondent>> respondents = caseData.getAllRespondents();
        final List<Element<Other>> others = caseData.getAllOthers();
        final Selector selector = caseData.getPersonSelector();
        final String allPeopleSelected = caseData.getNotifyApplicationsToAllOthers();

        if (useAllPeopleInTheCase(allPeopleSelected)) {
            return others;
        } else {
            if (isNull(selector) || isEmpty(selector.getSelected())) {
                return Collections.emptyList();
            }

            List<Element<Other>> selectedOthers = new ArrayList<>();
            List<Integer> selected = selector.getSelected();

            int othersIndex = 0;
            int startIndexForOthersInSelector = isEmpty(respondents) ? 0 : respondents.size();
            int lastIndexForOthersInSelector = startIndexForOthersInSelector + others.size();

            for (int i = startIndexForOthersInSelector; i < lastIndexForOthersInSelector; i++) {
                if (selected.contains(i)) {
                    selectedOthers.add(others.get(othersIndex));
                }
                othersIndex++;
            }
            return selectedOthers;
        }
    }

    public List<Element<Respondent>> getSelectedRespondents(CaseData caseData) {
        final List<Element<Respondent>> respondents = caseData.getAllRespondents();
        final Selector selector = caseData.getPersonSelector();
        final String allPeopleSelected = caseData.getNotifyApplicationsToAllOthers();

        if (useAllPeopleInTheCase(allPeopleSelected)) {
            return respondents;
        } else {
            if (isNull(selector) || isEmpty(selector.getSelected())) {
                return Collections.emptyList();
            }

            List<Element<Respondent>> selectedRespondents = new ArrayList<>();
            List<Integer> selected = selector.getSelected();
            for (int i = 0; i < respondents.size(); i++) {
                if (selected.contains(i)) {
                    selectedRespondents.add(respondents.get(i));
                }
            }
            return selectedRespondents;
        }
    }

    public String getPeopleNotified(List<Element<Representative>> allRepresentatives,
                                    List<Element<Respondent>> selectedRespondents,
                                    List<Element<Other>> selectedOthers) {
        StringBuilder sb = new StringBuilder();
        String respondentsNotified = getSelectedRespondentsNames(allRepresentatives, selectedRespondents);
        String othersNotified = getSelectedOthersNames(selectedOthers);
        if (isNotEmpty(respondentsNotified)) {
            sb.append(respondentsNotified);
        }

        if (isNotEmpty(othersNotified)) {
            if (isNotEmpty(respondentsNotified)) {
                sb.append(COMMA_DELIMITER);
            }
            sb.append(othersNotified);
        }
        return sb.toString();
    }

    private String getSelectedRespondentsNames(List<Element<Representative>> representatives,
                                               List<Element<Respondent>> selectedRespondents) {
        return unwrapElements(selectedRespondents).stream()
            .map(respondent -> respondent.getParty().getFullName())
            .collect(Collectors.joining(COMMA_DELIMITER));
    }

    private String getSelectedOthersNames(List<Element<Other>> selectedOthers) {
        return Optional.ofNullable(selectedOthers).map(
            others -> unwrapElements(others).stream()
                .map(this::getOtherPersonName)
                .collect(Collectors.joining(COMMA_DELIMITER))
        ).orElse(EMPTY);
    }

    private String getOtherPersonName(Other other) {
        return defaultIfNull(other.getName(), "");
    }

    private boolean useAllPeopleInTheCase(String sendOrdersToAllPeopleInCase) {
        return "Yes".equals(sendOrdersToAllPeopleInCase);
    }
}
