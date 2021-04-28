package uk.gov.hmcts.reform.fpl.service.respondent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentAfterSubmissionValidator {

    public List<String> validateOnApplicationSubmission(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        List<Element<Respondent>> respondents = caseData.getRespondents1();

        for (int i = 0; i < respondents.size(); i++) {
            Respondent respondent = respondents.get(i).getValue();
            errors.addAll(getStandardRespondentErrors(respondent, i));
        }

        return errors;
    }

    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {

        List<String> errors = new ArrayList<>();

        Set<UUID> currentRespondentIds = getIds(caseData.getRespondents1());
        Set<UUID> previousRespondentIds = getIds(nullSafeList(caseDataBefore.getRespondents1()));

        if (!currentRespondentIds.containsAll(previousRespondentIds)) {
            errors.add(" You cannot remove a respondent from the case");
        }

        Map<UUID, Respondent> currentRespondents = getIdRespondentMap(caseData.getRespondents1());

        Map<UUID, Respondent> previousRespondents = getIdRespondentMap(nullSafeList(caseDataBefore.getRespondents1()));

        List<Map.Entry<UUID, Respondent>> currentRespondentsList = new ArrayList<>(currentRespondents.entrySet());

        for (int i = 0; i < currentRespondents.size(); i++) {
            Map.Entry<UUID, Respondent> map = currentRespondentsList.get(i);
            Respondent current = currentRespondentsList.get(i).getValue();
            errors.addAll(getStandardRespondentErrors(current, i));

            Respondent previous = previousRespondents.getOrDefault(map.getKey(), current);

            if (YES.getValue().equals(previous.getLegalRepresentation())
                && NO.getValue().equals(current.getLegalRepresentation())) {
                errors.add(String.format("You cannot remove respondent %d's legal representative", i + 1));
            }

            if (getLegalRepresentation(current).equals(getLegalRepresentation(previous))
                && !Objects.equals(getOrganisationID(current), getOrganisationID(previous))) {

                errors.add("You cannot change organisation details for a legal representative");
            }
        }

        return errors;
    }

    private List<String> getStandardRespondentErrors(Respondent respondent, int i) {
        List<String> errors = new ArrayList<>();
        if (respondent.getLegalRepresentation() == null) {
            errors.add(String.format("Confirm if respondent %d has legal representation", i + 1));
        }
        if (isEmpty(respondent.getSolicitor().getFirstName()) || isEmpty(respondent.getSolicitor().getLastName())) {
            errors.add(String.format("Add the full name of respondent %d’s legal representative", i + 1));

        }
        if (isEmpty(respondent.getSolicitor().getEmail())) {
            errors.add(String.format("Add the email address of respondent %d’s legal representative", i + 1));
        }
        if (!respondent.hasRegisteredOrganisation() || !respondent.hasUnregisteredOrganisation()) {
            errors.add(String.format("Add the organisation details for respondent %d's representative", i + 1));
        }

        return errors;
    }

    private Map<UUID, Respondent> getIdRespondentMap(List<Element<Respondent>> elements) {
        return elements.stream()
            .collect(Collectors.toMap(Element::getId, Element::getValue, (val1, val2) -> val1, LinkedHashMap::new));
    }

    private Optional<String> getOrganisationID(Respondent value) {
        return ofNullable(value)
            .flatMap(respondent -> ofNullable(respondent.getSolicitor())
                .flatMap(solicitor -> ofNullable(solicitor.getOrganisation())
                    .map(Organisation::getOrganisationID))
            );
    }

    private Optional<String> getLegalRepresentation(Respondent value) {
        return ofNullable(value).flatMap(respondent -> ofNullable(respondent.getLegalRepresentation()));
    }

    private Set<UUID> getIds(List<Element<Respondent>> respondents1) {
        return respondents1.stream()
            .map(Element::getId)
            .collect(Collectors.toSet());
    }

}
