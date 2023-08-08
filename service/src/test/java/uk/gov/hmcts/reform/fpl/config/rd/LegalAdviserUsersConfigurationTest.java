package uk.gov.hmcts.reform.fpl.config.rd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.rd.client.StaffApi;
import uk.gov.hmcts.reform.rd.model.StaffProfile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalAdviserUsersConfigurationTest {

    private static final StaffProfile ADVISER = StaffProfile.builder()
        .emailId("email@test.com")
        .caseWorkerId("12345")
        .build();

    private static final List<StaffProfile> STAFF = List.of(ADVISER);

    @Mock
    private StaffApi staffApi;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void beforeEach() {
        when(systemUserService.getSysUserToken()).thenReturn("token");
        when(staffApi.getAllStaffResponseDetails(any(), any(), anyInt(), any(), any())).thenReturn(STAFF);
    }

    @Test
    void shouldGetJudgeUUIDIfInMapping() {
        LegalAdviserUsersConfiguration config = new LegalAdviserUsersConfiguration(systemUserService,
            authTokenGenerator, staffApi, true);

        Optional<String> uuid = config.getLegalAdviserUUID("email@test.com");
        assertThat(uuid.isPresent()).isTrue();
        assertThat(uuid.get()).isEqualTo("12345");
    }

    @Test
    void shouldGetAllJudges() {
        LegalAdviserUsersConfiguration config = new LegalAdviserUsersConfiguration(systemUserService,
            authTokenGenerator, staffApi, true);

        Map<String, String> judges = config.getAllLegalAdvisers();
        assertThat(judges).isEqualTo(Map.of("email@test.com", "12345"));

    }
}
