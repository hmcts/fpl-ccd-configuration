package uk.gov.hmcts.reform.fpl;

import com.google.common.io.Resources;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CftlibConfig implements CFTLibConfigurer {
    @Override
    public void configure(CFTLib lib) throws Exception {
        lib.createProfile("TEST_SOLICITOR@mailinator.com", "PUBLICLAW", "CARE_SUPERVISION_EPO", "Open");
        var roles = new String[] {
            "caseworker-publiclaw-systemupdate",
            "caseworker-publiclaw-judiciary",
            "caseworker-publiclaw-bulkscan",
            "caseworker-publiclaw-bulkscansystemupdate",
            "caseworker-publiclaw-solicitor",
            "caseworker-publiclaw-superuser",
            "caseworker-publiclaw-magistrate",
            "caseworker-approver",
            "caseworker-publiclaw-cafcass",
            "caseworker-publiclaw-gatekeeper",
            "caseworker-publiclaw-localAuthority",
            "caseworker-publiclaw-courtadmin",
            "caseworker-caa",
            "caseworker-ras-validation",
            "GS_profile",
            "caseworker-wa-task-configuration",
            "caseworker-publiclaw-cafcasssystemupdate",
            "ctsc",
            "hearing-centre-admin"
        };
        lib.createRoles(roles);

        var json = Resources.toString(Resources.getResource("cftlib-am-role-assignments.json"), StandardCharsets.UTF_8);
        lib.configureRoleAssignments(json);

        var def = Files.readAllBytes(Path.of("../build/fpl.xlsx"));
        lib.importDefinition(def);
    }
}
