package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    @MockBean
    private JudicialService judicialService;

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

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

    @ParameterizedTest
    @EnumSource(value = JudgeOrMagistrateTitle.class, names = {"LEGAL_ADVISOR"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldAttemptRoleAssignmentForAllocatedJudge(JudgeOrMagistrateTitle title) {
        when(judicialService.getHearingJudgeRolesForMigration(any())).thenReturn(List.of());

        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(title)
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("allocated-idam-id")
                    .build())
                .build())
            .build();

        postAboutToSubmitEvent(buildCaseDetails(caseData, "DFPL-2585Rollback"));

        verify(judicialService).migrateJudgeRoles(List.of(RoleAssignmentUtils.buildRoleAssignment(
            1L,
            "allocated-idam-id",
            ALLOCATED_JUDGE.getRoleName(),
            RoleCategory.JUDICIAL,
            any(),
            null // no end date
        )));
    }

    @Test
    void shouldAttemptRoleAssignmentForAllocatedLegalAdvisor() {
        when(judicialService.getHearingJudgeRolesForMigration(any())).thenReturn(List.of());

        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("allocated-idam-id")
                    .build())
                .build())
            .build();

        postAboutToSubmitEvent(buildCaseDetails(caseData, "DFPL-2585Rollback"));

        verify(judicialService).migrateJudgeRoles(List.of(RoleAssignmentUtils.buildRoleAssignment(
            1L,
            "allocated-idam-id",
            ALLOCATED_LEGAL_ADVISER.getRoleName(),
            RoleCategory.LEGAL_OPERATIONS,
            any(),
            null // no end date
        )));
    }


}
