package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class LegalRepresentativeServiceTest {

    @Mock
    private List<LegalRepresentative> originalRepresentatives;
    @Mock
    private List<LegalRepresentative> updatedRepresentatives;
    private static final Long CASE_ID = 232323L;
    private static final String REPRESENTATIVE_EMAIL_1 = "email1";
    private static final String USER_ID_1 = "userId1";
    private static final LegalRepresentativeRole REPRESENTATIVE_ROLE = LegalRepresentativeRole.EXTERNAL_LA_BARRISTER;

    @Mock
    private LegalRepresentativesDifferenceCalculator legalRepresentativesDifferenceCalculator;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private CaseService caseService;

    @InjectMocks
    private LegalRepresentativeService underTest;

    @Test
    void testDefaultLegalRepresentativesIfEmpty() {
        List<Element<LegalRepresentative>> actual = underTest.getDefaultLegalRepresentatives(
            CaseData.builder().build()
        );

        assertThat(actual).isEqualTo(wrapElements(LegalRepresentative.builder().build()));
    }

    @Test
    void testDefaultLegalRepresentativesIfExisting() {
        List<Element<LegalRepresentative>> existingLegalRepresentatives = wrapElements(mock(LegalRepresentative.class));

        List<Element<LegalRepresentative>> actual = underTest.getDefaultLegalRepresentatives(
            CaseData.builder().legalRepresentatives(existingLegalRepresentatives).build()
        );

        assertThat(actual).isEqualTo(existingLegalRepresentatives);
    }

    @Test
    void doNotUpdateIfNoChange() {
        when(legalRepresentativesDifferenceCalculator.calculate(
            originalRepresentatives,
            updatedRepresentatives)
        ).thenReturn(
            LegalRepresentativesChange.builder()
                .added(emptySet())
                .removed(emptySet())
                .build()
        );

        underTest.updateRepresentatives(CASE_ID, originalRepresentatives, updatedRepresentatives);

        verifyNoInteractions(organisationService, caseService);
    }

    @Test
    void updateAddedRepresentative() {
        when(organisationService.findUserByEmail(REPRESENTATIVE_EMAIL_1)).thenReturn(Optional.of(USER_ID_1));

        when(legalRepresentativesDifferenceCalculator.calculate(
            originalRepresentatives,
            updatedRepresentatives)
        ).thenReturn(
            LegalRepresentativesChange.builder()
                .added(Set.of(LegalRepresentative.builder()
                    .email(REPRESENTATIVE_EMAIL_1)
                    .role(REPRESENTATIVE_ROLE)
                    .build()))
                .removed(emptySet())
                .build()
        );

        underTest.updateRepresentatives(CASE_ID, originalRepresentatives, updatedRepresentatives);

        verify(caseService).addUser(CASE_ID.toString(), USER_ID_1, REPRESENTATIVE_ROLE.getCaseRoles());
    }

    @Test
    void notFoundAddedRepresentative() {
        when(organisationService.findUserByEmail(REPRESENTATIVE_EMAIL_1)).thenReturn(Optional.empty());

        when(legalRepresentativesDifferenceCalculator.calculate(
            originalRepresentatives,
            updatedRepresentatives)
        ).thenReturn(
            LegalRepresentativesChange.builder()
                .added(Set.of(LegalRepresentative.builder()
                    .email(REPRESENTATIVE_EMAIL_1)
                    .role(REPRESENTATIVE_ROLE)
                    .build()))
                .removed(emptySet())
                .build()
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> underTest.updateRepresentatives(CASE_ID, originalRepresentatives, updatedRepresentatives)
        );

        assertThat(exception.getMessage()).isEqualTo(String.format("Could not find the user with email %s",
            REPRESENTATIVE_EMAIL_1));
        verifyNoInteractions(caseService);
    }

    @Test
    void updateRemovedRepresentative() {
        when(organisationService.findUserByEmail(REPRESENTATIVE_EMAIL_1)).thenReturn(Optional.of(USER_ID_1));

        when(legalRepresentativesDifferenceCalculator.calculate(
            originalRepresentatives,
            updatedRepresentatives)
        ).thenReturn(
            LegalRepresentativesChange.builder()
                .added(emptySet())
                .removed(Set.of(LegalRepresentative.builder()
                    .email(REPRESENTATIVE_EMAIL_1)
                    .role(REPRESENTATIVE_ROLE)
                    .build()))
                .build()
        );

        underTest.updateRepresentatives(CASE_ID, originalRepresentatives, updatedRepresentatives);

        verify(caseService).addUser(CASE_ID.toString(), USER_ID_1, emptySet());
    }

    @Test
    void notFoundRemovedRepresentative() {
        when(organisationService.findUserByEmail(REPRESENTATIVE_EMAIL_1)).thenReturn(Optional.empty());

        when(legalRepresentativesDifferenceCalculator.calculate(
            originalRepresentatives,
            updatedRepresentatives)
        ).thenReturn(
            LegalRepresentativesChange.builder()
                .added(emptySet())
                .removed(Set.of(LegalRepresentative.builder()
                    .email(REPRESENTATIVE_EMAIL_1)
                    .role(REPRESENTATIVE_ROLE)
                    .build()))
                .build()
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> underTest.updateRepresentatives(CASE_ID, originalRepresentatives, updatedRepresentatives)
        );

        assertThat(exception.getMessage()).isEqualTo(String.format("Could not find the user with email %s",
            REPRESENTATIVE_EMAIL_1));
        verifyNoInteractions(caseService);
    }
}
