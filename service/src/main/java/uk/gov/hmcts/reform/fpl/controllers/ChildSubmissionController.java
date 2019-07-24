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
import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.ChildrenMigrationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

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

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return childrenMigrationService.addHiddenValues(caseDetails);
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        if (!isEmpty(caseData.getChildren1())) {
            List<Child> newChildren = caseData.getChildren1().stream()
                .map(Element::getValue)
                .collect(toList());

            if (newChildren.stream()
                .map(Child::getParty)
                .map(Party::getDateOfBirth)
                .filter(Objects::nonNull)
                .anyMatch(dateOfBirth -> dateOfBirth.after(new Date()))) {
                errors.add("Date of birth cannot be in the future");
            }
        } else if (!isEmpty(caseData.getChildren())) {
            if (caseData.getChildren().getAllChildren().stream()
                .map(OldChild::getChildDOB)
                .filter(Objects::nonNull)
                .anyMatch(dateOfBirth -> dateOfBirth.after(new Date()))) {
                errors.add("Date of birth cannot be in the future");
            }
        }
        return errors.build();
    }
}
