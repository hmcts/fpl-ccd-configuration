package uk.gov.hmcts.reform.fpl.testingsupport.controllers;

import feign.FeignException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnExpression("${testing.support.enabled:false}")
@SuppressWarnings("unchecked")
public class TestingSupportController {
    private static final String POPULATE_EVENT_ID_TEMPLATE = "populateCase-%s";
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/testing-support/case/populate/{caseId}")
    public void populateCase(@PathVariable("caseId") Long caseId, @RequestBody Map<String, Object> requestBody) {
        State state = State.valueOf(requestBody.get("state").toString());
        Map<String, Object> caseData = (Map<String, Object>) requestBody.get("caseData");

        try {
            coreCaseDataService.triggerEvent(JURISDICTION,
                CASE_TYPE,
                caseId,
                String.format(POPULATE_EVENT_ID_TEMPLATE, state.getValue()),
                caseData);
        } catch (FeignException e) {
            log.error(String.format("Populate case event failed: %s", e.contentUTF8()));
            throw e;
        }
    }
}
