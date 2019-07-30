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
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicantMigrationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.validatePBANumber;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    @Autowired
    private MapperService mapperService;
    private final ApplicantMigrationService applicantMigrationService;
    private final ObjectMapper mapper;

    @Autowired
    public ApplicantController(MapperService mapperService,
                               ApplicantMigrationService applicantMigrationService, ObjectMapper mapper) {
        this.mapperService = mapperService;
        this.applicantMigrationService = applicantMigrationService;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseData alteredData = CaseData.builder()
            .applicantsMigrated(applicantMigrationService.setMigratedValue(caseData))
            .applicants(applicantMigrationService.expandApplicantCollection(caseData))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(mapper.convertValue(alteredData, Map.class))
            .build();
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        ImmutableList.Builder<String> validationErrors = ImmutableList.builder();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getApplicants() != null) {
            List<Element<Applicant>> migratedApplicantsObject = caseData.getApplicants();

            List<Applicant> migratedApplicant = migratedApplicantsObject
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            migratedApplicant.stream()
                .map(Applicant::getParty)
                .map(ApplicantParty::getPbaNumber)
                .filter(Objects::nonNull)
                .forEach(pbaNumber -> {
                    String formattedPbaNumber =
                        PBANumberHelper.updatePBANumber(pbaNumber);
                    validationErrors.addAll(validatePBANumber(formattedPbaNumber));
                });

            CaseData.builder().applicants(migratedApplicantsObject).build();

        } else {
            Map<String, Object> applicantData = (Map<String, Object>)
                caseDetails.getData().get("applicant");
            

            OldApplicant applicant = mapperService.mapObject(applicantData, OldApplicant.class);

            if (isNullOrEmpty(applicant.getPbaNumber())) {
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

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        return applicantMigrationService.addHiddenValues(caseDetails);
    }
}
