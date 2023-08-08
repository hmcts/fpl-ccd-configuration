package uk.gov.hmcts.reform.fpl.config.rd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import java.util.Map;

@Slf4j
@Component
@Configuration
public class JudicialUsersConfiguration {

    private Map<String, String> mapping;

    private final JudicialService judicialService;

    public JudicialUsersConfiguration(@Autowired JudicialService judicialService) {
        this.judicialService = judicialService;
        log.info("Attempting to gather all judges");
        mapping = this.judicialService.getAllJudges();
        log.info("Obtained all judges");
    }

}
