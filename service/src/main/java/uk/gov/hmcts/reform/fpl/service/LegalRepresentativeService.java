package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;

import java.util.List;

import static java.util.Collections.emptySet;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalRepresentativeService {

    private final CaseService caseService;
    private final OrganisationService organisationService;
    private final LegalRepresentativesDifferenceCalculator legalRepresentativesDifferenceCalculator;

    public void updateRepresentatives(Long caseId,
                                      List<LegalRepresentative> original,
                                      List<LegalRepresentative> updated) {

        LegalRepresentativesChange legalRepresentativesChange = legalRepresentativesDifferenceCalculator.calculate(
            original,
            updated);

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
