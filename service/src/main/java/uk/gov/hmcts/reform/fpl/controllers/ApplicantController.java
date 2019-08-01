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
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicantMigrationService;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.updatePBANumber;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.validatePBANumber;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    @Autowired
    private final ApplicantMigrationService applicantMigrationService;
    private final ObjectMapper mapper;

    @Autowired
    public ApplicantController(ApplicantMigrationService applicantMigrationService,
                               ObjectMapper mapper) {
        this.applicantMigrationService = applicantMigrationService;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("applicantsMigrated", applicantMigrationService.setMigratedValue(caseData));
        caseDetails.getData().put("applicants", applicantMigrationService.expandApplicantCollection(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        ImmutableList.Builder<String> validationErrors = ImmutableList.builder();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getApplicants() != null) {
            List<Element<Applicant>> applicants = caseData.getApplicants().stream()
                .map(element -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (element.getValue().getParty().getPbaNumber() != null) {
                        String pba = updatePBANumber(element.getValue().getParty().getPbaNumber());
                        validationErrors.addAll(validatePBANumber(pba));

                        if (validationErrors.build().isEmpty()) {
                            applicantBuilder.party(element.getValue().getParty().toBuilder().pbaNumber(pba).build());
                        }
                    }

                    return Element.<Applicant>builder()
                        .id(element.getId())
                        .value(applicantBuilder.build())
                        .build();
                })
                .collect(Collectors.toList());

            caseDetails.getData().put("applicants", applicants);
        } else {
            OldApplicant applicantData = caseData.getApplicant();

            if (isNullOrEmpty(applicantData.getPbaNumber())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDetails.getData())
                    .errors(validationErrors.build())
                    .build();
            }

            String newPbaNumberData = PBANumberHelper.updatePBANumber(applicantData.getPbaNumber());
            validationErrors.addAll(validatePBANumber(newPbaNumberData));

            if (validationErrors.build().isEmpty()) {
                applicantData.setPbaNumber(newPbaNumberData);
                caseDetails.getData().put("applicant", applicantData);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors.build())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        return applicantMigrationService.addHiddenValues(caseDetails);
    }
}
