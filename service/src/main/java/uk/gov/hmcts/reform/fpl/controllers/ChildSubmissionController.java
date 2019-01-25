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
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


@Api
@RestController
@RequestMapping("/callback/enter-children")
@SuppressWarnings("unchecked")
public class ChildSubmissionController {

    private final MapperService mapperService;
    private final String DOB_IN_FUTURE_ERROR_MESSAGE = "Date of birth cannot be in the future";
    private final Logger logger = LoggerFactory.getLogger(ChildSubmissionController.class);

    @Autowired
    public ChildSubmissionController(final MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        boolean addError = false;
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        List<String> errorsList = new ArrayList<>();

        try {
            Children children = mapperService.mapObject((Map<String, Object>) caseDetails.getData().get("children"), Children.class);
            if (children.getFirstChild().getChildDOB().after(new Date())) {
                addError = true;
            } else {
                Iterator<AdditionalChild> childIterator = children.getAdditionalChildren().iterator();
                while (childIterator.hasNext()) {
                    if (childIterator.next().getChild().getChildDOB().after(new Date())) {
                        addError = true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("exception mapping " + e.toString());
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
