package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.boot.dependencies.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.fpl.service.UpdateAndValidatePbaService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.UUID;

import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    private final ApplicantService applicantService;
    private final UpdateAndValidatePbaService updateAndValidatePbaService;
    private final ObjectMapper mapper;
    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationApi organisationApi;

    @Autowired
    public ApplicantController(ApplicantService applicantService,
                               UpdateAndValidatePbaService updateAndValidatePbaService,
                               ObjectMapper mapper,
                               OrganisationApi organisationApi,
                               AuthTokenGenerator authTokenGenerator) {
        this.applicantService = applicantService;
        this.updateAndValidatePbaService = updateAndValidatePbaService;
        this.mapper = mapper;
        this.organisationApi = organisationApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest,
        @RequestHeader(value = "authorization") String authorisation) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Organisation organisation = organisationApi.findOrganisationById(authorisation, authTokenGenerator.generate());

        if(isNotEmpty(organisation)){
            caseData = buildCaseDataWithOrganisationDetails(caseData, organisation);
        }

        caseDetails.getData().put("applicants", applicantService.expandApplicantCollection(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private CaseData buildCaseDataWithOrganisationDetails(CaseData caseData, Organisation organisation){
        return caseData.toBuilder().applicants(ImmutableList.of(Element.<Applicant>builder()
            .value(Applicant.builder()
                .party(ApplicantParty.builder()
                    // A value within applicant party needs to be set in order to expand UI view.
                    .partyId(UUID.randomUUID().toString())
                    .organisationName(organisation.getName())
                    .build())
                .build())
            .build())).build();
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
