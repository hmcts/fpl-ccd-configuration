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
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static java.lang.String.join;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoboticsDataService.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    DateFormatterService.class})
public class RoboticsDataServiceTest {
    private static LocalDate NOW = LocalDate.now();

    @Autowired
    private RoboticsDataService roboticsDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRoboticsJsonDataWithoutSolicitorNodeWhenSolicitorNull() throws IOException {
        CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER).toBuilder()
            .solicitor(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
        String returnedRoboticsDataJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

        Map<String, Object> roboticsDataMap = objectMapper.reader()
            .forType(new TypeReference<Map<String, Object>>() {})
            .readValue(returnedRoboticsDataJson);

        assertThat(roboticsDataMap).doesNotContainKey("solicitor");
    }

    @Test
    void shouldReturnRoboticsDataWithNullAllocationWhenAllocationProposalNull() throws IOException {
        CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER).toBuilder()
            .allocationProposal(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData.getAllocation()).isNull();
    }

    @Test
    void shouldReturnRoboticsDataWithExpectedlAllocationWhenAllocationProposalHasValue() throws IOException {
        final String expectedAllocation = "To be moved";

        CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER).toBuilder()
            .allocationProposal(Allocation.builder()
                .proposal("To be moved")
                .build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData.getAllocation()).isEqualTo(expectedAllocation);
    }

    @Test
    void shouldReturnEmergencySupervisionOrderLabelWhenOrderTypeEmergencySupervisionOrder() throws IOException {
        CaseData caseData = prepareCaseData(NOW);

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData).isEqualTo(expectedRoboticsData(EMERGENCY_PROTECTION_ORDER.getLabel()));
    }

    @Nested
    class RoboticsApplicationTypeTests {
        @Test
        void shouldReturnCareOrderLabelAsApplicationTypeWhenInterimCareOrderSelected() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo(CARE_ORDER.getLabel());
        }

        @Test
        void shouldReturnCareOrderLabelAsApplicationTypeWhenCareOrderSelected() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo(CARE_ORDER.getLabel());
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenInterimSupervisionOrderSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo(SUPERVISION_ORDER.getLabel());
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenSupervisionOrderSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo(SUPERVISION_ORDER.getLabel());
        }

        @Test
        void shouldReturnEducationSupervisionOrderLabelAsApplicationTypeWhenOrderTypeEducationSupervisionOrder()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(EDUCATION_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo(EDUCATION_SUPERVISION_ORDER.getLabel());
        }

        @Test
        void shouldReturnCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, EDUCATION_SUPERVISION_ORDER,
                EMERGENCY_PROTECTION_ORDER, OTHER);

            RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(preparedRoboticsData.getApplicationType()).isEqualTo(
                "Care order,Education supervision order,Emergency protection order,"
                    + "Discharge of care");
        }

        @Test
        void shouldReturnNonDuplicatedCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected()
            throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, INTERIM_CARE_ORDER,
                INTERIM_SUPERVISION_ORDER, EDUCATION_SUPERVISION_ORDER, EMERGENCY_PROTECTION_ORDER, OTHER);

            RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(preparedRoboticsData.getApplicationType()).isEqualTo(
                "Care order,Supervision order,Education supervision order,Emergency protection order,"
                    + "Discharge of care");
        }
    }

    @Nested
    class RoboticsJsonTests {
        String expectedRoboticsDataJson;

        @BeforeEach
        void setup() throws IOException {
            expectedRoboticsDataJson = objectMapper.writeValueAsString(expectedRoboticsData(
                SUPERVISION_ORDER.getLabel()));
        }

        @Test
        void shouldNotReturnEmptyRoboticsJsonWhenNoError() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertNotEquals(new JSONObject().toString(), returnedRoboticsJson, true);
        }

        @Test
        void shouldReturnExpectedJsonStringWhenOrderTypeInterimSupervisionOrderType() throws IOException {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertEquals(expectedRoboticsDataJson, returnedRoboticsJson, true);
        }

        @Test
        void shouldReturnRoboticsJsonWithCommaSeparatedApplicationTypeWhenMultipleOrderTypeSelected()
            throws IOException {
            String expectedJsonWithCommaSeparatedApplicationType = objectMapper.writeValueAsString(
                expectedRoboticsData(join(",", SUPERVISION_ORDER.getLabel(), CARE_ORDER.getLabel(),
                    EMERGENCY_PROTECTION_ORDER.getLabel())));

            CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER, CARE_ORDER,
                EMERGENCY_PROTECTION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertEquals(returnedRoboticsJson, expectedJsonWithCommaSeparatedApplicationType, true);
        }
    }

    private CaseData prepareCaseData(LocalDate date) throws IOException {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(date);
        return caseData;
    }

    private CaseData prepareCaseDataWithOrderType(final OrderType... orderTypes) throws IOException {
        return prepareCaseData(NOW).toBuilder()
            .orders(Orders.builder()
                .orderType(asList(orderTypes))
                .build())
            .build();
    }
}
