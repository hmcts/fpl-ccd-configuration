package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

@Service
public class CaseService {

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

    }
}
