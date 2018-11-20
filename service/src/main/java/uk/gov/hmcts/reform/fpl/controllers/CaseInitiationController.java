package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
public class CaseInitiationController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity createdCase(@RequestHeader(value = "authorization") String authorization) {

        String caseLocalAuthority = userService.extractUserDomainName(authorization);

        AboutToStartOrSubmitCallbackResponse body = AboutToStartOrSubmitCallbackResponse.builder()
            .data(prepareLocalAuthority(caseLocalAuthority))
            .build();

        return ResponseEntity.ok(body);
    }

    private Map<String, Object> prepareLocalAuthority(String caseLocalAuthority) {
        return ImmutableMap.<String, Object>builder()
            .put("caseLocalAuthority", caseLocalAuthority)
            .build();
    }
}
