package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.robotics.*;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.service.robotics.SampleRoboticsTestData.expectedRoboticsData;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoboticsDataService.class, JacksonAutoConfiguration.class, LookupTestConfig.class})
public class RoboticsDataServiceTest {
    private static LocalDate NOW = LocalDate.now();

    @Autowired
    private RoboticsDataService roboticsDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnExpectedRoboticsData() throws IOException {
        CaseData caseData = prepareCaseData(NOW);

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData).isEqualTo(expectedRoboticsData(EMERGENCY_PROTECTION_ORDER.getLabel()));
    }

    @Test
    void shouldReturnExpectedRoboticsDataWithCommaSeparatedApplicationType() throws IOException {
        CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, EDUCATION_SUPERVISION_ORDER,
            EMERGENCY_PROTECTION_ORDER, OTHER);

        RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(preparedRoboticsData.getApplicationType()).isEqualTo(
            "Care order,Education supervision order,Emergency protection order," +
            "Other order under part 4 of the Children Act 1989");
    }

    @Test
    void shouldReturnExpectedRoboticsDataJsonString() throws IOException {
        String expectedRoboticsDataJson = objectMapper.writeValueAsString(expectedRoboticsData(
            SUPERVISION_ORDER.getLabel()));

        CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
        String returnedRoboticsDataJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

        assertEquals(expectedRoboticsDataJson, returnedRoboticsDataJson, true);
    }

    @Test
    void shouldReturnRoboticsJsonDataWithoutSolicitorNode() throws IOException {
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
