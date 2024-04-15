package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private JudicialUsersConfiguration judicialUsersConfiguration;

    @MockBean
    private LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    @MockBean
    private CaseAccessService caseAccessService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class ApplicantRoleRemoval {

        @Test
        void shouldPerformRoleMigrationWhenToggleHasOneUser() {
            when(featureToggleService.getUserIdsToRemoveRolesFrom()).thenReturn("abc-def");
            CaseData caseData = CaseData.builder().id(1L).build();
            postSubmittedEvent(asCaseDetails(caseData));

            verify(caseAccessService).revokeCaseRoleFromUser(1L,"abc-def", CaseRole.SOLICITORA);
            verifyNoMoreInteractions(caseAccessService);
        }

        @Test
        void shouldPerformRoleMigrationWhenToggleHasMultipleUsers() {
            when(featureToggleService.getUserIdsToRemoveRolesFrom()).thenReturn("123;456;789");
            CaseData caseData = CaseData.builder().id(1L).build();
            postSubmittedEvent(asCaseDetails(caseData));

            verify(caseAccessService).revokeCaseRoleFromUser(1L,"123", CaseRole.SOLICITORA);
            verify(caseAccessService).revokeCaseRoleFromUser(1L,"456", CaseRole.SOLICITORA);
            verify(caseAccessService).revokeCaseRoleFromUser(1L,"789", CaseRole.SOLICITORA);
            verifyNoMoreInteractions(caseAccessService);
        }

        @Test
        void shouldNotPerformRoleMigrationWhenToggledOff() {
            when(featureToggleService.getUserIdsToRemoveRolesFrom()).thenReturn("");
            CaseData caseData = CaseData.builder().id(1L).build();
            postSubmittedEvent(asCaseDetails(caseData));

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
