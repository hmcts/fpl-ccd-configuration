package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.FeignException.NotFound;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.fpl.service.UpdateAndValidatePbaService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    private final ApplicantService applicantService;
    private final UpdateAndValidatePbaService updateAndValidatePbaService;
    private final ObjectMapper mapper;
    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationApi organisationApi;
    private final RequestData requestData;

    @Autowired
    public ApplicantController(ApplicantService applicantService,
                               UpdateAndValidatePbaService updateAndValidatePbaService,
                               ObjectMapper mapper,
                               OrganisationApi organisationApi,
                               AuthTokenGenerator authTokenGenerator,
                               RequestData requestData) {
        this.applicantService = applicantService;
        this.updateAndValidatePbaService = updateAndValidatePbaService;
        this.mapper = mapper;
        this.organisationApi = organisationApi;
        this.authTokenGenerator = authTokenGenerator;
        this.requestData = requestData;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Organisation organisation = Organisation.builder().build();

        try {
            organisation = organisationApi.findOrganisationById(requestData.authorisation(),
                authTokenGenerator.generate());
        } catch (FeignException ex) {
            log.error("Could not find the associated organisation from reference data", ex);
        }

        caseDetails.getData().put("applicants", applicantService.expandApplicantCollection(caseData, organisation));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return updateAndValidatePbaService.updateAndValidatePbaNumbers(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("applicants", applicantService.addHiddenValues(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
