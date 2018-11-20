package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.CallbackEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static uk.gov.hmcts.reform.fpl.handlers.SubmittedCaseEventHandler.SubmittedFormFilenameHelper.buildFileName;

/**
 * Handler of case submission event.
 */
@Component
public class SubmittedCaseEventHandler {

    private final DocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseRepository caseRepository;

    @Autowired
    public SubmittedCaseEventHandler(DocumentGeneratorService documentGeneratorService,
                                     UploadDocumentService uploadDocumentService,
                                     CaseRepository caseRepository) {
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.caseRepository = caseRepository;
    }

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

        Document document = uploadDocumentService.uploadPDF(userId, authorization, pdf, buildFileName(caseDetails));

        caseRepository.setSubmittedFormPDF(authorization, userId, caseDetails.getId().toString(), document);
    }

    static class SubmittedFormFilenameHelper {

        private SubmittedFormFilenameHelper() {
            // NO-OP
        }

        static String buildFileName(CaseDetails caseDetails) {
            String caseName = Strings.nullToEmpty((String) caseDetails.getData().get("caseName")).trim();

            if (!Strings.isNullOrEmpty(caseName)) {
                return caseName.replaceAll("\\s", "_") + ".pdf";
            }

            return Long.toString(caseDetails.getId()) + ".pdf";
        }
    }

}
