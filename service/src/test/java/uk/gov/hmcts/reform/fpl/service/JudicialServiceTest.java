package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.rd.client.JudicialApi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class JudicialServiceTest {

    private static final Long CASE_ID = 12345L;
    private static final String USER_TOKEN = "USER";
    private static final String SERVICE_TOKEN = "SERVICE";

    private static final String EMAIL = "test@test.com";


    @Mock
    private SystemUserService systemUserService;
    @Mock
    private JudicialApi judicialApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private ValidateEmailService validateEmailService;

    @Mock
    private LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    @Mock
    private JudicialUsersConfiguration judicialUsersConfiguration;

    @Captor
    private ArgumentCaptor<List<RoleAssignment>> rolesCaptor;

    @InjectMocks
    private JudicialService underTest;

    @Nested
    class Migrations {
        private static final String JUDGE_1_ID = "judge 1 id";
        private static final String JUDGE_2_ID = "judge 2 id";

        private static final JudgeAndLegalAdvisor JUDGE_1 = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge1@test.com")
            .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
            .build();

        private static final JudgeAndLegalAdvisor JUDGE_2 = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge2@test.com")
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .build();

        private static final HearingBooking HEARING_1 = HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(5))
            .judgeAndLegalAdvisor(JUDGE_1)
            .build();

        private static final HearingBooking HEARING_2 = HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(10))
            .judgeAndLegalAdvisor(JUDGE_2)
            .build();

        @BeforeEach
        void beforeEach() {
            when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
            when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

            when(judicialUsersConfiguration.getJudgeUUID(JUDGE_1.getJudgeEmailAddress()))
                .thenReturn(Optional.empty());
            when(judicialUsersConfiguration.getJudgeUUID(JUDGE_2.getJudgeEmailAddress()))
                .thenReturn(Optional.of(JUDGE_2_ID));

            when(legalAdviserUsersConfiguration.getLegalAdviserUUID(JUDGE_1.getJudgeEmailAddress()))
                .thenReturn(Optional.of(JUDGE_1_ID));
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID(JUDGE_2.getJudgeEmailAddress()))
                .thenReturn(Optional.empty());

        }

        @Test
        void shouldGenerateRoleAssignmentsBasedOnHearingDates() {
            List<HearingBooking> hearings = List.of(HEARING_1, HEARING_2);
            underTest.migrateHearingJudges(hearings, CASE_ID);

            verify(roleAssignmentService).createRoleAssignments(rolesCaptor.capture());

            assertThat(rolesCaptor.getValue().stream()
                .map(RoleAssignment::getActorId)
                .collect(Collectors.toList()))
                .containsExactly(JUDGE_1_ID, JUDGE_2_ID);
        }

    }


    @Test
    void shouldReturnLegalAdviserEmailIfInMapping() {
        when(judicialUsersConfiguration.getJudgeUUID(EMAIL)).thenReturn(Optional.empty());
        when(legalAdviserUsersConfiguration.getLegalAdviserUUID(EMAIL)).thenReturn(Optional.of("UUID"));

        Optional<String> uuid = underTest.getJudgeUserIdFromEmail(EMAIL);

        assertThat(uuid.isPresent()).isTrue();
        assertThat(uuid.get()).isEqualTo("UUID");
    }

    @Test
    void shouldReturnJudgeEmailIfInMapping() {
        when(judicialUsersConfiguration.getJudgeUUID(EMAIL)).thenReturn(Optional.of("UUID"));
        when(legalAdviserUsersConfiguration.getLegalAdviserUUID(EMAIL)).thenReturn(Optional.empty());

        Optional<String> uuid = underTest.getJudgeUserIdFromEmail(EMAIL);

        assertThat(uuid.isPresent()).isTrue();
        assertThat(uuid.get()).isEqualTo("UUID");
    }

    @Test
    void shouldReturnEmptyIfInNeitherMapping() {
        when(judicialUsersConfiguration.getJudgeUUID(EMAIL)).thenReturn(Optional.empty());
        when(legalAdviserUsersConfiguration.getLegalAdviserUUID(EMAIL)).thenReturn(Optional.empty());

        Optional<String> uuid = underTest.getJudgeUserIdFromEmail(EMAIL);

        assertThat(uuid.isPresent()).isFalse();
    }

}
