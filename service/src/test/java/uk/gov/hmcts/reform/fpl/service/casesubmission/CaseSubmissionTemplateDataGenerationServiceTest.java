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
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicant;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingPreferences;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRisks;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType.CHILD_WHEREABOUTS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.HOSPITAL_SOON_TO_BE_DISCHARGED;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.REMOVED_BY_POLICE_POWER_ENDS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.VOLUNTARILY_SECTION_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.DONT_KNOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionTemplateDataGenerationService.class, JacksonAutoConfiguration.class,
    LookupTestConfig.class, FixedTimeConfiguration.class})
public class CaseSubmissionTemplateDataGenerationServiceTest {
    private static final LocalDate NOW = now();

    private static final String FORMATTED_DATE = formatLocalDateToString(NOW, DATE);

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private HmctsCourtLookupConfiguration courtLookupConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseSubmissionTemplateDataGenerationService templateDataGenerationService;

    private CaseData givenCaseData;

    @BeforeEach
    void init() {
        givenCaseData = prepareCaseData();
        given(userDetailsService.getUserName()).willReturn("Professor");
        given(courtLookupConfiguration.getCourt("example"))
            .willReturn(new HmctsCourtLookupConfiguration.Court("Family Court", "admin@family-court.com",
                "11"));
    }

    @Test
    void shouldReturnExpectedTemplateDataWithCourtSealWhenAllDataPresent() {
        DocmosisCaseSubmission returnedCaseSubmission = templateDataGenerationService.getTemplateData(givenCaseData);
        assertThat(returnedCaseSubmission).isEqualToComparingFieldByField(expectedDocmosisCaseSubmission());
    }

    @Test
    void shouldReturnExpectedCaseNumberInDocmosisCaseSubmissionWhenCaseNumberGiven() {
        String expectedCaseNumber = "12345";
        DocmosisCaseSubmission returnedCaseSubmission = templateDataGenerationService.getTemplateData(givenCaseData);
        DocmosisCaseSubmission returnedCaseSubmissionWithCaseNumber =
            templateDataGenerationService.populateDocmosisCaseSubmissionWithCaseNumber(returnedCaseSubmission, 12345L);

        assertThat(returnedCaseSubmissionWithCaseNumber.getCaseNumber()).isEqualTo(expectedCaseNumber);
    }

    @Nested
    class DocmosisCaseSubmissionOrdersNeededTest {
        @Test
        void shouldReturnDefaultValueForOrdersNeededWhenOrderTypeEmpty() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of())
                    .otherOrder("expected other order")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo("-");
        }

        @Test
        void shouldReturnDefaultValueForOrdersNeededWhenOrderIsNull() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(Orders.builder().build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo("-");
        }

        @Test
        void shouldReturnOrdersNeededWithOtherOrderAppendedWhenOtherOrderGiven() {
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
        void shouldReturnOrdersNeededWithOtherOrderAppendedWhenOtherOrderAndWithOrderTypesGiven() {
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
        void shouldHaveOrdersNeededWithAppendedEmergencyProtectionOrdersTypesWhenEmergencyProtectionOrdersTypesGiven() {
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
        void shouldReturnOrdersNeededAppendedEmergencyProtectionOrderDetailsWhenEmergencyProtectionOrderDetailsGiven() {
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
    class DocmosisCaseSubmissionGroundsForEPOReasonTest {

        @Test
        void shouldReturnEmptyWhenOrderTypesAreEmpty() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("");
        }

        @Test
        void shouldReturnEmptyWhenEPOIsNotInOrderType() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EDUCATION_SUPERVISION_ORDER))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("");
        }

        @Test
        void shouldReturnDefaultValueWhenOrderTypeEPOAndGroundsForEPOIsNull() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EMERGENCY_PROTECTION_ORDER))
                    .build())
                .groundsForEPO(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("-");
        }

        @Test
        void shouldReturnDefaultValueWhenOrderTypeEPOAndGroundsForEPOReasonIsEmpty() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EMERGENCY_PROTECTION_ORDER))
                    .build())
                .groundsForEPO(GroundsForEPO.builder()
                    .reason(of())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("-");
        }

        @Test
        void shouldReturnGroundsForEPOReasonWhenOrderTypeEPOAndGroundsForEPOReasonIsNotEmpty() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EMERGENCY_PROTECTION_ORDER))
                    .build())
                .groundsForEPO(GroundsForEPO.builder()
                    .reason(of("HARM_IF_KEPT_IN_CURRENT_ACCOMMODATION",
                        "URGENT_ACCESS_TO_CHILD_IS_OBSTRUCTED"))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason())
                .isEqualTo("There’s reasonable cause to believe the child is likely to suffer significant "
                    + "harm if they don’t stay in their current accommodation\n\nYou’re making enquiries and "
                    + "need urgent access to the child to find out about their welfare, and access is being "
                    + "unreasonably refused");
        }
    }

    @Nested
    class DocmosisCaseSubmissionGroundsThresholdDetailsTest {

        @Test
        void shouldReturnBeyondParentalControlForGroundsThresholdReasonWhenThresholdReasonIsBeyondControl() {
            CaseData updatedCasData = givenCaseData.toBuilder()
                .grounds(Grounds.builder()
                    .thresholdReason(of("beyondControl"))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCasData);

            assertThat(caseSubmission.getGroundsThresholdReason()).isEqualTo("Beyond parental control.");
        }

        @Test
        void shouldReturnDefaultValueForGroundsThresholdReasonWhenTGroundsIsNull() {
            CaseData updatedCasData = givenCaseData.toBuilder()
                .grounds(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCasData);

            assertThat(caseSubmission.getGroundsThresholdReason()).isEqualTo("-");
        }
    }

    @Nested
    class DocmosisCaseSubmissionDirectionsNeededTest {

        @Test
        void shouldReturnEmptyWhenOrdersAreNotAvailable() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo("-");
        }

        @Test
        void shouldReturnDefaultValueWhenOrderTypeAndDirectionsIsNull() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(Orders.builder()
                    .orderType(null)
                    .directions(null)
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo("-");
        }

        @Test
        void shouldReturnDirectionsNeededWithAppendedEmergencyProtectionOrderDirectionDetails() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrderDirectionDetails("direction details")
                    .emergencyProtectionOrderDirections(of(EmergencyProtectionOrderDirectionsType.values()))
                    .directions(null)
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedDirectionsNeeded = "Contact with any named person\n"
                + "A medical or psychiatric examination, or another assessment of the child\n"
                + "To be accompanied by a registered medical practitioner, nurse or midwife\n"
                + "An exclusion requirement\n"
                + "Other direction relating to an emergency protection order\n"
                + "direction details";
            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo(expectedDirectionsNeeded);
        }

        @Test
        void shouldReturnDirectionsNeededWithAppendedDirectionsAndDirectionDetailsWhenGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(Orders.builder()
                    .directions("directions")
                    .directionDetails("direction  details")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedDirectionsNeeded = "directions\ndirection  details";
            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo(expectedDirectionsNeeded);
        }
    }

    @Nested
    class DocmosisGetThresholdDetailsTest {

        @Test
        void shouldReturnEmptyWhenGroundsNotAvailable() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .grounds(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getThresholdDetails()).isEqualTo("-");
        }

        @Test
        void shouldReturnEmptyWhenThresholdDetailsNotAvailable() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .grounds(Grounds.builder()
                    .thresholdDetails("")
                    .thresholdReason(of("noCare"))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getThresholdDetails()).isEqualTo("-");
            assertThat(caseSubmission.getGroundsThresholdReason())
                .isEqualTo("Not receiving care that would be reasonably expected from a parent.");
        }
    }

    @Nested
    class DocmosisCaseSubmissionLivingSituationTest {
        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyForLivingSituationWhenChildLivingSituationIsEmptyOrNull(final String livingSituation) {
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
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsInHospitalSoonToBeDischarged() {
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
        void shouldReturnFormattedLivingSituationBasedOnDateWhenSituationIsInHospitalSoonToBeDischarged() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .address(Address.builder()
                            .postcode("SL11GF")
                            .build())
                        .livingSituation(HOSPITAL_SOON_TO_BE_DISCHARGED.getValue())
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "In hospital and soon to be discharged\nSL11GF";
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsRemovedByPolicePowerEnds() {
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
        void shouldReturnFormattedLivingSituationBasedOnDateWhenSituationIsRemovedByPolicePowerEnds() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(REMOVED_BY_POLICE_POWER_ENDS.getValue())
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Removed by Police, powers ending soon";
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsVoluntarySectionCareOrder() {
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

        @Test
        void shouldReturnFormattedLivingSituationBasedOnDateWhenSituationIsVoluntarySectionCareOrder() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(VOLUNTARILY_SECTION_CARE_ORDER.getValue())
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Voluntarily in section 20 care order";
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsOther() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation("Other")
                        .addressChangeDate(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Other\nDate this began: " + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnFormattedLivingSituationBasedOnDateWhenSituationIsOther() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation("Other")
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Other";
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }
    }

    @Nested
    class DocmosisCaseDefaultSectionsTest {

        @Test
        void shouldNotReturnDefaultHearingDatailsWhenInfoNotGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .hearing(null)
                .build();

            DocmosisHearing expectedDefaultHearing = DocmosisHearing.builder()
                .timeFrame("-")
                .respondentsAwareReason("-")
                .reducedNoticeDetails("-")
                .withoutNoticeDetails("-")
                .respondentsAware("-")
                .typeAndReason("-")
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getHearing()).isEqualToComparingFieldByField(expectedDefaultHearing);
        }

        @Test
        void shouldReturnDefaultHearingPreferencesWhenInfoNotGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .hearingPreferences(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            DocmosisHearingPreferences expectedDefaultHearingPreference = DocmosisHearingPreferences.builder()
                .disabilityAssistance("-")
                .extraSecurityMeasures("-")
                .intermediary("-")
                .interpreter("-")
                .somethingElse("-")
                .welshDetails("-")
                .build();

            assertThat(caseSubmission.getHearingPreferences())
                .isEqualToComparingFieldByField(expectedDefaultHearingPreference);
        }

        @Test
        void shouldReturnDefaultRisksWhenInfoNotGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .risks(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            DocmosisRisks expectedDefaultRisk = DocmosisRisks.builder()
                .emotionalHarmDetails("-")
                .neglectDetails("-")
                .physicalHarmDetails("-")
                .sexualAbuseDetails("-")
                .build();

            assertThat(caseSubmission.getRisks()).isEqualToComparingFieldByField(expectedDefaultRisk);
        }

        @Test
        void shouldReturnDefaultFactorsAffectingParentingWhenInfoNotGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .factorsParenting(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            DocmosisFactorsParenting expectedFactorsParenting = DocmosisFactorsParenting.builder()
                .alcoholDrugAbuseDetails("-")
                .anythingElse("-")
                .domesticViolenceDetails("-")
                .build();

            assertThat(caseSubmission.getFactorsParenting()).isEqualToComparingFieldByField(expectedFactorsParenting);
        }

        @Test
        void shouldReturnDefaultInternationalElementWhenInfoNotGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .internationalElement(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            DocmosisInternationalElement expectedInternationalElement = DocmosisInternationalElement.builder()
                .internationalAuthorityInvolvement("-")
                .issues("-")
                .possibleCarer("-")
                .proceedings("-")
                .significantEvents("-")
                .build();

            assertThat(caseSubmission.getInternationalElement())
                .isEqualToComparingFieldByField(expectedInternationalElement);
        }
    }

    @Nested
    class DocmosisCaseSubmissionBuildApplicantTest {

        @Test
        void shouldReturnDefaultApplicantDetailsWhenInfoNotGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .solicitor(null)
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .email(EmailAddress.builder()
                            .email("applicantemail@gmail.com")
                            .build())
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("080-90909090")
                            .contactDirection("Contact name")
                            .build())
                        .dateOfBirth(NOW.minusYears(34))
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            DocmosisApplicant expectedDocmosisApplicant = DocmosisApplicant.builder()
                .organisationName("-")
                .jobTitle("-")
                .mobileNumber("-")
                .pbaNumber("-")
                .address("-")
                .email("applicantemail@gmail.com")
                .telephoneNumber("080-90909090")
                .contactName("Contact name")
                .solicitorDx("-")
                .solicitorEmail("-")
                .solicitorMobile("-")
                .solicitorName("-")
                .solicitorReference("-")
                .solicitorTelephone("-")
                .build();

            assertThat(caseSubmission.getApplicants()).hasSize(1);
            assertThat(caseSubmission.getApplicants().get(0))
                .isEqualToComparingFieldByField(expectedDocmosisApplicant);
        }
    }

    @Nested
    class DocmosisCaseSubmissionBuildRespondentTest {
        @Test
        void shouldNotReturnRespondentConfidentialDetailsWhenContactDetailsHiddenIsSetToYes() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .respondents1(wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .gender("They identify in another way")
                        .genderIdentification("Other gender")
                        .dateOfBirth(NOW.minusYears(34))
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
            assertThat(caseSubmission.getRespondents().get(0).getGender()).isEqualTo("Other gender");
        }

        @Test
        void shouldReturnRespondentAddressAndTelephoneDetailsWhenContactDetailsHiddenIsSetToNo() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .respondents1(wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .dateOfBirth(NOW.minusYears(34))
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

    @Nested
    class DocmosisCaseSubmissionBuildOtherPartyTest {
        @Test
        void shouldNotReturnOtherPartyConfidentialDetailsWhenDetailsHiddenIsSetToYes() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .address(Address.builder()
                            .addressLine1("Flat 13")
                            .postcode("SL11GF")
                            .build())
                        .detailsHidden("yes")
                        .telephone("090-0999000")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getAddress()).isEqualTo("Confidential");
            assertThat(caseSubmission.getOthers().get(0).getTelephoneNumber()).isEqualTo("Confidential");
        }

        @Test
        void shouldReturnOtherPartyAddressAndTelephoneDetailsWhenDetailsHiddenIsSetToNo() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .address(Address.builder()
                            .addressLine1("Flat 13")
                            .postcode("SL11GF")
                            .build())
                        .detailsHidden("no")
                        .telephone("090-0999000")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getAddress()).isEqualTo("Flat 13\nSL11GF");
            assertThat(caseSubmission.getOthers().get(0).getTelephoneNumber()).isEqualTo("090-0999000");
        }

        @Test
        void shouldReturnOtherPartyDOBAsDefaultStringWhenDOBIsNull() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getDateOfBirth()).isEqualTo("-");
        }

        @Test
        void shouldReturnOtherPartyFormattedDOBAsWhenDOBIsGiven() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .DOB("1999-02-02")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getDateOfBirth()).isEqualTo("2 February 1999");
        }
    }

    @Nested
    class DocmosisCaseSubmissionGetValidAnswerOrDefaultValueTest {

        @Test
        void shouldReturnRelevantProceedingAsEmptyWhenGivenProceedingsAreEmpty() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo("-");
        }

        @Test
        void shouldReturnRelevantProceedingAsYesWhenGivenOnGoingProceedingIsYes() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(Proceeding.builder()
                    .onGoingProceeding("yes")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo(YES.getValue());
        }

        @Test
        void shouldReturnRelevantProceedingAsNoWhenGivenOnGoingProceedingIsYes() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(Proceeding.builder()
                    .onGoingProceeding("no")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo(NO.getValue());
        }

        @Test
        void shouldReturnRelevantProceedingAsDontKnowWhenGivenOnGoingProceedingIsDontKnow() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(Proceeding.builder()
                    .onGoingProceeding("Don't know")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo(DONT_KNOW.getValue());
        }
    }

    @Nested
    class DocmosisCaseSubmissionFormatAnnexDocumentDisplayTest {

        @Test
        void shouldReturnEmptyWhenDocumentIsNotAvailable() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology()).isEqualTo("-");
        }

        @Test
        void shouldReturnEmptyWhenDocumentStatusIsEmpty() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(Document.builder()
                    .documentStatus("")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology()).isEqualTo("-");
        }

        @Test
        void shouldReturnStatusWhenDocumentStatusIsAvailable() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(Document.builder()
                    .documentStatus("Attached")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology()).isEqualTo("Attached");
        }

        @Test
        void shouldReturnStatusAndReasonWhenDocumentStatusIsOtherThanAttached() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(Document.builder()
                    .documentStatus("To follow")
                    .statusReason("Documents not uploaded")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology())
                .isEqualTo("To follow\nDocuments not uploaded");
        }

        @Test
        void shouldReturnDocumentTitleOrDefaultValueForAdditionalAnnexDocuments() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder()
                        .documentTitle("Additional Doc 1")
                        .typeOfDocument(DocumentReference.builder()
                            .url("/test.doc")
                            .build())
                        .build(),
                    DocumentSocialWorkOther.builder()
                        .documentTitle("Additional Doc 2")
                        .typeOfDocument(DocumentReference.builder()
                            .url("/test.doc")
                            .build())
                        .build(),
                    DocumentSocialWorkOther.builder()
                        .documentTitle("")
                        .typeOfDocument(DocumentReference.builder()
                            .url("/test.doc")
                            .build())
                        .build()
                ))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getOthers()).hasSize(3);
            assertThat(caseSubmission.getAnnexDocuments().getOthers().get(0).getDocumentTitle())
                .isEqualTo("Additional Doc 1");
            assertThat(caseSubmission.getAnnexDocuments().getOthers().get(1).getDocumentTitle())
                .isEqualTo("Additional Doc 2");
            assertThat(caseSubmission.getAnnexDocuments().getOthers().get(2).getDocumentTitle()).isEqualTo("-");
        }
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(now());
        return caseData;
    }
}
