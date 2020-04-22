package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static java.time.Month.APRIL;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoboticsDataService.class, JacksonAutoConfiguration.class, LookupTestConfig.class})
public class RoboticsDataServiceTest {

    private static final long CASE_ID = 12345L;

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
            .forType(new TypeReference<Map<String, Object>>() {
            })
            .readValue(returnedRoboticsDataJson);

        assertThat(roboticsDataMap).doesNotContainKey("solicitor");
    }

    @Test
    void shouldReturnRoboticsDataWithExpectedlAllocationWhenAllocationProposalHasValue() {
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
    void shouldReturnEmergencySupervisionOrderLabelWhenOrderTypeEmergencySupervisionOrder() {
        CaseData caseData = prepareCaseData();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

        assertThat(roboticsData).isEqualTo(expectedRoboticsData("Emergency Protection Order"));
    }

    @Test
    void shouldReturnFalseForHarmAllegedWhenRisksIsNull() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks, CASE_ID);

        assertThat(roboticsData.isHarmAlleged()).isFalse();
    }

    @Test
    void shouldReturnFalseForHarmAllegedWhenNoSelectionForRisks() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(Risks.builder().build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks, CASE_ID);

        assertThat(roboticsData.isHarmAlleged()).isFalse();
    }

    @Test
    void shouldReturnTrueForHarmAllegedWhenOneOfTheOptionsForRisksIsYes() {
        CaseData caseData = prepareCaseData();
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
    void shouldReturnFalseForHarmAllegedWhenAllOfTheOptionsForRisksIsNo() {
        CaseData caseData = prepareCaseData();
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
    void shouldReturnFalseWhenInternationalElementIsNull() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement, CASE_ID);

        assertThat(roboticsData.isInternationalElement()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoSelectionForInternationalElement() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(InternationalElement.builder().build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement, CASE_ID);

        assertThat(roboticsData.isInternationalElement()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenOneOfTheOptionsForInternationalElementIsYes() {
        CaseData caseData = prepareCaseData();
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
    void shouldReturnFalseWhenAllOfTheOptionsForInternationalElementIsNo() {
        CaseData caseData = prepareCaseData();
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
        void shouldReturnCareOrderLabelAsApplicationTypeWhenInterimCareOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Care Order");
        }

        @Test
        void shouldReturnCareOrderLabelAsApplicationTypeWhenCareOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Care Order");
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenInterimSupervisionOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Supervision Order");
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenSupervisionOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Supervision Order");
        }

        @Test
        void shouldReturnEducationSupervisionOrderLabelAsApplicationTypeWhenOrderTypeEducationSupervisionOrder() {
            CaseData caseData = prepareCaseDataWithOrderType(EDUCATION_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Education Supervision Order");
        }

        @Test
        void shouldReturnCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, EDUCATION_SUPERVISION_ORDER,
                EMERGENCY_PROTECTION_ORDER, OTHER);

            RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);

            assertThat(preparedRoboticsData.getApplicationType()).isEqualTo(
                "Care Order,Education Supervision Order,Emergency Protection Order,"
                    + "Discharge of a Care Order");
        }

        @Test
        void shouldReturnNonDuplicatedCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected() {
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
        void shouldNotReturnEmptyRoboticsJsonWhenNoError() {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, CASE_ID);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertNotEquals(new JSONObject().toString(), returnedRoboticsJson, true);
        }

        @Test
        void shouldReturnExpectedJsonStringWhenOrderTypeInterimSupervisionOrderType() {
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
            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(prepareCaseData(), CASE_ID);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertThat(returnedRoboticsJson).isNotEmpty();

            Map<String, Object> returnedRoboticsDataMap = objectMapper.reader()
                .forType(new TypeReference<Map<String, Object>>() {
                })
                .readValue(returnedRoboticsJson);

            assertThat(returnedRoboticsDataMap).doesNotContainKey("caseId");
        }
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(LocalDate.now());

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

    private CaseData prepareCaseDataWithOrderType(final OrderType... orderTypes) {
        return prepareCaseData().toBuilder()
            .orders(Orders.builder()
                .orderType(asList(orderTypes))
                .build())
            .build();
    }
}
