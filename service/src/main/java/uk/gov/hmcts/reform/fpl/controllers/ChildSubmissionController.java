package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.AdditionalChild;
import uk.gov.hmcts.reform.fpl.model.Children;
import uk.gov.hmcts.reform.fpl.service.ChildrenMapperService;

import java.util.*;

import static uk.gov.hmcts.reform.fpl.validators.DateOfBirthValidator.dateOfBirthIsInFuture;

@Api
@RestController
@RequestMapping("/callback/enter-children")
@SuppressWarnings("unchecked")
public class ChildSubmissionController {

    private final ChildrenMapperService childrenMapperService;
    private final String DOB_IN_FUTURE_ERROR_MESSAGE = "Date of birth cannot be in the future";
    private final Logger logger = LoggerFactory.getLogger(ChildSubmissionController.class);

    @Autowired
    public ChildSubmissionController(final ChildrenMapperService childrenMapperService) {
        this.childrenMapperService = childrenMapperService;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        boolean addError = false;
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        List<String> errorsList = new ArrayList<>();

        Optional<Children> children = childrenMapperService.mapChildren((Map<String, Object>) caseDetails.getData().get("children"));
        if (dateOfBirthIsInFuture(children.get().getFirstChild().getChildDOB())) {
            addError = true;
        } else {
            Iterator<AdditionalChild> childIterator = children.get().getAdditionalChildren().iterator();
            while (childIterator.hasNext()) {
                if (dateOfBirthIsInFuture(childIterator.next().getChild().getChildDOB())) {
                    addError = true;
                }
            }
        }

        if (addError) {
            errorsList.add(DOB_IN_FUTURE_ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(Collections.unmodifiableList(errorsList))
            .build();
    }

}
