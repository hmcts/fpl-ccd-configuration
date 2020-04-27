package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType.CHILD_WHEREABOUTS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.HOSPITAL_SOON_TO_BE_DISCHARGED;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.REMOVED_BY_POLICE_POWER_ENDS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.VOLUNTARILY_SECTION_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionTemplateDataGenerationService.class, JacksonAutoConfiguration.class})
public class CaseSubmissionTemplateDataGenerationServiceTest {
    private static final LocalDate NOW = now();

    private static final String FORMATTED_DATE = formatLocalDateToString(NOW, DATE);

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

        @Test
        void shouldReturnDirectionsNeededWithAppendedDirectionsAndDirectionDetailsWhenGiven()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .directions("directions")
                    .directionDetails("direction  details")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedDirectionsNeeded = "Contact with any named person\n"
                + "directions\n"
                + "direction  details";
            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo(expectedDirectionsNeeded);
        }
    }

    @Nested
    class DocmosisCaseSubmissionLivingSituationTest {
        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyForLivingSituationWhenChildLivingSituationIsEmptyOrNull(final String livingSituation)
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(livingSituation)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getChildren()).hasSize(1);
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo("-");
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsInHospitalSoonToBeDischarged()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(HOSPITAL_SOON_TO_BE_DISCHARGED.getValue())
                        .dischargeDate(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "In hospital and soon to be discharged\nDischarge date: " + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsRemovedByPolicePowerEnds()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(REMOVED_BY_POLICE_POWER_ENDS.getValue())
                        .datePowersEnd(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Removed by Police, powers ending soon\nDate powers end: "
                + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsVoluntarySectionCareOrder()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(VOLUNTARILY_SECTION_CARE_ORDER.getValue())
                        .careStartDate(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Voluntarily in section 20 care order\nDate this began: " + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }
    }

    @Nested
    class DocmosisCaseSubmissionBuildRespondentTest {
        @Test
        void shouldNotReturnRespondentConfidentialDetailsWhenContactDetailsHidden()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .respondents1(wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("First Name")
                        .lastName("Last Name")
                        .relationshipToChild("Father")
                        .address(Address.builder()
                            .postcode("SL11GF")
                            .build())
                        .contactDetailsHidden("Yes")
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("080-90909090")
                            .build())
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRespondents()).hasSize(1);
            assertThat(caseSubmission.getRespondents().get(0).getAddress()).isEqualTo("Confidential");
            assertThat(caseSubmission.getRespondents().get(0).getTelephoneNumber()).isEqualTo("Confidential");
        }

        @Test
        void shouldReturnRespondentAddressAndTelephoneDetailsWhenContactDetailsHiddenIsNotSet()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .respondents1(wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("First Name")
                        .lastName("Last Name")
                        .relationshipToChild("Father")
                        .address(Address.builder()
                            .addressLine1("Flat 13")
                            .postcode("SL11GF")
                            .build())
                        .contactDetailsHidden("No")
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("080-90909090")
                            .build())
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRespondents()).hasSize(1);
            assertThat(caseSubmission.getRespondents().get(0).getAddress()).isEqualTo("Flat 13\nSL11GF");
            assertThat(caseSubmission.getRespondents().get(0).getTelephoneNumber()).isEqualTo("080-90909090");
        }
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(now());

        return caseData;
    }
}
