package uk.gov.hmcts.reform.fpl.service.docmosis;

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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisAnnexDocuments;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicant;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingPreferences;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRisks;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType.CHILD_WHEREABOUTS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.HOSPITAL_SOON_TO_BE_DISCHARGED;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.REMOVED_BY_POLICE_POWER_ENDS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.VOLUNTARILY_SECTION_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.DRAFT_WATERMARK;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.DONT_KNOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionGenerationService.class, JacksonAutoConfiguration.class,
    LookupTestConfig.class, FixedTimeConfiguration.class})
class CaseSubmissionGenerationServiceTest {
    private static final LocalDate NOW = now();

    private static final String FORMATTED_DATE = formatLocalDateToString(NOW, DATE);
    private static final String AUTH_TOKEN = "Bearer token";
    private static final DocmosisAnnexDocuments DOCMOSIS_ANNEX_DOCUMENTS = mock(DocmosisAnnexDocuments.class);

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private CaseSubmissionDocumentAnnexGenerator annexGenerator;

    @MockBean
    private RequestData requestData;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseSubmissionGenerationService templateDataGenerationService;

    private CaseData givenCaseData;

    @BeforeEach
    void init() {
        givenCaseData = prepareCaseData();
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().name("Professor").build());
    }

    @Test
    void shouldReturnExpectedTemplateDataWithCourtSealWhenAllDataPresent() {
        DocmosisCaseSubmission returnedCaseSubmission = templateDataGenerationService.getTemplateData(givenCaseData);

        DocmosisCaseSubmission updatedCaseSubmission = expectedDocmosisCaseSubmission().toBuilder()
            .annexDocuments(null)
            .build();

        assertThat(returnedCaseSubmission).isEqualToComparingFieldByField(updatedCaseSubmission);
    }

    @Test
    void shouldReturnExpectedCaseNumberInDocmosisCaseSubmissionWhenCaseNumberGiven() {
        String expectedCaseNumber = "12345";
        DocmosisCaseSubmission returnedCaseSubmission = templateDataGenerationService.getTemplateData(givenCaseData);

        templateDataGenerationService.populateCaseNumber(returnedCaseSubmission, 12345L);

        assertThat(returnedCaseSubmission.getCaseNumber()).isEqualTo(expectedCaseNumber);
    }

    @Nested
    class DocmosisCaseSubmissionSigneeNameTest {
        @Test
        void shouldReturnExpectedSigneeNameWhenLegalTeamManagerPresent() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .legalTeamManager("legal team manager")
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);
            assertThat(caseSubmission.getUserFullName()).isEqualTo("legal team manager");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnIdamUserNameAsSigneeNameWhenApplicantDetailsAreNullOrEmpty(
            List<Element<Applicant>> applicants) {

            CaseData updatedCaseData = givenCaseData.toBuilder().applicants(applicants).build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);
            assertThat(caseSubmission.getUserFullName()).isEqualTo("Professor");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnCurrentUserWhenLegalTeamManagerIsEmptyOrNotPresent(String legalTeamManager) {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .legalTeamManager(legalTeamManager)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);
            assertThat(caseSubmission.getUserFullName()).isEqualTo("Professor");
        }
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
                    .epoType(REMOVE_TO_ACCOMMODATION)
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\n"
                + "Remove to accommodation\n"
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
                    .epoType(REMOVE_TO_ACCOMMODATION)
                    .emergencyProtectionOrderDetails("emergency protection order details")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\n"
                + "Remove to accommodation\n"
                + "Information on the whereabouts of the child\n"
                + "emergency protection order details";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }

        @Test
        void shouldIncludeAddressWhenPreventRemovalEPOTypeSelected() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrders(of(EmergencyProtectionOrdersType.values()))
                    .epoType(PREVENT_REMOVAL)
                    .address(Address.builder()
                        .addressLine1("45")
                        .addressLine2("Ethel Street")
                        .postcode("BT7H3B")
                        .postTown("Lisburn Road")
                        .country("United Kingdom")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\n"
                + "Prevent removal from an address\n"
                + "45\n"
                + "Ethel Street\n"
                + "Lisburn Road\n"
                + "BT7H3B\n"
                + "United Kingdom\n"
                + "Information on the whereabouts of the child\n"
                + "Authorisation for entry of premises\n"
                + "Authorisation to search for another child on the premises\n"
                + "Other order under section 48 of the Children Act 1989";
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

            assertThat(caseSubmission.getGroundsForEPOReason()).isEmpty();
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

            assertThat(caseSubmission.getGroundsForEPOReason()).isEmpty();
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
        void shouldNotAppendBeyondParentalControlToGroundsThresholdReasonWhenThresholdReasonIsNotBeyondControl() {
            CaseData updatedCasData = givenCaseData.toBuilder()
                .grounds(Grounds.builder()
                    .thresholdReason(of("test", "noCare"))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCasData);

            assertThat(caseSubmission.getGroundsThresholdReason())
                .isEqualTo("Not receiving care that would be reasonably expected from a parent.");
        }

        @Test
        void shouldReturnDefaultValueForGroundsThresholdReasonWhenTGroundsIsNull() {
            CaseData updatedCasData = givenCaseData.toBuilder()
                .grounds(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCasData);

            assertThat(caseSubmission.getGroundsThresholdReason()).isEqualTo("-");
        }

        @Test
        void shouldReturnDefaultValueForGroundsThresholdReasonWhenTGroundsIsNotNullAndThresholdReasonEmpty() {
            CaseData updatedCasData = givenCaseData.toBuilder()
                .grounds(Grounds.builder()
                    .thresholdReason(of())
                    .build())
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
        void shouldReturnDefaultValueWhenEmergencyProtectionOrderDirectionsOrDirectionsIsNull() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(Orders.builder()
                    .emergencyProtectionOrderDirections(null)
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

        @Test
        void shouldIncludeEPOExcludedWhenEntered() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(Orders.builder()
                    .directions("directions")
                    .directionDetails("direction details")
                    .excluded("John Doe")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedDirectionsNeeded = "John Doe excluded\ndirections\ndirection details";
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
        void shouldReturnOtherPartyDOBAsDefaultStringWhenDOBIsEmpty() {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .dateOfBirth("")
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
                        .dateOfBirth("1999-02-02")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getDateOfBirth()).isEqualTo("2 February 1999");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnOtherPartyGenderAsMaleWhenNoGenderIdentificationIsullOrEmpty(
            final String genderIdentification) {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .gender("male")
                        .genderIdentification(genderIdentification)
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getGender()).isEqualTo("male");
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

    @Test
    void shouldBuildExpectedDocmosisAnnexDocumentsWhenApplicationDocumentsIncludeAnnexDocumentTypes() {
        when(annexGenerator.generate(givenCaseData)).thenReturn(DOCMOSIS_ANNEX_DOCUMENTS);

        DocmosisCaseSubmission actual = templateDataGenerationService.getTemplateData(givenCaseData);

        assertThat(actual.getAnnexDocuments()).isEqualTo(DOCMOSIS_ANNEX_DOCUMENTS);
    }

    @Nested
    class DocmosisCaseSubmissionDraftWaterMarkOrCourtSeal {
        private DocmosisCaseSubmission caseSubmission;

        @BeforeEach
        void setup() {
            caseSubmission = templateDataGenerationService.getTemplateData(givenCaseData);
        }

        @Test
        void shouldHaveDocmosisCaseSubmissionWithDraftWatermarkWhenApplicationIsDraft() {
            templateDataGenerationService.populateDraftWaterOrCourtSeal(caseSubmission, true);

            assertThat(caseSubmission.getDraftWaterMark()).isEqualTo(DRAFT_WATERMARK.getValue());
            assertThat(caseSubmission.getCourtSeal()).isNull();
        }

        @Test
        void shouldHaveDocmosisCaseSubmissionWithCourtSealWhenApplicationIsNotDraft() {
            templateDataGenerationService.populateDraftWaterOrCourtSeal(caseSubmission, false);

            assertThat(caseSubmission.getCourtSeal()).isEqualTo(COURT_SEAL.getValue());
            assertThat(caseSubmission.getDraftWaterMark()).isNull();
        }
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(now());
        return caseData;
    }
}
