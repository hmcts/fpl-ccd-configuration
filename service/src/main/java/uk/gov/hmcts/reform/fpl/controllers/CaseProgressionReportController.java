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
import uk.gov.hmcts.reform.fpl.events.CaseProgressionReportEvent;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@RestController
@RequestMapping("/callback/case-progression-report/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseProgressionReportController extends CallbackController {
    public static final String CMS_REPORT_DETAILS = "caseProgressionReportDetails";
    private final CaseProgressionReportService cmsReportService;
    private final IdamClient idamClient;
    private final RequestData requestData;

    @PostMapping("/fetch-data/mid-event")
    public CallbackResponse handleValidateCollectionMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();

        caseDetails.getData().put(CMS_REPORT_DETAILS, cmsReportService.getHtmlReport(getCaseData(caseDetails)));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        final UserDetails userDetails = idamClient.getUserDetails(requestData.authorisation());
        CaseDetails caseDetails = request.getCaseDetails();
        publishEvent(new CaseProgressionReportEvent(getCaseData(caseDetails), userDetails));

        caseDetails.getData().remove(CMS_REPORT_DETAILS);

        return respond(caseDetails);
    }
}
