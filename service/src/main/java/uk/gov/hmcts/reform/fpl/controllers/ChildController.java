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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Api
@RestController
@RequestMapping("/callback/enter-children")
public class ChildController {

    private final ObjectMapper mapper;
    private final ChildrenService childrenService;

    @Autowired
    public ChildController(ObjectMapper mapper, ChildrenService childrenService) {
        this.mapper = mapper;
        this.childrenService = childrenService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("children1", childrenService.expandChildrenCollection(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    //Mid event is never triggered - needs discussion with PO regarding legality of case for child with DOB in future
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validate(caseDetails))
            .build();
    }

    //TODO ChildControllerAboutToSubmitTest (or combine the 3 controller test files into one for bonus points...)
    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Child>> confidentialChildren = childrenService.buildConfidentialChildrenList(caseData);
        if (confidentialChildren.size() != 0) {
            caseDetails.getData().put("confidentialChildren", confidentialChildren);
        } else {
            caseDetails.getData().remove("confidentialChildren");
        }

        //Fixes expand collection 'bug' if user removes all children and submits (will not re-open collection)
        //Also stops empty children (i.e user submit blank child form) being added to tab
        if (childrenService.userInputtedChildExists(caseData.getChildren1())) {
            caseDetails.getData().put("children1", childrenService.modifyHiddenValues(caseData));
        } else {
            caseDetails.getData().remove("children1");
        }

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
