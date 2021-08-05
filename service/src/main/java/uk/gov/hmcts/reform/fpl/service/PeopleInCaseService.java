package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor
public class PeopleInCaseService {
    private static final String COMMA_DELIMITER = ", ";

    private final OthersService othersService;
    private final RespondentService respondentService;

    public String buildPeopleInCaseLabel(List<Element<Respondent>> respondents,
                                         Others others) {
        boolean hasNoOthers = isNull(others) || !others.hasOthers();
        if (isEmpty(respondents) && hasNoOthers) {
            return "No respondents and others on the case";
        } else {
            StringBuilder sb = new StringBuilder();

            if (isNotEmpty(respondents)) {
                String respondentLabel = respondentService.buildRespondentLabel(respondents);
                sb.append(respondentLabel);
            }

            if (!hasNoOthers) {
                String othersLabel = othersService.buildOthersLabel(others);
                sb.append(othersLabel);
            }
            return sb.toString();
        }
    }

    public List<Element<Other>> getSelectedOthers(CaseData caseData) {
        final List<Element<Respondent>> respondents = caseData.getAllRespondents();
        final List<Element<Other>> others = caseData.getAllOthers();
        final Selector selector = caseData.getOthersSelector();
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
        final Selector selector = caseData.getOthersSelector();
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
            .filter(respondent -> hasRepresentativeDetails(
                representatives, unwrapElements(respondent.getRepresentedBy()))
                || respondent.hasAddress())
            .map(respondent -> respondent.getParty().getFullName())
            .collect(Collectors.joining(COMMA_DELIMITER));
    }

    private boolean hasRepresentativeDetails(List<Element<Representative>> representatives,
                                             List<UUID> representedBy) {
        return nullSafeList(representatives).stream()
            .filter(element -> representedBy.contains(element.getId()))
            .map(Element::getValue)
            .anyMatch(representative -> validAddressForNotificationByPost(representative)
                || validEmailForDigitalOrEmailNotification(representative));
    }

    private boolean validEmailForDigitalOrEmailNotification(final Representative element) {
        return (element.getServingPreferences() == DIGITAL_SERVICE || element.getServingPreferences() == EMAIL)
            && isNotEmpty(element.getEmail());
    }

    private boolean validAddressForNotificationByPost(Representative representative) {
        return representative.getServingPreferences() == POST
            && isNotEmpty(representative.getAddress())
            && isNotEmpty(representative.getAddress().getPostcode());
    }

    private String getSelectedOthersNames(List<Element<Other>> selectedOthers) {
        return Optional.ofNullable(selectedOthers).map(
            others -> unwrapElements(others).stream()
                .filter(other -> other.isRepresented() || other.hasAddressAdded())
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
