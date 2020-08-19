package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseConverter.class})
class CaseConverterTest {

    @Autowired
    private CaseConverter caseConverter;

    @Test
    void shouldConvertCaseDataToMap() {
        assertThat(caseConverter.convertToMap(getCaseData())).containsKey("caseLocalAuthority");
        assertThat(caseConverter.convertToMap(getCaseData())).containsKey("hearingDetails");
    }

    @Test
    void shouldConvertCaseDetailsToCaseData() {
        String localAuthority = "LA1";
        CallbackRequest request = callbackRequest(Map.of("localAuthority", localAuthority));

        assertThat(caseConverter.convertToCaseData(request.getCaseDetails()).getCaseLocalAuthority()).isEqualTo(localAuthority);
    }

    private CaseData getCaseData() {
        return CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .dateSubmitted(LocalDate.now())
            .dateOfIssue(LocalDate.now())
            .build();
    }
}
