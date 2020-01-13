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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.CHILD;

@Api
@RestController
@RequestMapping("/callback/enter-children")
public class ChildController {
    private final ObjectMapper mapper;
    private final ChildrenService childrenService;
    private final ConfidentialDetailsService confidentialDetailsService;

    @Autowired
    public ChildController(ObjectMapper mapper,
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

        caseDetails.getData().put("children1", childrenService.prepareChildren(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validate(caseDetails))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Child>> confidentialChildren =
            confidentialDetailsService.addPartyMarkedConfidentialToList(caseData.getAllChildren());

        confidentialDetailsService.addConfidentialDetailsToCaseDetails(caseDetails, confidentialChildren, CHILD);

        caseDetails.getData().put("children1", childrenService.modifyHiddenValues(caseData.getAllChildren()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getChildren1() != null) {
            caseData.getChildren1().stream()
                .map(Element::getValue)
                .map(Child::getParty)
                .map(Party::getDateOfBirth)
                .filter(Objects::nonNull)
                .filter(dateOfBirth -> dateOfBirth.isAfter(LocalDate.now()))
                .findAny()
                .ifPresent(date -> errors.add("Date of birth cannot be in the future"));
        }

        return errors.build();
    }
}
