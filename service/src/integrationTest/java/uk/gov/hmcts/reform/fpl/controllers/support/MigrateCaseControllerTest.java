package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @MockBean
    private CaseAccessService caseAccessService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private OrganisationService organisationService;

    @Nested
    class Dfpl2284 {

        final CaseData caseData = CaseData.builder()
            .id(1L)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole("[SOLICITORA]")
                .build())
            .build();

        @BeforeEach
        void beforeEach() {
            when(organisationService.findOrganisation(any())).thenReturn(Optional.of(Organisation.builder()
                    .name("Test organisation")
                    .organisationIdentifier("TEST")
                .build()));
        }

        @Test
        void shouldPerformRoleMigrationWhenToggleHasOneUser() {
            when(featureToggleService.getUserIdsToRemoveRolesFrom()).thenReturn("abc-def");
            postAboutToSubmitEvent(buildCaseDetails(caseData, "DFPL-2284"));

            verify(caseAccessService).revokeCaseRoleFromUser(1L,"abc-def", CaseRole.SOLICITORA);
            verifyNoMoreInteractions(caseAccessService);
        }

        @Test
        void shouldPerformRoleMigrationWhenToggleHasMultipleUsers() {
            when(featureToggleService.getUserIdsToRemoveRolesFrom()).thenReturn("123;456;789");
            postAboutToSubmitEvent(buildCaseDetails(caseData, "DFPL-2284"));

            verify(caseAccessService).revokeCaseRoleFromUser(1L,"123", CaseRole.SOLICITORA);
            verify(caseAccessService).revokeCaseRoleFromUser(1L,"456", CaseRole.SOLICITORA);
            verify(caseAccessService).revokeCaseRoleFromUser(1L,"789", CaseRole.SOLICITORA);
            verifyNoMoreInteractions(caseAccessService);
        }

        @Test
        void shouldNotPerformRoleMigrationWhenToggledOff() {
            when(featureToggleService.getUserIdsToRemoveRolesFrom()).thenReturn("");
            postAboutToSubmitEvent(buildCaseDetails(caseData, "DFPL-2284"));

            verifyNoInteractions(caseAccessService);
        }
    }

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
    }

}
