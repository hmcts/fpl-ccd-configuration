package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static java.time.Month.APRIL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoboticsDataService.class, JacksonAutoConfiguration.class, LookupTestConfig.class})
class RoboticsDataServiceTest {

    @Autowired
    private RoboticsDataService roboticsDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRoboticsDataWithoutRespondentNodeWhenNoRespondents() {
        CaseData caseData = prepareCaseData().toBuilder()
            .respondents1(emptyList())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData.getRespondents()).isEmpty();
    }

    @Test
    void shouldReturnRoboticsDataWithoutApplicantNodeWhenApplicantIsNull() {
        CaseData caseData = prepareCaseData().toBuilder()
            .applicants(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData.getApplicant()).isNull();
    }

    @Test
    void shouldReturnRoboticsDataWithoutDateOfIssueWhenNotPresent() {
        CaseData caseData = prepareCaseData().toBuilder()
            .dateSubmitted(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData.getIssueDate()).isNull();
    }

    @Test
    void shouldReturnRoboticsDataWithEmptyApplicant() {
        CaseData caseData = prepareCaseData().toBuilder()
            .applicants(wrapElements(Applicant.builder()
                .party(ApplicantParty.builder().build())
                .build()))
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData.getApplicant())
            .isEqualTo(uk.gov.hmcts.reform.fpl.model.robotics.Applicant.builder().build());
    }

    @Test
    void shouldReturnEmergencySupervisionOrderLabelWhenOrderTypeEmergencySupervisionOrder() {
        CaseData caseData = prepareCaseData();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData).isEqualTo(expectedRoboticsData("Emergency Protection Order"));
    }

    @Test
    void shouldReturnFalseForHarmAllegedWhenRisksIsNull() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks);

        assertThat(roboticsData.isHarmAlleged()).isFalse();
    }

    @Test
    void shouldReturnFalseForHarmAllegedWhenNoSelectionForRisks() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithRisks = caseData.toBuilder()
            .risks(Risks.builder().build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks);

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

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks);

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

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithRisks);

        assertThat(roboticsData.isHarmAlleged()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenInternationalElementIsNull() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(null)
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement);

        assertThat(roboticsData.isInternationalElement()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoSelectionForInternationalElement() {
        CaseData caseData = prepareCaseData();
        CaseData caseDataWithInternationalElement = caseData.toBuilder()
            .internationalElement(InternationalElement.builder().build())
            .build();

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement);

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

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement);

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

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataWithInternationalElement);

        assertThat(roboticsData.isInternationalElement()).isFalse();
    }

    private CaseData prepareCaseData() {
        CaseData caseData = populatedCaseData();
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

    @Nested
    class Solicitor {

        @Test
        void shouldReturnRoboticsDataWithoutSolicitorNodeWhenSolicitorNull() {
            CaseData caseData = prepareCaseData().toBuilder()
                .solicitor(null)
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getSolicitor()).isNull();
        }

        @Test
        void shouldReturnRoboticsDataWithoutSolicitorNodeWhenSolicitorNameIsEmpty() {
            CaseData caseData = prepareCaseData().toBuilder()
                .solicitor(uk.gov.hmcts.reform.fpl.model.Solicitor.builder().build())
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getSolicitor()).isNull();
        }

        @Test
        void shouldReturnRoboticsDataWithoutSolicitorNodeWhenSolicitorNameIsNotFull() {
            CaseData caseData = prepareCaseData().toBuilder()
                .solicitor(uk.gov.hmcts.reform.fpl.model.Solicitor.builder()
                    .name("Smith")
                    .build())
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getSolicitor()).isNull();
        }
    }

    @Nested
    class Children {

        @Test
        void shouldReturnRoboticsDataWithoutChildrenNodeWhenNoChildren() {
            CaseData caseData = prepareCaseData().toBuilder()
                .children1(emptyList())
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getChildren()).isEmpty();
        }

        @Test
        void shouldReturnRoboticsDataWithEmptyChildWhenChildDoesNotHaveData() {
            CaseData caseData = prepareCaseData().toBuilder()
                .children1(wrapElements(Child.builder().party(
                    ChildParty.builder().build())
                    .build()))
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getChildren()).contains(uk.gov.hmcts.reform.fpl.model.robotics.Child.builder()
                .build());
        }
    }

    @Nested
    class AllocationProposal {
        @Test
        void shouldReturnRoboticsDataWithExpectedAllocationWhenAllocationProposalHasValue() {
            CaseData caseData = prepareCaseData().toBuilder()
                .allocationProposal(Allocation.builder()
                    .proposal("To be moved")
                    .build())
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getAllocation()).isEqualTo(caseData.getAllocationProposal().getProposal());
        }

        @Test
        void shouldReturnRoboticsDataWithoutAllocationWhenAllocationProposalHasEmptyProposal() {
            CaseData caseData = prepareCaseData().toBuilder()
                .allocationProposal(Allocation.builder()
                    .proposal("")
                    .build())
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getAllocation()).isNull();
        }

        @Test
        void shouldReturnRoboticsDataWithoutAllocationWhenAllocationProposalNotPresent() {
            CaseData caseData = prepareCaseData().toBuilder()
                .allocationProposal(null)
                .build();

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getAllocation()).isNull();
        }
    }

    @Nested
    class RoboticsApplicationTypeTests {

        @Test
        void shouldThrowExceptionWhenOrderTypeIsMissing() {
            CaseData caseData = prepareCaseDataWithOrderType();

            RoboticsDataException exception = assertThrows(RoboticsDataException.class,
                () -> roboticsDataService.prepareRoboticsData(caseData));

            assertThat(exception.getMessage()).isEqualTo("no order type(s) to derive Application Type from.");
        }

        @Test
        void shouldReturnCareOrderLabelAsApplicationTypeWhenInterimCareOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Care Order");
        }

        @Test
        void shouldReturnCareOrderLabelAsApplicationTypeWhenCareOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Care Order");
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenInterimSupervisionOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Supervision Order");
        }

        @Test
        void shouldReturnSupervisionOrderLabelAsApplicationTypeWhenSupervisionOrderSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Supervision Order");
        }

        @Test
        void shouldReturnEducationSupervisionOrderLabelAsApplicationTypeWhenOrderTypeEducationSupervisionOrder() {
            CaseData caseData = prepareCaseDataWithOrderType(EDUCATION_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(roboticsData.getApplicationType()).isEqualTo("Education Supervision Order");
        }

        @Test
        void shouldReturnCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, EDUCATION_SUPERVISION_ORDER,
                EMERGENCY_PROTECTION_ORDER, OTHER);

            RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData);

            assertThat(preparedRoboticsData.getApplicationType()).isEqualTo(
                "Care Order,Education Supervision Order,Emergency Protection Order,"
                    + "Discharge of a Care Order");
        }

        @Test
        void shouldReturnNonDuplicatedCommaSeparatedApplicationTypeWhenMoreThanOneOrderTypeSelected() {
            CaseData caseData = prepareCaseDataWithOrderType(CARE_ORDER, INTERIM_CARE_ORDER,
                INTERIM_SUPERVISION_ORDER, EDUCATION_SUPERVISION_ORDER, EMERGENCY_PROTECTION_ORDER, OTHER);

            RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData);

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

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertNotEquals(new JSONObject().toString(), returnedRoboticsJson, true);
        }

        @Test
        void shouldReturnExpectedJsonStringWhenOrderTypeInterimSupervisionOrderType() {
            CaseData caseData = prepareCaseDataWithOrderType(INTERIM_SUPERVISION_ORDER);

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
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

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertEquals(returnedRoboticsJson, expectedJsonWithCommaSeparatedApplicationType, true);
        }

        @Test
        void shouldNotHaveCaseIdPropertyWhenRoboticsDataDeserializes() throws IOException {
            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(prepareCaseData());
            String returnedRoboticsJson = roboticsDataService.convertRoboticsDataToJson(roboticsData);

            assertThat(returnedRoboticsJson).isNotEmpty();

            Map<String, Object> returnedRoboticsDataMap = objectMapper.reader()
                .forType(new TypeReference<Map<String, Object>>() {
                })
                .readValue(returnedRoboticsJson);

            assertThat(returnedRoboticsDataMap).doesNotContainKey("caseId");
        }
    }

    @Nested
    class FeePaid {
        private CaseData.CaseDataBuilder caseDataBuilder;

        @BeforeEach
        void setup() {
            caseDataBuilder = prepareCaseData().toBuilder();
        }

        @Test
        void shouldGetFeePaidFromCaseData() {
            caseDataBuilder.amountToPay("100000");

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataBuilder.build());

            MatcherAssert.assertThat(roboticsData.getFeePaid(), Matchers.comparesEqualTo(BigDecimal.valueOf(1000.00)));
        }

        @Test
        void shouldGetDefaultFeePaidWhenThereIsNoFeePaidInClaimData() {
            caseDataBuilder.amountToPay("");

            RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseDataBuilder.build());

            MatcherAssert.assertThat(roboticsData.getFeePaid(), Matchers.comparesEqualTo(BigDecimal.valueOf(2055.00)));
        }
    }
}
