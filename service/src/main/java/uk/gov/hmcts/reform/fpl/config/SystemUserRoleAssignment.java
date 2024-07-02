package uk.gov.hmcts.reform.fpl.config;

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

    @PostConstruct
    public void init() {
        try {
            log.info("Attempting to assign system-update user role");
            roleAssignmentService.assignSystemUserRole();
            log.info("Assigned role successfully");
        } catch (Exception e) {
            log.error("Could not automatically create system user role assignment", e);
        }
    }
}
