package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.model.OldChild;
import uk.gov.hmcts.reform.fpl.model.OldChildren;
import uk.gov.hmcts.reform.fpl.service.ChildrenMigrationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api
@RestController
@RequestMapping("/callback/enter-children")
public class ChildSubmissionController {

    private final MapperService mapperService;
    private final ChildrenMigrationService childrenMigrationService;

    @Autowired
    public ChildSubmissionController(MapperService mapperService,
                                     ChildrenMigrationService childrenMigrationService) {
        this.mapperService = mapperService;
        this.childrenMigrationService = childrenMigrationService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return childrenMigrationService.setMigratedValue(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validate(caseDetails))
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        Map<String, Object> childrenData = (Map<String, Object>) caseDetails.getData().get("children");
        OldChildren oldChildren = mapperService.mapObject(childrenData, OldChildren.class);
        if (oldChildren.getAllChildren().stream()
            .map(OldChild::getChildDOB)
            .filter(Objects::nonNull)
            .anyMatch(dateOfBirth -> dateOfBirth.after(new Date()))) {
            errors.add("Date of birth cannot be in the future");
        }
        return errors.build();
    }
}
