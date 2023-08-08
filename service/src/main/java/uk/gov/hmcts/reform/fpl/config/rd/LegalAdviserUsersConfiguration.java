package uk.gov.hmcts.reform.fpl.config.rd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.rd.client.StaffApi;
import uk.gov.hmcts.reform.rd.model.StaffProfile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Configuration
public class LegalAdviserUsersConfiguration {

    public static final String SERVICE_CODE = "ABA3";
    private static final String LEGAL_ADVISER_JOB_CODE = "3";
    private static final int STAFF_PAGE_SIZE = 500;

    private Map<String, String> mapping;

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final StaffApi staffApi;

    public LegalAdviserUsersConfiguration(@Autowired SystemUserService systemUserService,
                                          @Autowired AuthTokenGenerator authTokenGenerator,
                                          @Autowired StaffApi staffApi,
                                          @Value("${rd_staff.api.enabled:false}") boolean staffEnabled) {
        this.systemUserService = systemUserService;
        this.authTokenGenerator = authTokenGenerator;
        this.staffApi = staffApi;
        log.info("Attempting to gather all legal advisers");
        if (staffEnabled) {
            mapping = this.getAllLegalAdvisers();
        } else {
            mapping = Map.of();
        }
        log.info("Loaded {} legal advisers", mapping.size());
    }

    public Optional<String> getLegalAdviserUUID(String email) {
        return Optional.ofNullable(mapping.getOrDefault(email, null));
    }

    public Map<String, String> getAllLegalAdvisers() {
        String systemUserToken = systemUserService.getSysUserToken();

        List<StaffProfile> staff = staffApi.getAllStaffResponseDetails(systemUserToken, authTokenGenerator.generate(),
            STAFF_PAGE_SIZE, SERVICE_CODE, LEGAL_ADVISER_JOB_CODE);

        return staff.stream()
            .collect(Collectors.toMap(StaffProfile::getEmailId, StaffProfile::getCaseWorkerId));
    }

}
