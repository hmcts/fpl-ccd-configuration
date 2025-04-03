package uk.gov.hmcts.reform.fpl.service;

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
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private ArgumentCaptor<RoleAssignment> roleAssignmentCaptor;

    @InjectMocks
    private JudicialService underTest;

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
}
