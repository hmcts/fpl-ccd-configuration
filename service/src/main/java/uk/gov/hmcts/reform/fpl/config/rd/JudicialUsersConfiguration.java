package uk.gov.hmcts.reform.fpl.config.rd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.service.JudicialService.JUDICIAL_PAGE_SIZE;

@Slf4j
@Component
@Configuration
public class JudicialUsersConfiguration {

    private Map<String, String> mapping;

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final JudicialApi judicialApi;


    public JudicialUsersConfiguration(@Autowired JudicialApi judicialApi,
                                      @Autowired SystemUserService systemUserService,
                                      @Autowired AuthTokenGenerator authTokenGenerator) {
        this.judicialApi = judicialApi;
        this.systemUserService = systemUserService;
        this.authTokenGenerator = authTokenGenerator;
        log.info("Attempting to gather all judges");
        mapping = this.getAllJudges();
        log.info("Obtained all judges");
    }

    public Optional<String> getJudgeUUID(String email) {
        return Optional.ofNullable(mapping.getOrDefault(email, null));
    }

    public Map<String, String> getAllJudges() {
        String systemUserToken = systemUserService.getSysUserToken();

        List<JudicialUserProfile> users = judicialApi.findUsers(systemUserToken, authTokenGenerator.generate(),
            JUDICIAL_PAGE_SIZE,
            JudicialUserRequest.builder()
                .ccdServiceName("PUBLICLAW")
                .build());

        return users.stream()
            .collect(Collectors.toMap(JudicialUserProfile::getEmailId, JudicialUserProfile::getSidamId));
    }

}
