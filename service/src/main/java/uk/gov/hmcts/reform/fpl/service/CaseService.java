package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaseService {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public CaseService(CoreCaseDataApi coreCaseDataApi,
                                     AuthTokenGenerator authTokenGenerator) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public void deleteCase(String authorization, String creatorUserId, String caseId) {

        StartEventResponse ccdStartEventResponse = null;
        try {
            System.out.println("START: Calling CCD: Start Event for Caseworker");
            ccdStartEventResponse = coreCaseDataApi.startEventForCaseWorker(authorization,
                authTokenGenerator.generate(), creatorUserId, JURISDICTION, CASE_TYPE, caseId, "anevent");
        } catch (Exception ex) {
            logger.warn("Could not start event for user {} to case {}", creatorUserId, caseId, ex);
        }

        System.out.println("END: Calling CCD: Submit Event for Caseworker");

        // remove all data
        CaseDataContent caseDataContentToSubmit = CaseDataContent.builder().build();
        System.out.println("Setting case data to be empty");
        Map<String, Object> emptyMap = new HashMap<String, Object>();
        // must have at least one field
        emptyMap.put("", "");
        caseDataContentToSubmit.setData(emptyMap);

        // submit the empty one
        System.out.println("START: Calling CCD: Submit Event for Caseworker");
        CaseDetails ccdSubmissionResponse = coreCaseDataApi.submitEventForCaseWorker(authorization,
            authTokenGenerator.generate(),
            creatorUserId,
            JURISDICTION, CASE_TYPE, caseId, true, caseDataContentToSubmit);
        System.out.println("END: Calling CCD: Submit Event for Caseworker");
    }

}
