package uk.gov.hmcts.reform.fpl.config.rd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Configuration
public class LegalAdviserUsersConfiguration {

    private Map<String, String> mapping;

    private final JudicialService judicialService;

    public LegalAdviserUsersConfiguration(@Autowired JudicialService judicialService) {
        this.judicialService = judicialService;
        log.info("Attempting to gather all legal-advisers");
        mapping = this.judicialService.getAllLegalAdvisers();
        log.info("Obtained all legal-advisers");
    }

}
