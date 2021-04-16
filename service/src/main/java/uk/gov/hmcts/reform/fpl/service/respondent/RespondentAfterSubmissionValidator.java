package uk.gov.hmcts.reform.fpl.service.respondent;

import com.google.common.collect.ImmutableList;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentAfterSubmissionValidator {

    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {

        ImmutableList.Builder<String> errors = ImmutableList.builder();

        Set<UUID> currentRespondentIds = getIds(caseData.getRespondents1());
        Set<UUID> previousRespondentIds = getIds(nullSafeList(caseDataBefore.getRespondents1()));

        if (!currentRespondentIds.containsAll(previousRespondentIds)) {
            errors.add("Removing an existing respondent is not allowed");
        }

        Map<UUID, Respondent> currentRespondents = getIdRespondentMap(caseData.getRespondents1());

        Map<UUID, Respondent> previousRespondents = getIdRespondentMap(nullSafeList(caseDataBefore.getRespondents1()));

        List<Map.Entry<UUID, Respondent>> currentRespondentsList = new ArrayList<>(currentRespondents.entrySet());

        for (int i = 0; i < currentRespondents.size(); i++) {
            Map.Entry<UUID, Respondent> map = currentRespondentsList.get(i);
            Respondent current = currentRespondentsList.get(i).getValue();
            Respondent previous = previousRespondents.getOrDefault(map.getKey(), current);

            if (getLegalRepresentation(current).equals(getLegalRepresentation(previous))
                && !Objects.equals(getOrganisationID(current), getOrganisationID(previous))) {

                errors.add(String.format("Change of organisation for respondent %d is not allowed", i + 1));
            }
        }

        return errors.build();
    }

    private Map<UUID, Respondent> getIdRespondentMap(List<Element<Respondent>> elements) {
        return elements
            .stream()
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
        return ofNullable(value)
            .flatMap(respondent -> ofNullable(respondent.getLegalRepresentation())
            );
    }

    private Set<UUID> getIds(List<Element<Respondent>> respondents1) {
        return respondents1
            .stream()
            .map(Element::getId)
            .collect(Collectors.toSet());
    }

}
