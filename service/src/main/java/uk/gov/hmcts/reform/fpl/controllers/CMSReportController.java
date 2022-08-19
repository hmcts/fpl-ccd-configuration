package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.CMSReportService;

@RestController
@RequestMapping("/callback/cms-report/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CMSReportController extends CallbackController {
    public static final String CMS_REPORT_DETAILS = "cmsReportDetails";
    private final CMSReportService cmsReportService;

    @PostMapping("/fetch-data/mid-event")
    public CallbackResponse handleValidateCollectionMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();

        caseDetails.getData().put(CMS_REPORT_DETAILS, cmsReportService.getReport(getCaseData(caseDetails)));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();

        caseDetails.getData().remove(CMS_REPORT_DETAILS);

        return respond(caseDetails);
    }
}
