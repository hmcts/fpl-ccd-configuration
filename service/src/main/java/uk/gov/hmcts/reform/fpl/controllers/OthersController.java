package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;

@Api
@RestController
@RequestMapping("/callback/enter-others")
public class OthersController {
    private final ObjectMapper mapper;
    private final ChildrenService childrenService;
    private final OthersService othersService;
    private final ConfidentialDetailsService confidentialDetailsService;

    @Autowired
    public OthersController(ObjectMapper mapper,
                            ChildrenService childrenService,
                            ConfidentialDetailsService confidentialDetailsService,
                            OthersService othersService) {
        this.mapper = mapper;
        this.childrenService = childrenService;
        this.confidentialDetailsService = confidentialDetailsService;
        this.othersService = othersService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        //caseDetails.getData().put("others", childrenService.prepareOthers(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Other>> confidentialOther = othersService.getAllConfidentialOther(caseData);

        final List <Element<Others>> confidentialOthersForCaseData = othersService.prepareConfidentialOthersForCaseData(confidentialOther);

        if(!confidentialOthersForCaseData.isEmpty())
        {
            //puts this into confidentialOthers only if not empty as tab appears if not
            confidentialDetailsService.addConfidentialDetailsToCaseDetails(caseDetails, confidentialOthersForCaseData, OTHER);
        }

        Others other = confidentialOthersForCaseData.get(0).getValue();

        caseDetails.getData().put("others", Others.builder().firstOther(other.getFirstOther()).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
