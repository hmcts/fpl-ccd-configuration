package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-submission")
public class CaseSubmissionController {

    @Autowired
    private CaseService caseService;

    @PostMapping
    public ResponseEntity submittedCase(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody @NotNull CallbackRequest caseData) throws JSONException, IOException {
        System.out.println("Authorization: " + authorization);
        System.out.println("User Id: " + userId);
        System.out.println("Case data: " + caseData);

        caseService.handleCaseSubmission(authorization, userId, caseData);

        return new ResponseEntity(HttpStatus.OK);
    }
}
