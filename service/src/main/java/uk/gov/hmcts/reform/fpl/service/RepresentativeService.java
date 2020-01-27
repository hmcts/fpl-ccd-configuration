package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativeService {

    private final CaseService caseService;
    private final OrganisationService organisationService;
    private final RepresentativeCaseRoleService representativeCaseRoleService;

    public List<Element<Representative>> getDefaultRepresentatives(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData.getRepresentatives())) {
            return wrapElements(Representative.builder().build());
        } else {
            return caseData.getRepresentatives();
        }
    }

    public List<String> validateRepresentatives(CaseData caseData) {
        List<String> validationErrors = new ArrayList<>();

        List<Representative> representatives = unwrapElements(caseData.getRepresentatives());

        int representativeSequence = 1;

        for (Representative representative : representatives) {

            final RepresentativeServingPreferences servingPreferences = representative.getServingPreferences();
            final RepresentativeRole role = representative.getRole();

            String representativeLabel = representatives.size() == 1
                ? "Representative" : "Representative " + representativeSequence++;

            performBasicValidation(validationErrors, representative, servingPreferences, role, representativeLabel);

            if (POST.equals(servingPreferences)) {
                validateRepresentativeAddress(validationErrors, representative, representativeLabel);
            }

            if (nonNull(role) && RESPONDENT.equals(representative.getRole().getType())) {
                validateRepresentativeRespondentRole(caseData, validationErrors, representative, role,
                    representativeLabel);
            }

            if (nonNull(role) && OTHER.equals(role.getType())) {
                validateRepresentativeOtherRole(caseData, validationErrors, representative, representativeLabel);
            }

            if (DIGITAL_SERVICE.equals(servingPreferences)) {
                validateDigitalServicePreference(validationErrors, representative, representativeLabel);
            }
        }

        return validationErrors;
    }

    private void performBasicValidation(List<String> validationErrors, Representative representative,
                                        RepresentativeServingPreferences servingPreferences, RepresentativeRole role,
                                        String representativeLabel) {
        if (isEmpty(representative.getFullName())) {
            validationErrors.add(format("Enter a full name for %s", representativeLabel));
        }

        if (isEmpty(representative.getPositionInACase())) {
            validationErrors.add(format("Enter a position in the case for %s", representativeLabel));
        }

        if (isNull(role)) {
            validationErrors.add(format("Select who %s is", representativeLabel));
        }

        if (isNull(servingPreferences)) {
            validationErrors.add(format("Select how %s wants to get case information", representativeLabel));
        }

        if (EMAIL.equals(servingPreferences) && isEmpty(representative.getEmail())) {
            validationErrors.add(format("Enter an email address for %s", representativeLabel));
        }
    }

    private void validateRepresentativeRespondentRole(CaseData caseData, List<String> validationErrors,
                                                      Representative representative, RepresentativeRole role,
                                                      String representativeLabel) {
        if (caseData.findRespondent(role.getSequenceNo()).isEmpty()) {
            validationErrors.add(format("Respondent %s represented by %s doesn't exist."
                    + " Choose a respondent who is associated with this case",
                representative.getRole().getSequenceNo() + 1, representativeLabel));
        }
    }

    private void validateRepresentativeOtherRole(CaseData caseData, List<String> validationErrors,
                                                 Representative representative,
                                                 String representativeLabel) {
        int otherPersonSeq = representative.getRole().getSequenceNo();
        if (caseData.findOther(otherPersonSeq).isEmpty()) {
            String otherPersonLabel = otherPersonSeq == 0 ? "Person" : "Other person " + otherPersonSeq;
            validationErrors.add(format("%s represented by %s doesn't exist."
                    + " Choose a person who is associated with this case",
                otherPersonLabel, representativeLabel));
        }
    }

    private void validateDigitalServicePreference(List<String> validationErrors,
                                                  Representative representative, String representativeLabel) {
        if (isEmpty(representative.getEmail())) {
            validationErrors.add(format("Enter an email address for %s", representativeLabel));
        } else {
            Optional<String> userId = organisationService.findUserByEmail(representative.getEmail());

            if (userId.isEmpty()) {
                validationErrors.add(
                    format("%s must already have an account with the digital service", representativeLabel));
            }
        }
    }

    private void validateRepresentativeAddress(List<String> validationErrors, Representative representative,
                                               String representativeLabel) {
        if (isNull(representative.getAddress()) || isEmpty(representative.getAddress().getPostcode())) {
            validationErrors.add(format("Enter a postcode for %s", representativeLabel));
        }
        if (isNull(representative.getAddress()) || isEmpty(representative.getAddress().getAddressLine1())) {
            validationErrors.add(format("Enter a valid address for %s", representativeLabel));
        }
    }

    public void updateRepresentatives(Long caseId, CaseData updatedCaseData, CaseData originalCaseData) {
        associatedRepresentativesWithParties(updatedCaseData);
        updateRepresentativesCaseRoles(updatedCaseData, originalCaseData, caseId);
    }

    private void updateRepresentativesCaseRoles(CaseData newCase, CaseData oldCase, Long caseId) {
        Map<String, Set<CaseRole>> caseRolesToBeUpdated = representativeCaseRoleService.calculateCaseRoleUpdates(
            unwrapElements(newCase.getRepresentatives()),
            unwrapElements(oldCase.getRepresentatives()));

        caseRolesToBeUpdated.forEach((email, roles) ->
            organisationService.findUserByEmail(email)
                .ifPresent(userId -> caseService.addUser(Long.toString(caseId), userId, roles)));
    }

    public List<Representative> getRepresentativesByServedPreference(List<Element<Representative>> representatives,
                                                                     RepresentativeServingPreferences preference) {
        if (isNotEmpty(representatives)) {
            return representatives.stream()
                .filter(Objects::nonNull)
                .map(Element::getValue)
                .filter(representative -> preference == representative.getServingPreferences())
                .collect(toList());
        }
        return emptyList();
    }

    private void associatedRepresentativesWithParties(CaseData caseData) {
        caseData.getRespondents1().stream()
            .map(Element::getValue)
            .map(Representable::getRepresentedBy)
            .forEach(List::clear);
        caseData.getAllOthers().forEach(o -> o.getRepresentedBy().clear());

        caseData.getRepresentatives()
            .forEach(representative -> associatedRepresentativeWithParty(caseData, representative));
    }

    private void associatedRepresentativeWithParty(CaseData caseData, Element<Representative> representative) {
        findRepresentable(caseData, representative.getValue())
            .ifPresent(representable -> representable.addRepresentative(representative.getId()));
    }

    private Optional<? extends Representable> findRepresentable(CaseData caseData, Representative representative) {
        switch (representative.getRole().getType()) {
            case RESPONDENT:
                return caseData.findRespondent(representative.getRole().getSequenceNo());
            case OTHER:
                return caseData.findOther(representative.getRole().getSequenceNo());
            default:
                return Optional.empty();
        }
    }
}
