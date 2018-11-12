package uk.gov.hmcts.reform.fpl.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.populatedCaseDetails;

@RunWith(SpringRunner.class)
public class CaseServiceTest {

    private Method getFileName;
    private CaseService caseService = new CaseService();

    @Before
    public void setup() throws NoSuchMethodException {
        getFileName = caseService.getClass().getDeclaredMethod("getFileName", CaseDetails.class);
        getFileName.setAccessible(true);
    }

    @Test
    public void testGetFileNameReturnsCaseReferenceWhenNoTitleIsProvided()
        throws IOException, InvocationTargetException, IllegalAccessException {
        CaseDetails caseDetails = emptyCaseDetails();
        String fileName = (String) getFileName.invoke(caseService, caseDetails);

        assertThat("File name should match the caseID of 123", fileName, is("123.pdf"));
    }

    @Test
    public void testGetFileNameReturnsCaseTitleWhenProvided()
        throws IOException, InvocationTargetException, IllegalAccessException {
        CaseDetails caseDetails = populatedCaseDetails();
        String fileName = (String) getFileName.invoke(caseService, caseDetails);

        assertThat("File name should match the file name of test", fileName, is("test.pdf"));
    }
}
