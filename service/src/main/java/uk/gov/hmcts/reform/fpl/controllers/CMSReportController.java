package uk.gov.hmcts.reform.fpl.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@RestController
@RequestMapping("/callback/cms-report/")
public class CMSReportController extends CallbackController {
    public static final String CMS_REPORT_DETAILS = "cmsReportDetails";

    @PostMapping("/fetch-data/mid-event")
    public CallbackResponse handleValidateCollectionMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();

        caseDetails.getData().put(CMS_REPORT_DETAILS, "<div>Hello report</div>");

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();

        caseDetails.getData().remove(CMS_REPORT_DETAILS);

        return respond(caseDetails);
    }
}
