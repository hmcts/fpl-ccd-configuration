package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.exception.MalformedTemplateException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@RunWith(SpringRunner.class)
public class DocumentGeneratorServiceTest {

    private DocumentGeneratorService documentGeneratorService = new DocumentGeneratorService(
        new DocumentTemplates(), new ObjectMapper()
    );

    @Test
    public void testEmptyCaseDetailsSuccessfullyReturnsByteArray() throws IOException {
        CaseDetails caseDetails = emptyCaseDetails();

        assertThat("Byte array is still returned if the caseDetails is empty",
            byte[].class.isInstance(documentGeneratorService.generateSubmittedFormPDF(caseDetails)));
    }

    @Test
    public void testPopulatedCaseDetailsSuccessfullyReturnsByteArray() throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        assertThat("Byte array is returned on populated submission",
            byte[].class.isInstance(documentGeneratorService.generateSubmittedFormPDF(caseDetails)));
    }

    @Test(expected = MalformedTemplateException.class)
    public void testNullCaseDetailsProvidesMalformedTemplate() {
        documentGeneratorService.generateSubmittedFormPDF(null);
    }
}
