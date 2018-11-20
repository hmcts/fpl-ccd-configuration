package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Document;

import java.util.Map;

@Service
public class CaseRepository {

    static final String JURISDICTION = "PUBLICLAW";
    static final String CASE_TYPE = "Shared_Storage_DRAFTType";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    public CaseRepository(AuthTokenGenerator authTokenGenerator, CoreCaseDataApi coreCaseDataApi) {
        this.authTokenGenerator = authTokenGenerator;
        this.coreCaseDataApi = coreCaseDataApi;
    }

    public void setSubmittedFormPDF(String authorization, String userId, String caseId, Document document) {
        String event = "attachSubmittedFormPDF";

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(authorization,
            authTokenGenerator.generate(), userId, JURISDICTION, CASE_TYPE, caseId, event);

        logger.debug("Event {} on case {} started with token {}", event, caseId, startEventResponse.getToken());

        CaseDataContent body = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("Attach submitted form PDF")
                .description("Attach submitted form PDF")
                .build())
            .data(prepareData(document))
            .build();

        coreCaseDataApi.submitEventForCaseWorker(authorization, authTokenGenerator.generate(), userId,
            JURISDICTION, CASE_TYPE, caseId, true, body);

        logger.debug("Event {} on case {} completed", event, caseId);
    }

    private Map<String, Object> prepareData(Document document) {
        return ImmutableMap.<String, Object>builder()
            .put("submittedForm", ImmutableMap.builder()
                .put("document_url", document.links.self.href)
                .put("document_binary_url", document.links.binary.href)
                .put("document_filename", document.originalDocumentName)
                .build())
            .build();
    }

    /**
     * Adds caseLocalAuthority to CCD case data.
     *
     * @param authorization the authorization from the CCD callback.
     * @param userId the userId from the CCD callback.
     * @param caseId the current ID of the case.
     * @param caseLocalAuthority the value to be added to CCD case details.
     */
    public void setCaseLocalAuthority(
        String authorization,
        String userId,
        String caseId,
        String caseLocalAuthority) {

        String event = "addLocalAuthority";

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(authorization,
            authTokenGenerator.generate(), userId, JURISDICTION, CASE_TYPE, caseId, event);

        logger.debug("Event {} on case {} started with token {}", event, caseId, startEventResponse.getToken());

        CaseDataContent body = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("Add Local Authority")
                .description("Add Local Authority")
                .build())
            .data(prepareLocalAuthority(caseLocalAuthority))
            .build();

        coreCaseDataApi.submitEventForCaseWorker(authorization, authTokenGenerator.generate(), userId,
            JURISDICTION, CASE_TYPE, caseId, true, body);

        logger.debug("Event {} on case {} completed", event, caseId);
    }

    private Map<String, Object> prepareLocalAuthority(String caseLocalAuthority) {
        return ImmutableMap.<String, Object>builder()
            .put("caseLocalAuthority", caseLocalAuthority)
            .build();
    }
}
