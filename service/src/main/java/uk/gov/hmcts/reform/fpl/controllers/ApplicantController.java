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
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.PartyApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.ApplicantMigrationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.validatePBANumber;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    @Autowired
    private MapperService mapperService;
    private final ApplicantMigrationService applicantMigrationService;

    @Autowired
    public ApplicantController(MapperService mapperService,
                               ApplicantMigrationService applicantMigrationService) {
        this.mapperService = mapperService;
        this.applicantMigrationService = applicantMigrationService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return applicantMigrationService.setMigratedValue(caseDetails);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        ImmutableList.Builder<String> validationErrors = ImmutableList.builder();

        Map<String, Object> applicantData = (Map<String, Object>)
            defaultIfNull(caseDetails.getData().get("applicant"), null);

        if (caseDetails.getData().containsKey("applicants")) {
            List<Map<String, Object>> migratedApplicantsObject =
                (List<Map<String, Object>>) caseDetails.getData().get("applicants");

            migratedApplicantsObject.stream()
                .map(applicant ->
                    mapperService.mapObject(applicant, Applicant.class))
                .map(Applicant::getParty)
                .map(PartyApplicant::getPbaNumber)
                .filter(String::isBlank)
                .forEach(pbaNumber -> {
                    System.out.println("pbaNumber = " + pbaNumber);
                    String newPbaNumberData = PBANumberHelper.updatePBANumber(pbaNumber);
                    validationErrors.addAll(validatePBANumber(newPbaNumberData));
                });

            caseDetails.getData().put("applicants", migratedApplicantsObject);

        } else {
            OldApplicant applicant = mapperService.mapObject(applicantData, OldApplicant.class);

            if (applicant.getPbaNumber() == null || applicant.getPbaNumber().isBlank()) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDetails.getData())
                    .errors(validationErrors.build())
                    .build();
            }

            String newPbaNumberData = PBANumberHelper.updatePBANumber(applicant.getPbaNumber());
            validationErrors.addAll(validatePBANumber(newPbaNumberData));

            if (validationErrors.build().isEmpty()) {
                applicantData.put("pbaNumber", newPbaNumberData);
                caseDetails.getData().put("applicant", applicantData);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors.build())
            .build();
    }
}
