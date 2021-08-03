package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildPolicyData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeChildAnswersData;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3262 {

        private final String migrationId = "FPLA-3132";

        @Test
        void shouldPerformMigration() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .state(State.SUBMITTED)
                .children1(wrapElements(
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build()

                ))
                .localAuthorities(wrapElements(LocalAuthority.builder().name("Some LA").build()))
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getNoticeOfChangeChildAnswersData()).isEqualTo(
                NoticeOfChangeChildAnswersData.builder()
                    .noticeOfChangeChildAnswers0(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers1(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers2(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers3(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers4(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers5(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers6(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers7(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers8(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers9(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers10(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers11(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers12(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers13(NoticeOfChangeAnswers.builder().build())
                    .noticeOfChangeChildAnswers14(NoticeOfChangeAnswers.builder().build())
                    .build()
            );

            assertThat(responseData.getChildPolicyData()).isEqualTo(
                ChildPolicyData.builder()
                    .childPolicy0(blankPolicyFor(SolicitorRole.CHILDSOLICITORA))
                    .childPolicy1(blankPolicyFor(SolicitorRole.CHILDSOLICITORB))
                    .childPolicy2(blankPolicyFor(SolicitorRole.CHILDSOLICITORC))
                    .childPolicy3(blankPolicyFor(SolicitorRole.CHILDSOLICITORD))
                    .childPolicy4(blankPolicyFor(SolicitorRole.CHILDSOLICITORE))
                    .childPolicy5(blankPolicyFor(SolicitorRole.CHILDSOLICITORF))
                    .childPolicy6(blankPolicyFor(SolicitorRole.CHILDSOLICITORG))
                    .childPolicy7(blankPolicyFor(SolicitorRole.CHILDSOLICITORH))
                    .childPolicy8(blankPolicyFor(SolicitorRole.CHILDSOLICITORI))
                    .childPolicy9(blankPolicyFor(SolicitorRole.CHILDSOLICITORJ))
                    .childPolicy10(blankPolicyFor(SolicitorRole.CHILDSOLICITORK))
                    .childPolicy11(blankPolicyFor(SolicitorRole.CHILDSOLICITORL))
                    .childPolicy12(blankPolicyFor(SolicitorRole.CHILDSOLICITORM))
                    .childPolicy13(blankPolicyFor(SolicitorRole.CHILDSOLICITORN))
                    .childPolicy14(blankPolicyFor(SolicitorRole.CHILDSOLICITORO))
                    .build()
            );
        }

        @Test
        void shouldNotPerformMigrationWhenTooManyChildren() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .state(State.SUBMITTED)
                .children1(wrapElements(
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build()
                ))
                .localAuthorities(wrapElements(LocalAuthority.builder().name("Some LA").build()))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(
                    "Migration {id = FPLA-3132, case reference = 12345} not migrating when number of children = 18 "
                    + "(max = 15)"
                );
        }

        @ParameterizedTest
        @EnumSource(value = State.class, names = {"OPEN", "RETURNED", "CLOSED"})
        void shouldNotPerformMigrationWhenInWrongState(State state) {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .state(state)
                .children1(wrapElements(
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build(),
                    Child.builder().build(), Child.builder().build(), Child.builder().build()
                ))
                .localAuthorities(wrapElements(LocalAuthority.builder().name("Some LA").build()))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = FPLA-3132, case reference = 12345} not migrating when state = " + state);
        }

        private OrganisationPolicy blankPolicyFor(SolicitorRole role) {
            return OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(role.getCaseRoleLabel())
                .organisation(Organisation.builder().build())
                .build();
        }
    }
}
