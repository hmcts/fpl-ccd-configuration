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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.DfjAreaCourtMapping;
import uk.gov.hmcts.reform.fpl.service.CourtSelectionService;
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;

import java.util.Objects;

@RestController
@RequestMapping("/callback/select-court")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtSelectionController extends CallbackController {

    private final CourtSelectionService courtSelectionService;
    private final DfjAreaLookUpService dfjAreaLookUpService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("courtsList", courtSelectionService.getCourtsList(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        Court selectedCourt = courtSelectionService.getSelectedCourt(caseData);
        caseDetails.getData().put("court", selectedCourt);
        caseDetails.getData().remove("courtsList");

        if (Objects.nonNull(selectedCourt)) {
            DfjAreaCourtMapping dfjArea = dfjAreaLookUpService.getDfjArea(selectedCourt.getCode());
            caseDetails.getData().keySet().removeAll(dfjAreaLookUpService.getAllCourtFields());
            caseDetails.getData().put("dfjArea", dfjArea.getDfjArea());
            caseDetails.getData().put(dfjArea.getCourtField(), selectedCourt.getCode());
        }

        return respond(caseDetails);
    }

}
