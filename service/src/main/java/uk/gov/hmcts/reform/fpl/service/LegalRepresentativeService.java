package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.Collections.emptySet;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalRepresentativeService {

    private final CaseService caseService;
    private final OrganisationService organisationService;
    private final LegalRepresentativesDifferenceCalculator differenceCalculator;

    public List<Element<LegalRepresentative>> getDefaultLegalRepresentatives(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData.getLegalRepresentatives())) {
            return List.of(element(LegalRepresentative.builder().build()));
        } else {
            return caseData.getLegalRepresentatives();
        }
    }

    public void updateRepresentatives(Long caseId,
                                      List<LegalRepresentative> originalRepresentatives,
                                      List<LegalRepresentative> updatedRepresentatives) {

        LegalRepresentativesChange legalRepresentativesChange = differenceCalculator.calculate(
            originalRepresentatives,
            updatedRepresentatives);

        legalRepresentativesChange.getAdded().forEach(userToBeAdded ->
            organisationService.findUserByEmail(userToBeAdded.getEmail())
                .ifPresentOrElse(
                    userId -> caseService.addUser(Long.toString(caseId),
                        userId,
                        userToBeAdded.getRole().getCaseRoles()),
                    throwException(userToBeAdded)
                )
        );

        legalRepresentativesChange.getRemoved().forEach(userToBeAdded ->
            organisationService.findUserByEmail(userToBeAdded.getEmail())
                .ifPresentOrElse(
                    userId -> caseService.addUser(Long.toString(caseId), userId, emptySet()),
                    throwException(userToBeAdded)
                )
        );
    }

    private Runnable throwException(LegalRepresentative userToBeAdded) {
        return () -> {
            throw new IllegalArgumentException(String.format("Could not find the user with email %s",
                userToBeAdded.getEmail()));
        };
    }
}
