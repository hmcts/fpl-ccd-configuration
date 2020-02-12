package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.Month.APRIL;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertNotEquals;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.service.robotics.SampleRoboticsTestDataHelper.expectedRoboticsData;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoboticsDataService.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    DateFormatterService.class, RoboticsDataValidatorService.class, ValidationAutoConfiguration.class})
public class RoboticsDataServiceTest {
    private static LocalDate NOW = LocalDate.now();

    private static long CASE_ID = 12345L;

    @Autowired
    private RoboticsDataService roboticsDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRoboticsJsonDataWithoutSolicitorNodeWhenSolicitorNull() throws IOException {
        CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER).toBuilder()
            .solicitor(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);
        String returnedRoboticsDataJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

        Map<String, Object> roboticsDataMap = objectMapper.reader()
            .forType(new TypeReference<Map<String, Object>>() {})
            .readValue(returnedRoboticsDataJson);

        assertThat(roboticsDataMap).doesNotContainKey("solicitor");
    }

    @Test
    void shouldThrowRoboticsDataExceptionWhenWhenAllocationProposalNull() throws IOException {
        CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER).toBuilder()
            .allocationProposal(null)
            .build();

        assertThrows(RoboticsDataException.class, () -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @Test
    void shouldReturnRoboticsDataWithExpectedlAllocationWhenAllocationProposalHasValue() throws IOException {
        final String expectedAllocation = "To be moved";

        CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER).toBuilder()
            .allocationProposal(Allocation.builder()
                .proposal("To be moved")
                .build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

        assertThat(roboticsData.getAllocation()).isEqualTo(expectedAllocation);
    }

    @Test
    void shouldReturnEmergencySupervisionOrderLabelWhenOrderTypeEmergencySupervisionOrder() throws IOException {
        CaseData caseData = prepareCaseData(NOW);

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

        assertThat(roboticsData).isEqualTo(expectedRoboticsData("Emergency Protection Order"));
    }

    @Test
    void shouldReturnFalseForHarmAllegedWhenRisksIsNull() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks, CASE_ID);

        assertThat(roboticsData.isHarmAlleged()).isFalse();
    }

    @Test
    void shouldReturnFalseForHarmAllegedWhenNoSelectionForRisks() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(Risks.builder().build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks, CASE_ID);

        assertThat(roboticsData.isHarmAlleged()).isFalse();
    }

    @Test
    void shouldReturnTrueForHarmAllegedWhenOneOfTheOptionsForRisksIsYes() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(Risks.builder()
                .physicalHarm("Yes")
                .emotionalHarm("No")
                .sexualAbuse("No")
                .neglect("No")
                .build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks, CASE_ID);

        assertThat(roboticsData.isHarmAlleged()).isTrue();
    }

    @Test
    void shouldReturnFalseForHarmAllegedWhenAllOfTheOptionsForRisksIsNo() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(Risks.builder()
                .physicalHarm("No")
                .emotionalHarm("No")
                .sexualAbuse("No")
                .neglect("No")
                .build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks, CASE_ID);

        assertThat(roboticsData.isHarmAlleged()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenInternationalElementIsNull() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement, CASE_ID);

        assertThat(roboticsData.isInternationalElement()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoSelectionForInternationalElement() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(InternationalElement.builder().build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement, CASE_ID);

        assertThat(roboticsData.isInternationalElement()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenOneOfTheOptionsForInternationalElementIsYes() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(InternationalElement.builder()
                .possibleCarer("Yes")
                .significantEvents("No")
                .issues("No")
                .proceedings("No")
                .internationalAuthorityInvolvement("No")
                .build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement, CASE_ID);

        assertThat(roboticsData.isInternationalElement()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAllOfTheOptionsForInternationalElementIsNo() throws IOException {
        CaseData caseData = prepareCaseData(NOW);
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(InternationalElement.builder()
                .possibleCarer("No")
                .significantEvents("No")
                .issues("No")
                .proceedings("No")
                .internationalAuthorityInvolvement("No")
                .build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement, CASE_ID);

        assertThat(roboticsData.isInternationalElement()).isFalse();
    }

    @Nested
    class RoboticsApplicationTypeTests {
        @Test
        void shouldReturnCareOrderLabelAsApplicationTypeWhenInterimCareOrderSelected() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Care Order");
        }

        @Test
        void shouldReturnCareOrderLabelAsApplicationTypeWhenCareOrderSelected() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Care Order");
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenInterimSupervisionOrderSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Supervision Order");
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenSupervisionOrderSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Supervision Order");
        }

        @Test
        void shouldReturnEducationSupervisionOrderLabelAsApplicationTypeWhenOrderTypeEducationSupervisionOrder()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(EDUCATION_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Education Supervision Order");
        }

        @Test
        void shouldReturnCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, EDUCATION_SUPERVISION_ORDER,
                EMERGENCY_PROTECTION_ORDER, OTHER);

            RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(preparedRoboticsData.getApplicationType()).isEqualTo(
                "Care Order,Education Supervision Order,Emergency Protection Order,"
                    + "Discharge of a Care Order");
        }

        @Test
        void shouldReturnNonDuplicatedCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, INTERIM_CARE_ORDER,
                INTERIM_SUPERVISION_ORDER, EDUCATION_SUPERVISION_ORDER, EMERGENCY_PROTECTION_ORDER, OTHER);

            RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(preparedRoboticsData.getApplicationType()).isEqualTo(
                "Care Order,Supervision Order,Education Supervision Order,Emergency Protection Order,"
                    + "Discharge of a Care Order");
        }
    }

    @Nested
    class RoboticsJsonTests {
        String expectedRoboticsDataJson;

        @BeforeEach
        void setup() throws IOException {
            expectedRoboticsDataJson = objectMapper.writeValueAsString(expectedRoboticsData(
                "Supervision Order"));
        }

        @Test
        void shouldNotReturnEmptyRoboticsJsonWhenNoError() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertNotEquals(new JSONObject().toString(), returnedRoboticsJson, true);
        }

        @Test
        void shouldReturnExpectedJsonStringWhenOrderTypeInterimSupervisionOrderType() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertEquals(expectedRoboticsDataJson, returnedRoboticsJson, true);
        }

        @Test
        void shouldReturnRoboticsJsonWithCommaSeparatedApplicationTypeWhenMultipleOrderTypeSelected()
            throws IOException {
            String expectedJsonWithCommaSeparatedApplicationType = objectMapper.writeValueAsString(
                expectedRoboticsData("Supervision Order,Care Order,Emergency Protection Order"));

            CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER, CARE_ORDER,
                EMERGENCY_PROTECTION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertEquals(returnedRoboticsJson, expectedJsonWithCommaSeparatedApplicationType, true);
        }

        @Test
        void shouldNotHaveCaseIdPropertyWhenRoboticsDataDeserializes() throws IOException {
            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(prepareCaseData(NOW), CASE_ID);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertThat(returnedRoboticsJson).isNotEmpty();

            Map<String, Object> returnedRoboticsDataMap = objectMapper.reader()
                .forType(new TypeReference<Map<String, Object>>() {})
                .readValue(returnedRoboticsJson);

            assertThat(returnedRoboticsDataMap).doesNotContainKey("caseId");
        }
    }

    @Test
    void shouldNotThrowRoboticsDataExceptionWhenApplicantMobileNumberIsValid() throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantMobileNumber("(0) 777 977 777");
        assertDoesNotThrow(() -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @Test
    void shouldNotThrowRoboticsDataExceptionWhenApplicantInternationalMobileNumberIsValid() throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantMobileNumber("+(0) 777 977 777");
        assertDoesNotThrow(() -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("blankAndNull")
    void shouldNotThrowRoboticsDataExceptionWhenApplicantMobileNumberIsNullOrEmpty(final String value)
        throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantMobileNumber(value);
        assertDoesNotThrow(() -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("inValidPhoneNumbers")
    void shouldThrowRoboticsDataExceptionWhenApplicantPhoneNumberIsInValid(String phoneNumber) throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantTelephoneNumber(phoneNumber);
        assertThrows(RoboticsDataException.class, () -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("inValidInternationalPhoneNumbers")
    void shouldThrowRoboticsDataExceptionWhenApplicantInternationalPhoneNumberIsInValid(String phoneNumber)
        throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantTelephoneNumber(phoneNumber);
        assertThrows(RoboticsDataException.class, () -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("validPhoneNumbers")
    void shouldNotThrowRoboticsDataExceptionWhenApplicantPhoneNumberIsValid(String phoneNumber) throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantTelephoneNumber(phoneNumber);
        assertDoesNotThrow(() -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("validInternationalPhoneNumbers")
    void shouldNotThrowRoboticsDataExceptionWhenApplicantInternationalPhoneNumberIsValid(String phoneNumber)
        throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantTelephoneNumber(phoneNumber);
        assertDoesNotThrow(() -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("blankAndNull")
    void shouldNotThrowRoboticsDataExceptionWhenApplicantPhoneNumberIsNullOrEmpty(final String value)
        throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantMobileNumber(value);
        assertDoesNotThrow(() -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("inValidMobileNumbers")
    void shouldThrowRoboticsDataExceptionWhenApplicantMobileNumberIsInValid(String mobileNumber) throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantMobileNumber(mobileNumber);
        assertThrows(RoboticsDataException.class, () -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    @ParameterizedTest
    @MethodSource("inValidInternationalMobileNumbers")
    void shouldThrowRoboticsDataExceptionWhenApplicantInternationalMobileNumberIsInValid(String mobileNumber)
        throws IOException {
        CaseData caseData = prepareCaseDataWithUpdatedApplicantMobileNumber(mobileNumber);
        assertThrows(RoboticsDataException.class, () -> roboticsDataService.prepareRoboticsData(caseData, CASE_ID));
    }

    private static Stream<String> inValidPhoneNumbers() {
        return Stream.of("01222233343444545556778889999887776655555544", "c/o");
    }

    private static Stream<String> inValidInternationalPhoneNumbers() {
        return Stream.of("+1800801920777777777888886565557778888", "c/o");
    }

    private static Stream<String> validPhoneNumbers() {
        return Stream.of("(0)20-8579 7105", "0208 579 7105", "202 762 1401", "c/o02085797105",
            "c/o 02085797105", "C/O02085797105");
    }

    private static Stream<String> validInternationalPhoneNumbers() {
        return Stream.of("c/o +44-(0)20-8579 7105", "+1 800 444 4444", "+1 914 232 9901", "C/O +1800 801 920");
    }

    private static Stream<String> inValidMobileNumbers() {
        return Stream.of("c/o yo!", "078888888888888888888888656");
    }

    private static Stream<String> inValidInternationalMobileNumbers() {
        return Stream.of("+1800801920777777777888886565557778888", "c/o");
    }

    private static Stream<String> blankAndNull() {
        return Stream.of("", null);
    }

    private CaseData prepareCaseData(LocalDate date) throws IOException {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(date);

        RespondentParty respondentPartyWithConfidentialDetails = RespondentParty.builder()
            .firstName("Billy")
            .lastName("Grant")
            .gender("Male")
            .dateOfBirth(LocalDate.of(1933, APRIL, 2))
            .contactDetailsHidden("Yes")
            .address(Address.builder()
                .addressLine1("Flat 90")
                .addressLine2("Surrey street")
                .addressLine3("Surrey road")
                .postTown("Surrey")
                .county("Croydon")
                .postcode("BT22 2345")
                .country("UK")
                .build())
            .build();

        Respondent respondent = Respondent.builder()
            .party(respondentPartyWithConfidentialDetails)
            .build();

        caseData.getRespondents1().add(element(respondent));

        return caseData;
    }

    private CaseData prepareCaseDataWithOrderType(final OrderType... orderTypes) throws IOException {
        return prepareCaseData(NOW).toBuilder()
            .orders(Orders.builder()
                .orderType(asList(orderTypes))
                .build())
            .build();
    }

    private CaseData prepareCaseDataWithUpdatedApplicantTelephoneNumber(final String telephoneNumber)
        throws IOException {
        return prepareCaseData(NOW).toBuilder()
            .applicants(wrapElements(Applicant.builder()
                .party(ApplicantParty.builder()
                    .telephoneNumber(Telephone.builder()
                        .telephoneNumber(telephoneNumber)
                        .build())
                    .build())
                .build()))
            .build();
    }

    private CaseData prepareCaseDataWithUpdatedApplicantMobileNumber(final String mobileNumber)
        throws IOException {
        return prepareCaseData(NOW).toBuilder()
            .applicants(wrapElements(Applicant.builder()
                .party(ApplicantParty.builder()
                    .mobileNumber(Telephone.builder()
                        .telephoneNumber(mobileNumber)
                        .build())
                    .build())
                .build()))
            .build();
    }
}
