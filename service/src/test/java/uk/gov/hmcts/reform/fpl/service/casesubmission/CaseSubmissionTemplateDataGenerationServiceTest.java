package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
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

    private CaseData givenCaseData;

    @BeforeEach
    void init() {
        givenCaseData = prepareCaseData();
        given(userDetailsService.getUserName()).willReturn("Professor");
    }

    @Test
    void shouldReturnExpectedTemplateDataWithCourtSealWhenAllDataPresent() throws IOException {
        DocmosisCaseSubmission returnedCaseSubmission = templateDataGenerationService.getTemplateData(givenCaseData);
        assertThat(returnedCaseSubmission).isEqualToComparingFieldByField(expectedDocmosisCaseSubmission());
    }

    @Nested
    class OrdersNeededTest {
        @Test
        void shouldReturnOrdersNeededWithOtherOrderAppendedWhenOtherOrderGiven() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .otherOrder("expected other order")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\nexpected other order\n";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }
    }


    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(LocalDate.now());

        return caseData;
    }
}
