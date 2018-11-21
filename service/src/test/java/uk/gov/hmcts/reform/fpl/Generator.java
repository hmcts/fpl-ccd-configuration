package uk.gov.hmcts.reform.fpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;

import java.io.FileOutputStream;
import java.io.IOException;

import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

public class Generator {

    public static void main(String[] args) throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        DocumentGeneratorService documentGeneratorService = new DocumentGeneratorService(
            new DocumentTemplates(), new ObjectMapper()
        );
        byte[] bytes = documentGeneratorService.generateSubmittedFormPDF(caseDetails);

        IOUtils.write(bytes, new FileOutputStream("/Users/jamesne/Documents/form.pdf"));
    }

}
