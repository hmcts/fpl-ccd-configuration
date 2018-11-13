package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Document;

import java.util.Map;

@Service
public class CaseRepository {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "Shared_Storage_DRAFTType";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

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
            JURISDICTION, CASE_TYPE, caseId, false, body);

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

}
