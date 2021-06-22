package uk.gov.hmcts.reform.fpl.service.email;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_OTHER_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_PERSON_1;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativesInbox {

    private static final List<String> OTHER_CASE_ROLES = List.of("REPRESENTING_PERSON_1", "REPRESENTING_OTHER_PERSON_1");

    public Set<String> getEmailsByPreference(CaseData caseData, RepresentativeServingPreferences preference) {
        if (preference.equals(RepresentativeServingPreferences.POST)) {
            throw new IllegalArgumentException("Preference should not be POST");
        }

        LinkedHashSet<String> emails = caseData.getRepresentativesByServedPreference(preference)
            .stream()
            .map(Representative::getEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        emails.addAll(getRespondentSolicitorEmails(caseData.getRespondents1(), RepresentativeServingPreferences.DIGITAL_SERVICE));

        return emails;
    }

    public Set<String> getEmailsByPreferenceExcludingOthers(CaseData caseData, RepresentativeServingPreferences preference) {
        if (preference.equals(RepresentativeServingPreferences.POST)) {
            throw new IllegalArgumentException("Preference should not be POST");
        }

        LinkedHashSet<String> emails = caseData.getRepresentativesByServedPreference(preference)
            .stream()
            .filter(representative ->
                !representative.getRole().getType().equals(RepresentativeRole.Type.OTHER))
            .map(Representative::getEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        emails.addAll(getRespondentSolicitorEmails(caseData.getRespondents1(), RepresentativeServingPreferences.DIGITAL_SERVICE));

        return emails;
    }

    private LinkedHashSet<String> getRespondentSolicitorEmails(List<Element<Respondent>> respondents, RepresentativeServingPreferences preference) {
        return nullSafeList(respondents).stream()
            .filter(respondent ->
                (preference == RepresentativeServingPreferences.DIGITAL_SERVICE)
                    == respondent.getValue().hasRegisteredOrganisation())
            .map(respondent -> Optional.ofNullable(respondent.getValue().getSolicitor())
                .map(RespondentSolicitor::getEmail).orElse(null))
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
