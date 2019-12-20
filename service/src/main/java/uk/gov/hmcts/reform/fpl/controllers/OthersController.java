package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/enter-others")
public class OthersController {

    private final ObjectMapper mapper;
    private final OthersService othersService;

    @Autowired
    public OthersController(ObjectMapper mapper, OthersService othersService) {
        this.mapper = mapper;
        this.othersService = othersService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("others", othersService.expandOthersCollection(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Other>> confidentialOthers = othersService.getConfidentialOthers(caseData);
        if (isNotEmpty(confidentialOthers)) {
            caseDetails.getData().put("confidentialOthers", confidentialOthers);
        } else {
            caseDetails.getData().remove("confidentialOthers");
        }

        if (isNotEmpty(caseData.getOthers().getAdditionalOthers())
            && !caseData.getOthers().getAdditionalOthers().get(0).getValue().equals(Other.builder()
            .address(Address.builder()
                .build())
            .build())) {
            caseDetails.getData().put("others", othersService.handleFirstOtherAndHideConfidentialValues(caseData));
        } else {
            caseDetails.getData().remove("others");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
