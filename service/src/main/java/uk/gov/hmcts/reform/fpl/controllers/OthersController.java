package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
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

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;

@Api
@RestController
@RequestMapping("/callback/enter-others")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OthersController {
    private final ObjectMapper mapper;
    private final ConfidentialDetailsService confidentialService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Other>> others = confidentialService.combineOtherDetails(caseData.getAllOthers(),
            caseData.getConfidentialOthers());

        caseDetails.getData().put("others", Others.builder()
            .firstOther(others.get(0).getValue())
            .additionalOthers(getAdditionalOthers(others))
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Other>> confidentialOthers = confidentialService.getConfidentialDetails(caseData.getAllOthers());

        confidentialService.addConfidentialDetailsToCase(caseDetails, confidentialOthers, OTHER);

        List<Element<Other>> others = confidentialService.removeConfidentialDetails(caseData.getAllOthers());

        caseDetails.getData().put("others", Others.builder()
            .firstOther(others.get(0).getValue())
            .additionalOthers(getAdditionalOthers(others))
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<Element<Other>> getAdditionalOthers(List<Element<Other>> others) {
        if (isNotEmpty(others)) {
            others.remove(0);
        }
        return others;
    }
}
