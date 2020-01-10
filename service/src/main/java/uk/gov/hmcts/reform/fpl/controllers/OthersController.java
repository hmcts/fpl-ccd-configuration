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
    private final ConfidentialDetailsService confidentialDetailsService;

    @Autowired
    public OthersController(ObjectMapper mapper,
                            ChildrenService childrenService,
                            ConfidentialDetailsService confidentialDetailsService) {
        this.mapper = mapper;
        this.childrenService = childrenService;
        this.confidentialDetailsService = confidentialDetailsService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("others", childrenService.prepareOthers(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        //GET ALL OTHERS
        final List <Element<Other>> getExistingOthers = new ArrayList<>();

        caseData.getAllOthers().forEach(element -> {
            if (element.containsConfidentialDetails()) {

                System.out.println("Confidential details contained");
                caseData.getAllOthers().forEach(other -> getExistingOthers.add(Element.<Other>builder()
                    .id(UUID.randomUUID())
                    .value(other)
                    .build()));
            }
        });

        //caseDetails.getData().put("others", childrenService.modifyHiddenValues(caseData.getAllChildren()));

        Other firstOther = Other.builder().name("Test").build();
        final List <Element<Other>> additionalOthers = new ArrayList<>();
        additionalOthers.add(Element.<Other>builder()
            .id(UUID.randomUUID())
            .value(firstOther)
            .build());

        Others other = new Others(firstOther,getExistingOthers);

        final List <Element<Others>> others = new ArrayList<>();

        others.add(Element.<Others>builder().value(other).build());

        caseDetails.getData().put("confidentialOthers",others);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
