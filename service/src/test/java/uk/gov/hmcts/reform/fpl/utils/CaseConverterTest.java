package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseConverter.class})
class CaseConverterTest {

    @Autowired
    private CaseConverter caseConverter;

    @MockBean
    private ObjectMapper mapper;

    @Test
    void shouldConvertCaseDataToMap() {
        caseConverter.convertToMap(getCaseData());
        verify(mapper).convertValue(eq(getCaseData()), eq(Map.class));
    }

    @Test
    void shouldConvertCaseDetailsToCaseData() {
        caseConverter.convertToCaseData(getCaseDetails());
        verify(mapper).convertValue(eq(getCaseDetails().getData()), eq(CaseData.class));
    }

    private CaseDetails getCaseDetails() {
        Map<String, Object> data = mapper.convertValue(getCaseData(), new TypeReference<>() {});
        return CaseDetails.builder().data(data).build();
    }

    private CaseData getCaseData() {
        return CaseData.builder().build();
    }
}
