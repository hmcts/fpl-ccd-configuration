package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.config.TimeConfiguration.LONDON_TIMEZONE;
import static uk.gov.hmcts.reform.fpl.service.HearingJudgeService.HEARING_ROLES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class HearingJudgeServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private JudicialService judicialService;

    @InjectMocks
    private HearingJudgeService underTest;

    @BeforeEach
    void setUp() {
        when(judicialService.getJudge("P1")).thenReturn(Optional.of(JudicialUserProfile.builder()
            .personalCode("P1")
            .sidamId("SIDAM1")
            .title(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE.toString())
            .build()));
        when(judicialService.getJudge("P2")).thenReturn(Optional.of(JudicialUserProfile.builder()
            .personalCode("P2")
            .sidamId("SIDAM2")
            .title(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE.toString())
            .build()));
        when(judicialService.getJudge("P3")).thenReturn(Optional.of(JudicialUserProfile.builder()
            .personalCode("P3")
            .sidamId("SIDAM3")
            .title(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE.toString())
            .build()));
    }

    @Test
    void shouldAssignInitialHearingJudge() {
        LocalDateTime startDate = LocalDateTime.now(LONDON_TIMEZONE);
        List<RoleAssignment> currentRoles = List.of();

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(element(HearingBooking.builder()
            .startDate(startDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
                .judgeLastName("Smith")
                .judgeJudicialUser(JudicialUser.builder()
                    .personalCode("P1")
                    .build())
                .build())
            .build()));

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());
        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM1"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate.atZone(LONDON_TIMEZONE),
            null);

        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void shouldAssignInitialHearingLegalAdviser() {
        LocalDateTime startDate = LocalDateTime.now(LONDON_TIMEZONE);
        List<RoleAssignment> currentRoles = List.of();

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(element(HearingBooking.builder()
            .startDate(startDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                .judgeLastName("Smith")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("SIDAM1")
                    .build())
                .build())
            .build()));

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());
        verify(roleAssignmentService).assignLegalAdvisersRole(12345L,
            List.of("SIDAM1"),
            LegalAdviserRole.HEARING_LEGAL_ADVISER,
            startDate.atZone(LONDON_TIMEZONE),
            null);

        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void shouldAssignFollowUpJudge() {
        LocalDateTime startDate1 = LocalDateTime.now(LONDON_TIMEZONE).minusDays(1);
        LocalDateTime startDate2 = LocalDateTime.now(LONDON_TIMEZONE).plusDays(2);

        List<RoleAssignment> currentRoles = List.of(
            RoleAssignment.builder()
                .id("role1")
                .actorId("SIDAM1")
                .beginTime(startDate1.atZone(LONDON_TIMEZONE))
                .build()
        );

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(
            element(HearingBooking.builder()
                .startDate(startDate1)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
                    .judgeLastName("Smith")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P1")
                        .build())
                    .build())
                .build()),
            element(HearingBooking.builder()
                .startDate(startDate2)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Jones")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P2")
                        .build())
                    .build())
                .build())
        );

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());

        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM1"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate1.atZone(LONDON_TIMEZONE),
            startDate2.atZone(LONDON_TIMEZONE));

        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM2"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate2.atZone(LONDON_TIMEZONE),
            null);

        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(0));
        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void shouldEditHearingJudgeRoleWhenHearingJudgeChanged() {
        LocalDateTime startDate1 = LocalDateTime.now(LONDON_TIMEZONE).minusDays(1);

        List<RoleAssignment> currentRoles = List.of(
            RoleAssignment.builder()
                .id("role1")
                .actorId("SIDAM1")
                .beginTime(startDate1.atZone(LONDON_TIMEZONE))
                .build());

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(
            element(HearingBooking.builder()
                .startDate(startDate1)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Jones")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P2")
                        .build())
                    .build())
                .build())
        );

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());

        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM2"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate1.atZone(LONDON_TIMEZONE),
            null);

        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(0));
        verifyNoMoreInteractions(roleAssignmentService);

    }

    @Test
    void shouldSyncRolesWhenHearingAddedBetweenHearings() {
        LocalDateTime startDate1 = LocalDateTime.now(LONDON_TIMEZONE).minusDays(1);
        LocalDateTime startDate2 = LocalDateTime.now(LONDON_TIMEZONE).plusDays(4);
        LocalDateTime newMiddleStart = LocalDateTime.now(LONDON_TIMEZONE).plusDays(2);

        List<RoleAssignment> currentRoles = List.of(
            RoleAssignment.builder()
                .id("role1")
                .actorId("SIDAM1")
                .beginTime(startDate1.atZone(LONDON_TIMEZONE))
                .endTime(startDate2.atZone(LONDON_TIMEZONE))
                .build(),
            RoleAssignment.builder()
                .id("role2")
                .actorId("SIDAM2")
                .beginTime(startDate2.atZone(LONDON_TIMEZONE))
                .build()
        );

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(
            element(HearingBooking.builder()
                .startDate(startDate1)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
                    .judgeLastName("Smith")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P1")
                        .build())
                    .build())
                .build()),
            element(HearingBooking.builder()
                .startDate(startDate2)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Jones")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P2")
                        .build())
                    .build())
                .build()),
            // new hearing added between the two existing hearings
            element(HearingBooking.builder()
                .startDate(newMiddleStart)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
                    .judgeLastName("Jackson")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P3")
                        .build())
                    .build())
                .build())
        );

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());

        // recreate role for hearing 1 as now ends earlier
        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM1"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate1.atZone(LONDON_TIMEZONE),
            newMiddleStart.atZone(LONDON_TIMEZONE));

        // create role for new middle hearing
        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM3"),
            JudgeCaseRole.HEARING_JUDGE,
            newMiddleStart.atZone(LONDON_TIMEZONE),
            startDate2.atZone(LONDON_TIMEZONE));

        // delete old role for hearing 1 as now invalid
        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(0));
        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void shouldRecreatePrecedingRoleWhenHearingTimeEdited() {
        LocalDateTime startDate1 = LocalDateTime.now(LONDON_TIMEZONE).minusDays(1);
        LocalDateTime startDate2 = LocalDateTime.now(LONDON_TIMEZONE).plusDays(4);
        LocalDateTime newStart2 = LocalDateTime.now(LONDON_TIMEZONE).plusDays(2);

        List<RoleAssignment> currentRoles = List.of(
            RoleAssignment.builder()
                .id("role1")
                .actorId("SIDAM1")
                .beginTime(startDate1.atZone(LONDON_TIMEZONE))
                .endTime(startDate2.atZone(LONDON_TIMEZONE))
                .build(),
            RoleAssignment.builder()
                .id("role2")
                .actorId("SIDAM2")
                .beginTime(startDate2.atZone(LONDON_TIMEZONE))
                .build()
        );

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(
            element(HearingBooking.builder()
                .startDate(startDate1)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
                    .judgeLastName("Smith")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P1")
                        .build())
                    .build())
                .build()),
            // hearing 2 has been brought forward to newStart2 time
            element(HearingBooking.builder()
                .startDate(newStart2)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Jones")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P2")
                        .build())
                    .build())
                .build())
        );

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());

        // recreate role for hearing 1 as now ends earlier at newStart2
        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM1"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate1.atZone(LONDON_TIMEZONE),
            newStart2.atZone(LONDON_TIMEZONE));

        // recreate role for hearing 2 as now starts earlier at newStart2
        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM2"),
            JudgeCaseRole.HEARING_JUDGE,
            newStart2.atZone(LONDON_TIMEZONE),
            null);

        // both roles deleted as invalid
        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(0));
        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(1));
        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void shouldAdjustRolesWhenHearingDeleted() {
        LocalDateTime startDate1 = LocalDateTime.now(LONDON_TIMEZONE).minusDays(1);
        LocalDateTime startDate2 = LocalDateTime.now(LONDON_TIMEZONE).plusDays(4);

        List<RoleAssignment> currentRoles = List.of(
            RoleAssignment.builder()
                .id("role1")
                .actorId("SIDAM1")
                .beginTime(startDate1.atZone(LONDON_TIMEZONE))
                .endTime(startDate2.atZone(LONDON_TIMEZONE))
                .build(),
            RoleAssignment.builder()
                .id("role2")
                .actorId("SIDAM2")
                .beginTime(startDate2.atZone(LONDON_TIMEZONE))
                .build()
        );

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(
            element(HearingBooking.builder()
                .startDate(startDate1)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
                    .judgeLastName("Smith")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P1")
                        .build())
                    .build())
                .build())
        );

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());

        // recreate role for hearing 1 as now doesn't end
        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM1"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate1.atZone(LONDON_TIMEZONE),
            null);

        // delete old role for both hearings as 1 invalid, 1 no longer needed
        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(0));
        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(1));
        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void shouldCreateRoleToFillGapUntilFirstHearing() {
        LocalDateTime startDate1 = LocalDateTime.now(LONDON_TIMEZONE).plusDays(4);

        List<RoleAssignment> currentRoles = List.of();

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of(
            element(HearingBooking.builder()
                .startDate(startDate1)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
                    .judgeLastName("Smith")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("P1")
                        .build())
                    .build())
                .build())
        );

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());

        // create role for hearing 1
        verify(roleAssignmentService).assignJudgesRole(12345L,
            List.of("SIDAM1"),
            JudgeCaseRole.HEARING_JUDGE,
            startDate1.atZone(LONDON_TIMEZONE),
            null);

        // create gap role to fill until hearing 1 starts
        verify(roleAssignmentService).assignJudgesRole(eq(12345L),
            eq(List.of("SIDAM1")),
            eq(JudgeCaseRole.HEARING_JUDGE),
            any(), // todo mock static?
            eq(startDate1.atZone(LONDON_TIMEZONE)));

        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void shouldDeleteAllRolesIfNoHearingsInFuture() {
        ZonedDateTime startDate1 = ZonedDateTime.now(LONDON_TIMEZONE).minusDays(4);
        ZonedDateTime startDate2 = ZonedDateTime.now(LONDON_TIMEZONE).minusDays(3);
        ZonedDateTime startDate3 = ZonedDateTime.now(LONDON_TIMEZONE).minusDays(2);
        List<RoleAssignment> currentRoles = List.of(
            RoleAssignment.builder()
                .id("role1")
                .actorId("SIDAM1")
                .beginTime(startDate1)
                .endTime(startDate2)
                .build(),
            RoleAssignment.builder()
                .id("role2")
                .actorId("SIDAM2")
                .beginTime(startDate2)
                .beginTime(startDate3)
                .build()
        );

        when(roleAssignmentService.getRolesOnCase(12345L, HEARING_ROLES)).thenReturn(currentRoles);

        List<Element<HearingBooking>> bookings = List.of();

        underTest.syncHearingJudgeRoles(12345L, bookings);

        verify(roleAssignmentService).getRolesOnCase(any(), any());
        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(0));
        verify(roleAssignmentService).deleteRoleAssignment(currentRoles.get(1));
        verifyNoMoreInteractions(roleAssignmentService);
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

            verify(judicialService).getJudge("code");
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
            verifyNoInteractions(judicialService);
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
            verifyNoInteractions(judicialService);
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
            verifyNoInteractions(judicialService);
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoHearingJudge() {
            HearingBooking booking = HearingBooking.builder()
                .build();

            Optional<String> idamId = underTest.getJudgeIdFromHearing(booking);

            assertThat(idamId).isEqualTo(Optional.empty());
            verifyNoInteractions(judicialService);
        }
    }



}
