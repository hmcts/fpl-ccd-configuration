package uk.gov.hmcts.reform.fpl.config;

import feign.FeignException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;


@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(value = "create-system-user-role.enabled", havingValue = "true")
public class SystemUserRoleAssignment {

    private final RoleAssignmentService roleAssignmentService;

    private static final int MAX_ATTEMPTS = 5;
    private static final int DELAY_MILLIS = 5000;

    @PostConstruct
    public void init() {
        assignSystemUserRoleWithRetry();
    }

    private void assignSystemUserRoleWithRetry() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("Attempt {} to assign system-update user role", attempt);
                roleAssignmentService.assignSystemUserRole();
                log.info("Assigned role successfully");
                return;
            } catch (FeignException e) {
                log.error("Attempt {} failed to create system user role assignment (FeignException)", attempt, e);
                if (attempt < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(DELAY_MILLIS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry sleep interrupted", ie);
                        break;
                    }
                } else {
                    log.error("All attempts to assign system user role failed");
                }
            } catch (Exception e) {
                log.error("Unexpected error during system user role assignment", e);
                break;
            }
        }
    }
}
