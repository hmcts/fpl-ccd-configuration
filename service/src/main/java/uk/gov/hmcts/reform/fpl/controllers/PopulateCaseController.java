package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.service.PopulateCaseService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.State.PREPARE_FOR_HEARING;


@Api
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(prefix = "populate_case", name = "enabled", havingValue = "true")
public class PopulateCaseController {
    private static final String POPULATE_EVENT_ID_TEMPLATE = "populateCase-%s";
    private final CoreCaseDataService coreCaseDataService;
    private final PopulateCaseService populateCaseService;

    @PostMapping("/populateCase/{caseId}/{newState}")
    public void populateCase(@PathVariable("caseId") Long caseId, @PathVariable("newState") State newState,
                             @RequestBody Map<String, Object> data) {
        if (Boolean.TRUE.toString().equals(data.get("updateTimeBasedAndDocumentData"))) {
            data.putAll(populateCaseService.getTimeBasedAndDocumentData());
            if (PREPARE_FOR_HEARING.equals(newState)) {
                data.put("standardDirectionOrder", populateCaseService.getUpdatedSDOData(data));
            }
            data.remove("updateTimeBasedAndDocumentData");
        }

        coreCaseDataService.triggerEvent(JURISDICTION,
            CASE_TYPE,
            caseId,
            String.format(POPULATE_EVENT_ID_TEMPLATE, newState.getValue()),
            data);
    }
}
