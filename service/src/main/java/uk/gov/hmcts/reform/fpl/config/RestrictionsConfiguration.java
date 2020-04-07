package uk.gov.hmcts.reform.fpl.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Configuration
@Scope(SCOPE_SINGLETON)
@Getter
@Slf4j
public class RestrictionsConfiguration {
    private final List<String> localAuthorityCodesForbiddenCaseSubmission;

    public RestrictionsConfiguration(
        @Value("${fpl.local_authority_codes_forbidden_case_submission}")
        List<String> localAuthorityCodesForbiddenCaseSubmission
    ) {
        this.localAuthorityCodesForbiddenCaseSubmission = copyOf(localAuthorityCodesForbiddenCaseSubmission);
        log.info("Local authorities forbidden case submission: {}", localAuthorityCodesForbiddenCaseSubmission);
    }
}
