package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.Optional;

@RestController
@RequestMapping("/callback/enter-c1-with-supplement")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EnterC1WithSupplementController extends CallbackController {

    private final ManageDocumentService manageDocumentService;

    private static final String SUBMITTED_C1_WITH_SUPPLEMENT_KEY = "submittedC1WithSupplement";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (caseData.getSubmittedC1WithSupplement() != null) {
            caseDetails.getData().put(SUBMITTED_C1_WITH_SUPPLEMENT_KEY, caseData.getSubmittedC1WithSupplement());
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (YesNo.YES.getValue().equalsIgnoreCase(Optional.ofNullable(caseData.getSubmittedC1WithSupplement())
            .orElse(SubmittedC1WithSupplementBundle.builder().clearSubmittedC1WithSupplement(YesNo.NO.getValue())
                .build())
            .getClearSubmittedC1WithSupplement())) {
            caseDetails.getData().remove(SUBMITTED_C1_WITH_SUPPLEMENT_KEY);
        } else if (caseData.getSubmittedC1WithSupplement() != null) {
            caseData.getSubmittedC1WithSupplement().getSupportingEvidenceBundle().forEach(seb -> {
                seb.getValue().setUploaderCaseRoles(manageDocumentService.getUploaderCaseRoles(caseData));
                seb.getValue().setUploaderType(manageDocumentService.getUploaderType(caseData));
            });
            caseDetails.getData().put(SUBMITTED_C1_WITH_SUPPLEMENT_KEY, caseData.getSubmittedC1WithSupplement());
        }

        return respond(caseDetails);
    }
}
