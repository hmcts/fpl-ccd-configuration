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

    public void deleteCase(String authorization, String creatorUserId, String caseId, String eventId) {
        StartEventResponse ccdStartEventResponse = null;
        try {
            ccdStartEventResponse = coreCaseDataApi.startEventForCaseWorker(authorization,
                authTokenGenerator.generate(), creatorUserId, JURISDICTION, CASE_TYPE, caseId, eventId);
        } catch (Exception ex) {
            logger.warn("Could not start event for user {} to case {}", creatorUserId, caseId, ex);
        }

        // remove all data
        CaseDataContent caseDataContentToSubmit = CaseDataContent.builder().build();
        caseDataContentToSubmit.setData(new Object());

        // submit the empty one
        CaseDetails ccdSubmissionResponse = coreCaseDataApi.submitEventForCaseWorker(authorization,
            authTokenGenerator.generate(),
            creatorUserId,
            JURISDICTION, CASE_TYPE, caseId, true, caseDataContentToSubmit);
    }
}
