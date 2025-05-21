package uk.gov.hmcts.reform.fpl.config.rd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.service.ElinksService;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JudicialUsersConfigurationTest {

    private static final JudicialUserProfile JUDGE = JudicialUserProfile.builder()
        .emailId("email@test.com")
        .sidamId("12345")
        .build();

    private static final List<JudicialUserProfile> JUPS = List.of(JUDGE);

    @Mock
    private JudicialApi jrdApi;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ElinksService elinksService;

    @BeforeEach
    void beforeEach() {
        when(systemUserService.getSysUserToken()).thenReturn("token");
        when(jrdApi.findUsers(any(), any(), anyInt(), any(), any())).thenReturn(JUPS);
    }

    @Test
    void shouldGetJudgeUUIDIfInMapping() {
        JudicialUsersConfiguration config = new JudicialUsersConfiguration(jrdApi, systemUserService,
            authTokenGenerator, elinksService, true);

        Optional<String> uuid = config.getJudgeUUID("email@test.com");
        assertThat(uuid.isPresent()).isTrue();
        assertThat(uuid.get()).isEqualTo("12345");
    }

    @Test
    void shouldGetAllJudges() {
        JudicialUsersConfiguration config = new JudicialUsersConfiguration(jrdApi, systemUserService,
            authTokenGenerator, elinksService, true);

        Map<String, JudicialUserProfile> judges = config.getAllJudges();
        assertThat(judges.get("email@test.com").getSidamId()).isEqualTo("12345");
    }
}
