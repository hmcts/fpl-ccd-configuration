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
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;
import java.time.LocalDate;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType.CHILD_WHEREABOUTS;
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
    class DocmosisCaseSubmissionOrdersNeededTest {
        @Test
        void shouldReturnOrdersNeededWithOtherOrderAppendedWhenOtherOrderGiven() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .otherOrder("expected other order")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\nexpected other order";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }

        @Test
        void shouldReturnOrdersNeededWithOtherOrderAppendedWhenOtherOrderAndWithOrderTypesGiven() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .otherOrder("expected other order")
                    .orderType(of(OrderType.values()))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Care order\n"
                + "Interim care order\n"
                + "Supervision order\n"
                + "Interim supervision order\n"
                + "Education supervision order\n"
                + "Emergency protection order\n"
                + "Variation or discharge of care or supervision order\n"
                + "expected other order";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }

        @Test
        void shouldReturnOrdersNeededWithAppendedEmergencyProtectionOrdersTypesWhenEmergencyProtectionOrdersTypesGiven()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrders(of(EmergencyProtectionOrdersType.values()))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\n"
                + "Information on the whereabouts of the child\n"
                + "Authorisation for entry of premises\n"
                + "Authorisation to search for another child on the premises\n"
                + "Other order under section 48 of the Children Act 1989";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }

        @Test
        void shouldReturnOrdersNeededAppendedEmergencyProtectionOrderDetailsWhenEmergencyProtectionOrderDetailsGiven()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrders(of(CHILD_WHEREABOUTS))
                    .emergencyProtectionOrderDetails("emergency protection order details")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\n"
                + "Information on the whereabouts of the child\n"
                + "emergency protection order details";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }
    }

    @Nested
    class DocmosisCaseSubmissionDirectionsNeededTest {
        @Test
        void shouldReturnDirectionsNeededWithAppendedEmergencyProtectionOrderDirectionDetails() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrderDirectionDetails("direction details")
                    .emergencyProtectionOrderDirections(of(EmergencyProtectionOrderDirectionsType.values()))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedDirectionsNeeded = "Contact with any named person\n"
                + "A medical or psychiatric examination, or another assessment of the child\n"
                + "To be accompanied by a registered medical practitioner, nurse or midwife\n"
                + "An exclusion requirement\n"
                + "Other direction relating to an emergency protection order\n"
                + "direction details\n"
                + "Yes";
            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo(expectedDirectionsNeeded);
        }
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(LocalDate.now());

        return caseData;
    }
}
