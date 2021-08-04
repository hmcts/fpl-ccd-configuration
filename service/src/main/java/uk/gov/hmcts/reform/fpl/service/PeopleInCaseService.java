package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Address;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PeopleInCaseService {
    private static final String NO_RESPONDENTS_ON_THE_CASE = "No respondents on the case";
    private static final String NO_OTHERS_ON_THE_CASE = "No others on the case";
    private static final String COMMA_DELIMITER = ", ";

    private final OthersService othersService;
    private final RespondentService respondentService;

    public String buildPeopleInCaseLabel(List<Element<Respondent>> respondents,
                                         Others others) {
        StringBuilder sb = new StringBuilder();

        if (isEmpty(respondents) && isNull(others.getFirstOther())) {
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
            int respondentsSize = isEmpty(respondents) ? 0 : respondents.size();
            for (int i = respondentsSize; i < (respondentsSize + others.size()); i++) {
                if (selected.contains(i)) {
                    selectedOthers.add(others.get(othersIndex));
                }
                othersIndex++;
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

    public String getPeopleNotified(List<Element<Representative>> allRepresentatives,
                                    List<Element<Respondent>> selectedRespondents,
                                    List<Element<Other>> selectedOthers) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSelectedRespondentsNames(allRepresentatives, selectedRespondents));
        String othersNotified = getSelectedOthersNames(selectedOthers);
        if (isNotEmpty(othersNotified) && sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(othersNotified);
        return sb.toString();
    }

    private String getSelectedRespondentsNames(List<Element<Representative>> representatives,
                                               List<Element<Respondent>> selectedRespondents) {
        return Optional.ofNullable(selectedRespondents)
            .map(respondent -> respondent.stream()
                .filter(respondentElement -> hasRepresentativeDetails(representatives,
                    unwrapElements(respondentElement.getValue().getRepresentedBy())) || hasAddress(respondentElement))
                .map(respondentElement -> getRespondentFullName(respondentElement.getValue().getParty()))
                .collect(Collectors.joining(COMMA_DELIMITER)))
            .orElse(EMPTY);
    }

    private boolean hasAddress(Element<Respondent> respondentElement) {
        Address address = respondentElement.getValue().getParty().getAddress();
        return isNotEmpty(address) && isNotEmpty(address.getPostcode());
    }

    private boolean hasRepresentativeDetails(List<Element<Representative>> representatives,
                                             List<UUID> representedBy) {
        return nullSafeList(representatives).stream()
            .filter(element -> representedBy.contains(element.getId()))
            .anyMatch(element -> validAddressForNotificationByPost(element.getValue())
                || validEmailForDigitalOrEmailNotification(element.getValue()));
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

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), EMPTY);
        String lastName = defaultIfNull(respondentParty.getLastName(), EMPTY);

        return String.format("%s %s", firstName, lastName);
    }

    private String getSelectedOthersNames(List<Element<Other>> selectedOthers) {
        return Optional.ofNullable(selectedOthers).map(
            others -> others.stream()
                .filter(other -> other.getValue().isRepresented() || other.getValue()
                    .hasAddressAdded())
                .map(this::getOtherPersonName)
                .collect(Collectors.joining(COMMA_DELIMITER))
        ).orElse(EMPTY);
    }

    private String getOtherPersonName(Element<Other> other) {
        return defaultIfNull(other.getValue().getName(), "");
    }

    private boolean useAllPeopleInTheCase(String sendOrdersToAllPeopleInCase) {
        return "Yes".equals(sendOrdersToAllPeopleInCase);
    }
}
