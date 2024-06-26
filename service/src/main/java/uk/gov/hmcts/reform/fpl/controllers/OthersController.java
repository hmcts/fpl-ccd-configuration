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
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;

@RestController
@RequestMapping("/callback/enter-others")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OthersController extends CallbackController {
    private final ConfidentialDetailsService confidentialService;
    private final OthersService othersService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("others", othersService.prepareOthers(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Others updatedOthers = othersService.removeAddressOrAddressNotKnowReason(getCaseData(caseDetails));
        caseDetails.getData().put("others", updatedOthers);

        CaseData caseData = getCaseData(caseDetails);
        List<Element<Other>> allOthers = caseData.getAllOthers();

        confidentialService.addConfidentialDetailsToCase(caseDetails, allOthers, OTHER);

        List<Element<Other>> othersList = confidentialService.removeConfidentialDetails(allOthers);

        Others others = Others.from(othersList);
        if (isNull(others)) {
            caseDetails.getData().remove("others");
        } else {
            caseDetails.getData().put("others", others);
        }

        return respond(caseDetails);
    }

}
