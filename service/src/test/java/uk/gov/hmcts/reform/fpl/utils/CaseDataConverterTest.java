package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildCaseDataForCMODocmosisGeneration;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseDataConverter.class})
class CaseDataConverterTest {

    @Autowired
    private CaseDataConverter caseDataConverter;

    @Test
    void shouldConvertCaseDataToMap() {
        assertThat(caseDataConverter.convertToMap(getCaseData())).containsKey("caseLocalAuthority");
        assertThat(caseDataConverter.convertToMap(getCaseData())).containsKey("hearingDetails");
    }

    @Nested
    class ConvertToCaseData {
        String localAuthority;
        CallbackRequest request;

        @BeforeEach
        void setup() {
            localAuthority = "LA1";
            request = callbackRequest(Map.of("localAuthority", localAuthority));
        }

        @Test
        void shouldConvertCallbackRequestToCaseData() {
            assertThat(caseDataConverter.convertToCaseData(request).getCaseLocalAuthority()).isEqualTo(localAuthority);
            assertThat(caseDataConverter.convertToCaseData(request)).isInstanceOf(CaseData.class);
        }

        @Test
        void shouldConvertCaseDetailsToCaseData() {
            final CaseDetails caseDetails = request.getCaseDetails();

            assertThat(caseDataConverter.convertToCaseData(request).getCaseLocalAuthority()).isEqualTo(localAuthority);
            assertThat(caseDataConverter.convertToCaseData(caseDetails)).isInstanceOf(CaseData.class);
        }

    }

    private CaseData getCaseData() {
        LocalDateTime dateTime = LocalDateTime.of(2099, 1, 1, 10, 0, 0);
        return buildCaseDataForCMODocmosisGeneration(dateTime);
    }
}
