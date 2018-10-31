package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-submission")
public class CaseSubmissionController {

    public static final String JURISDICTION_ID = "PUBLICLAW";
    public static final String CASE_TYPE = "Shared_Storage_DRAFTType";
    private final HTMLToPDFConverter converter = new HTMLToPDFConverter();

    @Autowired
    private CaseService caseService;

    @PostMapping
    public ResponseEntity submittedCase(
        @RequestHeader(value = "serviceauthorization") String serviceAuthorization,
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody @NotNull Map<String, Object> caseData) {
        System.out.println("Service authorization: " + serviceAuthorization);
        System.out.println("Authorization: " + authorization);
        System.out.println("User Id: " + userId);
        System.out.println("Case data: " + caseData);

        caseService.handleCaseSubmission(authorization, serviceAuthorization,
            userId, caseData);

        return new ResponseEntity(HttpStatus.OK);
    }


}
