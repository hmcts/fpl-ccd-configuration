package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Guardian;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

public class CafcassApiGuardianServiceTest {
    private static final Guardian EXISTING_GUARDIAN_1 = Guardian.builder()
        .guardianName("Guardian 1")
        .email("guardian1@test.com")
        .telephoneNumber("0123456789")
        .childrenRepresenting(List.of("Child 1", "Child 2"))
        .build();
    private static final Guardian EXISTING_GUARDIAN_2 = Guardian.builder()
        .guardianName("Guardian 2")
        .email("guardian2@test.com")
        .telephoneNumber("0123456789")
        .childrenRepresenting(List.of("Child 3"))
        .build();
    private static final List<Element<Guardian>> EXISTING_GUARDIANS =
        wrapElementsWithUUIDs(EXISTING_GUARDIAN_1, EXISTING_GUARDIAN_2);
    private static final CaseData CASE_DATA = CaseData.builder().guardians(EXISTING_GUARDIANS).build();

    private final CaseConverter caseConverter = mock(CaseConverter.class);
    private final CoreCaseDataService coreCaseDataService = mock(CoreCaseDataService.class);
    private CafcassApiGuardianService underTest = new CafcassApiGuardianService(caseConverter, coreCaseDataService);

    @Test
    void shouldReturnFalseIfGuardianListsAreIdentical() {
        List<Guardian> updatedGuardians = List.of(EXISTING_GUARDIAN_1.toBuilder().build(),
            EXISTING_GUARDIAN_2.toBuilder().build());

        assertFalse(underTest.checkIfAnyGuardianUpdated(CASE_DATA, updatedGuardians));
        assertFalse(underTest.checkIfAnyGuardianUpdated(CaseData.builder().guardians(null).build(), List.of()));
        assertFalse(underTest.checkIfAnyGuardianUpdated(CaseData.builder().guardians(List.of()).build(), List.of()));
        assertFalse(underTest.checkIfAnyGuardianUpdated(CaseData.builder().guardians(null).build(), null));
        assertFalse(underTest.checkIfAnyGuardianUpdated(CaseData.builder().guardians(List.of()).build(), null));
    }

    @Test
    void shouldReturnTrueIfOnlyEmailUpdated() {
        List<Guardian> updatedGuardians = List.of(
            EXISTING_GUARDIAN_1.toBuilder().email("guardian_newMail@test.com").build(),
            EXISTING_GUARDIAN_2);
        assertTrue(underTest.checkIfAnyGuardianUpdated(CASE_DATA,
            updatedGuardians));
    }

    @Test
    void shouldReturnTrueIfOnlyTelephoneUpdated() {
        List<Guardian> updatedGuardians = List.of(
            EXISTING_GUARDIAN_1.toBuilder().telephoneNumber("000").build(),
            EXISTING_GUARDIAN_2);
        assertTrue(underTest.checkIfAnyGuardianUpdated(CASE_DATA,
            updatedGuardians));
    }

    @Test
    void shouldReturnTrueIfOnlyChildrenRepresentingUpdated() {
        List<Guardian> updatedGuardians = List.of(
            EXISTING_GUARDIAN_1.toBuilder().childrenRepresenting(List.of("Child 123")).build(),
            EXISTING_GUARDIAN_2);
        assertTrue(underTest.checkIfAnyGuardianUpdated(CASE_DATA,
            updatedGuardians));
    }

    @Test
    void shouldReturnTrueIfNewGuardianAdded() {
        Guardian newGuardian = Guardian.builder().guardianName("New Guardian").build();

        assertTrue(underTest.checkIfAnyGuardianUpdated(CaseData.builder().guardians(null).build(),
            List.of(newGuardian)));
        assertTrue(underTest.checkIfAnyGuardianUpdated(CaseData.builder().guardians(List.of()).build(),
            List.of(newGuardian)));
        assertTrue(underTest.checkIfAnyGuardianUpdated(CASE_DATA,
            List.of(EXISTING_GUARDIAN_1, EXISTING_GUARDIAN_2, newGuardian)));
    }

    @Test
    void shouldReturnTrueIfGuardianRemoved() {
        assertTrue(underTest.checkIfAnyGuardianUpdated(CASE_DATA, List.of(EXISTING_GUARDIAN_1)));
        assertTrue(underTest.checkIfAnyGuardianUpdated(CASE_DATA, List.of()));
        assertTrue(underTest.checkIfAnyGuardianUpdated(CASE_DATA, null));
    }
}
