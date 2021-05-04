package uk.gov.hmcts.reform.fpl.service.respondent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UserService;

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
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentAfterSubmissionValidator {

    private final FeatureToggleService featureToggleService;
    private final UserService userService;

    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {

        List<String> errors = new ArrayList<>();

        Set<UUID> currentRespondentIds = getIds(caseData.getAllRespondents());
        Set<UUID> previousRespondentIds = getIds(caseDataBefore.getAllRespondents());

        if (!currentRespondentIds.containsAll(previousRespondentIds)) {
            errors.add("Removing an existing respondent is not allowed");
        }

        //if (!(featureToggleService.isNoticeOfChangeEnabled() && userService.isHmctsAdminUser())) {

        if (!(featureToggleService.isNoticeOfChangeEnabled() && userService.isHmctsAdminUser())) {
            Map<UUID, Respondent> currentRespondents = getIdRespondentMap(caseData.getAllRespondents());

            Map<UUID, Respondent> previousRespondents = getIdRespondentMap(caseDataBefore.getAllRespondents());

            List<Map.Entry<UUID, Respondent>> currentRespondentsList = new ArrayList<>(currentRespondents.entrySet());

            for (int i = 0; i < currentRespondents.size(); i++) {
                Map.Entry<UUID, Respondent> map = currentRespondentsList.get(i);
                Respondent current = currentRespondentsList.get(i).getValue();
                Respondent previous = previousRespondents.getOrDefault(map.getKey(), current);

                if (YES.getValue().equals(previous.getLegalRepresentation())
                    && NO.getValue().equals(current.getLegalRepresentation())) {
                    errors.add(String.format("You cannot remove respondent %d's legal representative", i + 1));
                }

                if (getLegalRepresentation(current).equals(getLegalRepresentation(previous))
                    && !Objects.equals(getOrganisationID(current), getOrganisationID(previous))) {

                    errors.add(String.format("Change of organisation for respondent %d is not allowed", i + 1));
                }
            }
        }

        return errors;
    }

    private Map<UUID, Respondent> getIdRespondentMap(List<Element<Respondent>> elements) {
        return nullSafeList(elements).stream()
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

    private Set<UUID> getIds(List<Element<Respondent>> respondents) {
        return nullSafeList(respondents).stream()
            .map(Element::getId)
            .collect(Collectors.toSet());
    }

}
