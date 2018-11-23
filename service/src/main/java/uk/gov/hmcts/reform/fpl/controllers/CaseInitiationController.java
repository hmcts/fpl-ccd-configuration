package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityNameService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
public class CaseInitiationController {

    private final LocalAuthorityNameService localAuthorityNameService;

    @Autowired
    public CaseInitiationController(LocalAuthorityNameService localAuthorityNameService) {
        this.localAuthorityNameService = localAuthorityNameService;
    }

    @PostMapping
    public ResponseEntity createdCase(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode(authorization);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();
        data.put("caseLocalAuthority", caseLocalAuthority);

        AboutToStartOrSubmitCallbackResponse body = AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();

        return ResponseEntity.ok(body);
    }
}
