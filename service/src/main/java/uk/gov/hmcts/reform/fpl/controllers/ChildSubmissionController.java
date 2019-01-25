package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Children;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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

        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        List<String> errorsList = new ArrayList<>();

        try {
            Children children = mapperService.mapObject((Map<String, Object>) caseDetails.getData().get("children"), Children.class);
            if (children.getAllChildren().stream().anyMatch(child ->
                child.getChildDOB().after(new Date())
            )) {
                errorsList.add(DOB_IN_FUTURE_ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("exception mapping children data " + e.toString());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(Collections.unmodifiableList(errorsList))
            .build();
    }

}
