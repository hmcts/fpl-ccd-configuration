package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionTemplateDataGenerationService.class, JacksonAutoConfiguration.class})
public class CaseSubmissionTemplateDataGenerationServiceTest {
    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseSubmissionTemplateDataGenerationService templateDataGenerationService;

    @BeforeEach
    void init() {
        given(userDetailsService.getUserName()).willReturn("Professor");
    }

    @Test
    void shouldReturnExpectedTemplateDataWhenAllDataPresent() throws IOException {
        Map<String, Object> returnedTemplateData = templateDataGenerationService.getTemplateData(
            prepareCaseData(), false);
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(LocalDate.now());

        return caseData;
    }
}
