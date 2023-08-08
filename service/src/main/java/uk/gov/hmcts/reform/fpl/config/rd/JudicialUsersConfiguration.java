package uk.gov.hmcts.reform.fpl.config.rd;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
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
                                      @Autowired AuthTokenGenerator authTokenGenerator,
                                      @Value("${rd_judicial.api.enabled:false}") boolean jrdEnabled) {
        this.judicialApi = judicialApi;
        this.systemUserService = systemUserService;
        this.authTokenGenerator = authTokenGenerator;
        log.info("Attempting to gather all judges");
        if (jrdEnabled) {
            mapping = this.getAllJudges();
        } else {
            mapping = Map.of();
        }
        log.info("Loaded {} judges", mapping.size());
    }

    public Optional<String> getJudgeUUID(String email) {
        return Optional.ofNullable(mapping.getOrDefault(email, null));
    }

    @Retryable(value = FeignException.class, recover = "recoverFailedJudgeCall")
    public Map<String, String> getAllJudges() {
        String systemUserToken = systemUserService.getSysUserToken();

        List<JudicialUserProfile> users = judicialApi.findUsers(systemUserToken, authTokenGenerator.generate(),
            JUDICIAL_PAGE_SIZE,
            JudicialUserRequest.builder()
                .ccdServiceName("PUBLICLAW")
                .build());

        return users.stream()
            .filter(jup -> !isEmpty(jup.getSidamId()))
            .collect(Collectors.toMap(JudicialUserProfile::getEmailId, JudicialUserProfile::getSidamId));
    }

    @Recover
    public Map<String, String> recoverFailedJudgeCall(FeignException e) {
        log.error("Could not download list of publiclaw judiciary from JRD", e);
        return Map.of();
    }

}
