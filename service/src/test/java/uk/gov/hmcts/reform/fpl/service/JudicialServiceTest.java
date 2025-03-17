package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class JudicialServiceTest {

    private static final Long CASE_ID = 12345L;
    private static final String USER_TOKEN = "USER";
    private static final String SERVICE_TOKEN = "SERVICE";

    private static final String EMAIL = "test@test.com";

    private static final ZoneId ZONE = ZoneId.of("Europe/London");

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

    @Mock
    private ElinksService elinksService;

    @Captor
    private ArgumentCaptor<List<RoleAssignment>> rolesCaptor;

    @Captor
    private ArgumentCaptor<RoleAssignment> roleAssignmentCaptor;

    @InjectMocks
    private JudicialService underTest;

    @Nested
    class Migrations {
        private static final String JUDGE_1_ID = "judge 1 id";
        private static final String JUDGE_2_ID = "judge 2 id";
        private static final String JUDGE_3_ID = "judge 3 id";

        private static final JudgeAndLegalAdvisor JUDGE_1 = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge1@test.com")
            .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(JUDGE_1_ID)
                .build())
            .build();

        private static final JudgeAndLegalAdvisor JUDGE_2 = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge2@test.com")
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(JUDGE_2_ID)
                .build())
            .build();

        private static final JudgeAndLegalAdvisor JUDGE_3 = JudgeAndLegalAdvisor.builder()
            .judgeEmailAddress("judge3@test.com")
            .judgeTitle(JudgeOrMagistrateTitle.OTHER)
            .otherTitle("JLA")
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(JUDGE_3_ID)
                .build())
            .build();

        private static final HearingBooking HEARING_1 = HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(5))
            .judgeAndLegalAdvisor(JUDGE_1)
            .build();

        private static final HearingBooking HEARING_2 = HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(10))
            .judgeAndLegalAdvisor(JUDGE_2)
            .build();

        private static final HearingBooking HEARING_3 = HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(15))
            .judgeAndLegalAdvisor(JUDGE_3)
            .build();

        @BeforeEach
        void beforeEach() {
            when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
            when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

            when(judicialUsersConfiguration.getJudgeUUID(JUDGE_1.getJudgeEmailAddress()))
                .thenReturn(Optional.empty());
            when(judicialUsersConfiguration.getJudgeUUID(JUDGE_2.getJudgeEmailAddress()))
                .thenReturn(Optional.of(JUDGE_2_ID));
            when(judicialUsersConfiguration.getJudgeUUID(JUDGE_3.getJudgeEmailAddress()))
                .thenReturn(Optional.empty());

            when(legalAdviserUsersConfiguration.getLegalAdviserUUID(JUDGE_1.getJudgeEmailAddress()))
                .thenReturn(Optional.of(JUDGE_1_ID));
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID(JUDGE_2.getJudgeEmailAddress()))
                .thenReturn(Optional.empty());
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID(JUDGE_3.getJudgeEmailAddress()))
                .thenReturn(Optional.of(JUDGE_3_ID));

            when(elinksService.getElinksAcceptHeader()).thenReturn("application/json");
        }

        @Test
        void shouldGenerateRoleAssignmentsBasedOnHearingDates() {
            CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .hearingDetails(wrapElements(HEARING_1, HEARING_2, HEARING_3))
                .build();

            List<RoleAssignment> roles = underTest.getHearingJudgeRolesForMigration(caseData);

            verifyHearingRoleAssignments(roles);
        }

        void verifyHearingRoleAssignments(List<RoleAssignment> roles) {
            assertThat(roles).hasSize(3);
            assertThat(roles.get(0)).extracting("roleName", "roleCategory", "beginTime", "endTime")
                .containsExactly("hearing-legal-adviser", RoleCategory.LEGAL_OPERATIONS,
                    HEARING_1.getStartDate().atZone(ZONE),
                    HEARING_2.getStartDate().atZone(ZONE));
            assertThat(roles.get(1)).extracting("roleName", "roleCategory", "beginTime", "endTime")
                .containsExactly("hearing-judge", RoleCategory.JUDICIAL,
                    HEARING_2.getStartDate().atZone(ZONE),
                    HEARING_3.getStartDate().atZone(ZONE));
            assertThat(roles.get(2)).extracting("roleName", "roleCategory", "beginTime", "endTime")
                .containsExactly("hearing-legal-adviser", RoleCategory.LEGAL_OPERATIONS,
                    HEARING_3.getStartDate().atZone(ZONE),
                    null);
        }

    }

    @Nested
    class Mapping {

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

    @Nested
    class ExistingRoles {

        @Test
        void shouldRemoveExistingAllocatedJudges() {
            List<RoleAssignment> existing = Stream.of("12345", "67890")
                .map(id -> RoleAssignment.builder()
                    .actorId(id)
                    .id(id)
                    .roleName("allocated-judge")
                    .roleCategory(RoleCategory.JUDICIAL)
                    .build())
                .toList();

            when(roleAssignmentService.getCaseRolesAtTime(any(), any(), any()))
                .thenReturn(existing);

            underTest.removeExistingAllocatedJudgesAndLegalAdvisers(12345L);

            verify(roleAssignmentService).getCaseRolesAtTime(any(), any(), any());
            verify(roleAssignmentService, times(2)).deleteRoleAssignment(roleAssignmentCaptor.capture());

            assertThat(roleAssignmentCaptor.getAllValues())
                .extracting("id")
                .containsExactlyInAnyOrder("12345", "67890");

            verifyNoMoreInteractions(roleAssignmentService);
        }

        @Test
        void shouldRemoveAndRecreateExistingHearingJudges() {
            List<RoleAssignment> existing = Stream.of("12345", "67890")
                .map(id -> RoleAssignment.builder()
                    .actorId(id)
                    .id(id)
                    .roleName("hearing-judge")
                    .roleCategory(RoleCategory.JUDICIAL)
                    .build())
                .toList();

            when(roleAssignmentService.getCaseRolesAtTime(any(), any(), any()))
                .thenReturn(existing);

            underTest.setExistingHearingJudgesAndLegalAdvisersToExpire(12345L,
                ZonedDateTime.now(ZONE));

            verify(roleAssignmentService).getCaseRolesAtTime(any(), any(), any());
            verify(roleAssignmentService, times(2)).deleteRoleAssignment(roleAssignmentCaptor.capture());
            verify(roleAssignmentService).createRoleAssignments(rolesCaptor.capture());

            assertThat(roleAssignmentCaptor.getAllValues())
                .extracting("id")
                .containsExactlyInAnyOrder("12345", "67890");

            assertThat(rolesCaptor.getValue()).hasSize(2);
            assertThat(rolesCaptor.getValue()).extracting("actorId")
                .containsExactlyInAnyOrder("12345", "67890");

            verifyNoMoreInteractions(roleAssignmentService);
        }
    }

    @Test
    void shouldCheckJudgeExistsWhenPresentInJrd() {
        when(judicialApi.findUsers(any(), any(), anyInt(), any(), any()))
            .thenReturn(List.of(JudicialUserProfile.builder().build()));
        boolean exists = underTest.checkJudgeExists("1234");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldCheckJudgeDoesntExistWhenNotPresentInJrd() {
        when(judicialApi.findUsers(any(), any(), anyInt(), any(), any()))
            .thenReturn(List.of());
        boolean exists = underTest.checkJudgeExists("1234");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldGetAllocatedJudgeDetailsOffCase() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("test@test.com")
                .build())
            .build();

        Optional<Judge> judge = underTest.getAllocatedJudge(caseData);

        assertThat(judge.isPresent()).isTrue();
    }

    @Test
    void shouldNotGetAllocatedJudgeDetailsOffCaseIfMissingEmail() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .build())
            .build();

        Optional<Judge> judge = underTest.getAllocatedJudge(caseData);

        assertThat(judge.isPresent()).isFalse();
    }

    @Test
    void shouldGetHearingJudgesOnCase() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeEmailAddress("test@test.com")
                    .build())
                .build()))
            .build();

        Set<JudgeAndLegalAdvisor> judges = underTest.getHearingJudges(caseData);

        assertThat(judges).hasSize(1);
    }

    @Test
    void shouldDeleteSpecificHearingRole() {
        HearingBooking hearing = HearingBooking.builder()
            .startDate(LocalDateTime.now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEnterManually(YesNo.NO)
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idam")
                    .build())
                .build())
            .build();

        underTest.deleteSpecificHearingRole(12345L, hearing);

        verify(roleAssignmentService).deleteRoleAssignmentOnCaseAtTime(eq(12345L), any(), eq("idam"),
            eq(List.of("hearing-judge", "hearing-legal-adviser")));
    }

    @Test
    void shouldCreateTimeUnboundHearingJudgeRoleWhenNoFutureHearing() {
        LocalDateTime startDate = LocalDateTime.now();

        HearingBooking hearing = HearingBooking.builder()
            .startDate(startDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEnterManually(YesNo.NO)
                .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idam")
                    .build())
                .build())
            .build();

        underTest.assignHearingJudge(12345L, hearing, Optional.empty(), false);

        verify(roleAssignmentService).assignJudgesRole(
            12345L,
            List.of("idam"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate.atZone(ZONE),
            null);
    }

    @Test
    void shouldCreateRoleStartingNowNotStartDateIfOnlyHearing() {
        LocalDateTime now = LocalDateTime.now();
        final ZonedDateTime nowZoned = now.atZone(ZONE);

        HearingBooking hearing = HearingBooking.builder()
            .startDate(now.plusDays(2))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEnterManually(YesNo.NO)
                .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idam")
                    .build())
                .build())
            .build();

        try (MockedStatic<ZonedDateTime> zonedStatic = mockStatic(ZonedDateTime.class)) {
            zonedStatic.when(() -> ZonedDateTime.now(ZONE)).thenReturn(nowZoned);

            underTest.assignHearingJudge(12345L, hearing, Optional.empty(), true);

            verify(roleAssignmentService).assignJudgesRole(
                12345L,
                List.of("idam"),
                JudgeCaseRole.HEARING_JUDGE,
                nowZoned,
                null);
        }
    }

    @Test
    void shouldCreateTimeBoundHearingJudgeRoleWithFutureHearing() {
        LocalDateTime now = LocalDateTime.now();

        HearingBooking hearing = HearingBooking.builder()
            .startDate(now)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEnterManually(YesNo.NO)
                .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idam")
                    .build())
                .build())
            .build();

        HearingBooking futureHearing = hearing.toBuilder()
            .startDate(now.plusDays(2))
            .build();

        underTest.assignHearingJudge(12345L, hearing, Optional.of(futureHearing), false);

        verify(roleAssignmentService).assignJudgesRole(
            12345L,
            List.of("idam"),
            JudgeCaseRole.HEARING_JUDGE,
            now.atZone(ZONE),
            now.plusDays(2).atZone(ZONE));
    }

    @Nested
    class GetJudgeId {
        @Test
        void shouldLookupJudgeIdWhenPersonalCodeOnly() {
            HearingBooking booking = HearingBooking.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeEnterManually(YesNo.NO)
                    .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("code")
                        .build())
                    .build())
                .build();

            underTest.getJudgeIdFromHearing(booking);

            verify(judicialApi).findUsers(any(), any(), anyInt(),
                any(), eq(JudicialUserRequest.fromPersonalCode("code")));
        }

        @Test
        void shouldReturnJudgeIdWhenIdamOnly() {
            HearingBooking booking = HearingBooking.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeEnterManually(YesNo.NO)
                    .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                    .judgeJudicialUser(JudicialUser.builder()
                        .idamId("idam")
                        .build())
                    .build())
                .build();

            Optional<String> idamId = underTest.getJudgeIdFromHearing(booking);

            assertThat(idamId.get()).isEqualTo("idam");
            verifyNoInteractions(judicialApi);
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoIdamOrPersonalCode() {
            HearingBooking booking = HearingBooking.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeEnterManually(YesNo.NO)
                    .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                    .judgeJudicialUser(JudicialUser.builder()
                        .build())
                    .build())
                .build();

            Optional<String> idamId = underTest.getJudgeIdFromHearing(booking);

            assertThat(idamId).isEqualTo(Optional.empty());
            verifyNoInteractions(judicialApi);
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoJudicialUser() {
            HearingBooking booking = HearingBooking.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeEnterManually(YesNo.NO)
                    .judgeTitle(JudgeOrMagistrateTitle.OTHER)
                    .build())
                .build();

            Optional<String> idamId = underTest.getJudgeIdFromHearing(booking);

            assertThat(idamId).isEqualTo(Optional.empty());
            verifyNoInteractions(judicialApi);
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoHearingJudge() {
            HearingBooking booking = HearingBooking.builder()
                .build();

            Optional<String> idamId = underTest.getJudgeIdFromHearing(booking);

            assertThat(idamId).isEqualTo(Optional.empty());
            verifyNoInteractions(judicialApi);
        }
    }

}
