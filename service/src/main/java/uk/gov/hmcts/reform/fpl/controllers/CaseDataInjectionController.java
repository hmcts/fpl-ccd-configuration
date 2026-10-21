package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cdi.CaseDataInjectionResponse;
import uk.gov.hmcts.reform.fpl.service.CaseDataInjectionService;

@Slf4j
@RestController
@RequestMapping("/callback/case-data-injection")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataInjectionController extends CallbackController {

    private final CaseDataInjectionService caseDataInjectionService;

    @PostMapping("/get-case")
    public CaseDataInjectionResponse getCaseMetadata(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        log.info("CDI get-case callback invoked for caseId={}, state={}, dateSubmitted={}",
            caseData != null ? caseData.getId() : null,
            caseData != null && caseData.getState() != null ? caseData.getState().getValue() : null,
            caseData != null ? caseData.getDateSubmitted() : null);

        CaseDataInjectionResponse response = caseDataInjectionService.generate(caseData);
        log.info("CDI metadata fields generated: {}", response.getMetadataFields());

        return response;
    }
}


