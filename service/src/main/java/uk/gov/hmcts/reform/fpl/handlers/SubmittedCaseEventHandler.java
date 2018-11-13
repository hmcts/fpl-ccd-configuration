package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

/**
 * Handler of case submission event.
 */
@Component
public class SubmittedCaseEventHandler {

    @Autowired
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private UploadDocumentService uploadDocumentService;
    @Autowired
    private CaseRepository caseRepository;

    /**
     * Generates PDF, uploads it into document store and updates case with reference to the document.
     *
     * @param event case submitted event.
     */
    @Async
    @EventListener
    public void handleCaseSubmission(SubmittedCaseEvent event) {
        String userId = event.getUserId();
        String authorization = event.getAuthorization();
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        byte[] pdf = documentGeneratorService.generateSubmittedFormPDF(caseDetails);

        Document document = uploadDocumentService.upload(userId, authorization, pdf, buildFileName(caseDetails));

        caseRepository.setSubmittedFormPDF(authorization, userId, caseDetails.getId().toString(), document);
    }

    private String buildFileName(CaseDetails caseDetails) {
        try {
            String title = Strings.nullToEmpty(caseDetails.getData().get("caseName").toString().trim());
            return title.replaceAll("\\s", "_") + ".pdf";
        } catch (NullPointerException e) {
            return caseDetails.getId().toString() + ".pdf";
        }
    }

}
