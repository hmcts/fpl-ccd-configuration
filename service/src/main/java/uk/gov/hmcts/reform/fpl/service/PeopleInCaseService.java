package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
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

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PeopleInCaseService {
    public static final String NO_RESPONDENTS_ON_THE_CASE = "No respondents on the case";
    public static final String NO_OTHERS_ON_THE_CASE = "No others on the case";
    private final OthersService othersService;
    private final RespondentService respondentService;

    public String buildPeopleInCaseLabel(List<Element<Respondent>> respondents,
                                         Others others) {
        StringBuilder sb = new StringBuilder();

        if (isEmpty(respondents) && isEmpty(others)) {
            return "No respondents and others on the case";
        } else {
            String respondentLabel = respondentService.buildRespondentLabel(respondents);
            String othersLabel = othersService.buildOthersLabel(others);

            if (!NO_RESPONDENTS_ON_THE_CASE.equals(respondentLabel)) {
                sb.append(respondentLabel);
            }

            if (!NO_OTHERS_ON_THE_CASE.equals(othersLabel)) {
                sb.append(othersLabel);
            }

            return sb.toString();
        }
    }

    public List<Element<Other>> getSelectedOthers(List<Element<Respondent>> respondents,
                                                  List<Element<Other>> others,
                                                  Selector selector,
                                                  String allPeopleSelected) {

        if (useAllPeopleInTheCase(allPeopleSelected)) {
            return others;
        } else {
            if (isNull(selector) || isEmpty(selector.getSelected())) {
                return Collections.emptyList();
            }

            List<Element<Other>> selectedOthers = new ArrayList<>();
            List<Integer> selected = selector.getSelected();

            int othersIndex = 0;
            int index = respondents.size() == 0 ? 0 : respondents.size();
            for (int i = index; i < (index + others.size() - 1); i++) {
                if (selected.contains(i)) {
                    selectedOthers.add(others.get(othersIndex));
                }
            }
            return selectedOthers;
        }
    }

    public List<Element<Respondent>> getSelectedRespondents(List<Element<Respondent>> respondents,
                                                            Selector selector,
                                                            String allPeopleSelected) {
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

    public String getPeopleNotified(List<Element<Respondent>> selectedRespondents,
                                    List<Element<Other>> selectedOthers) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSelectedRespondents(selectedRespondents));
        String othersNotified = getSelectedOthers(selectedOthers);
        if (isNotEmpty(othersNotified) && sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(othersNotified);
        return sb.toString();
    }

    private String getSelectedRespondents(List<Element<Respondent>> selectedRespondents) {
        //List<UUID> repUuids = representatives.stream().map(Element::getId).collect(Collectors.toList());
        return Optional.ofNullable(selectedRespondents).map(
            respondent -> respondent.stream()
                .filter(respondentElement -> !isNull(respondentElement.getValue().getRepresentedBy()))
                .map(respondentElement -> getRespondentFullName(respondentElement.getValue().getParty()))
                .collect(Collectors.joining(", "))
        ).orElse("");
    }

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }

    private String getSelectedOthers(List<Element<Other>> selectedOthers) {
        return Optional.ofNullable(selectedOthers).map(
            others -> others.stream()
                .filter(other -> other.getValue().isRepresented() || other.getValue()
                    .hasAddressAdded())
                .map(other -> other.getValue().getName()).collect(Collectors.joining(", "))
        ).orElse("");
    }

    private boolean useAllPeopleInTheCase(String sendOrdersToAllPeopleInCase) {
        return "Yes".equals(sendOrdersToAllPeopleInCase);
    }
}
