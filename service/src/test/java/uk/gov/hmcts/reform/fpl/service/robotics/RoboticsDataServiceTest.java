package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.robotics.*;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.*;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RoboticsDataService.class, JacksonAutoConfiguration.class,
    LookupTestConfig.class, DateFormatterService.class})
public class RoboticsDataServiceTest {
    private static LocalDate NOW = LocalDate.now();

    @Autowired
    private RoboticsDataService roboticsDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Test
    void shouldReturnExpectedRoboticsData() throws IOException {
        CaseData caseData = prepareCaseData(NOW);

        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(roboticsData).isEqualTo(expectedRoboticsData(EMERGENCY_PROTECTION_ORDER.getLabel()));
    }

    @Test
    void shouldReturnExpectedRoboticsDataWithCommaSeparatedApplicationType() throws IOException {
        CaseData caseData = prepareCaseData(NOW).toBuilder()
            .orders(Orders.builder()
                .orderType(asList(CARE_ORDER, EDUCATION_SUPERVISION_ORDER, EMERGENCY_PROTECTION_ORDER))
            .build())
            .build();

        RoboticsData preparedRoboticsData = roboticsDataService.prepareRoboticsData(caseData);

        assertThat(preparedRoboticsData.getApplicationType()).isEqualTo("Care order,Education supervision order," +
            "Emergency protection order");
    }

    private RoboticsData expectedRoboticsData(final String applicationType) {
        return RoboticsData.builder()
            .caseNumber("12345")
            .applicationType(applicationType)
            .feePaid(2055.00)
            .children(expectedChildren())
            .respondents(expectedRespondents())
            .solicitor(Solicitor.builder()
                .firstName("Brian")
                .lastName("Banks")
                .build())
            .harmAlleged(true)
            .internationalElement(true)
            .allocation("Section 9 circuit judge")
            .issueDate(dateFormatterService.formatLocalDateToString(NOW, "dd-MM-yyyy"))
            .applicant(expectedApplicant())
            .owningCourt(11)
            .build();
    }

    private Set<Respondent> expectedRespondents() {
        return ImmutableSet.of(Respondent.builder()
            .firstName("An")
            .lastName("Other")
            .gender("MALE")
            .address(Address.builder()
                .addressLine1("Flat 90")
                .addressLine2("Surrey street")
                .addressLine3("Surrey road")
                .postTown("Surrey")
                .county("Croydon")
                .postcode(null)
                .country("UK")
                .build())
            .relationshipToChild(null)
            .dob("2-APR-1933")
            .confidential(true)
            .build(), Respondent.builder()
            .firstName("James")
            .lastName("Smith")
            .gender(null)
            .address(Address.builder()
                .addressLine1("Unit 12")
                .addressLine2("Transa Way")
                .addressLine3("Hillsborough")
                .postTown("Lurgan")
                .county("Down")
                .postcode(null)
                .country("United Kingdom")
                .build())
            .relationshipToChild("Brother")
            .dob("2-APR-1933")
            .confidential(true)
            .build(), Respondent.builder()
            .firstName("Paul")
            .lastName("Smith")
            .gender("MALE")
            .address(Address.builder()
                .addressLine1(null)
                .addressLine2(null)
                .addressLine3(null)
                .postTown(null)
                .county(null)
                .postcode(null)
                .country(null)
                .build())
            .relationshipToChild("Uncle")
            .dob("2-MAY-1944")
            .confidential(true)
            .build());
    }

    private Set<Child> expectedChildren() {
        return ImmutableSet.of(Child.builder()
            .firstName("Tom")
            .lastName("Reeves")
            .gender("MALE")
            .dob("15-JUN-2018")
            .isParty(true)
            .build(), Child.builder()
            .firstName("Sarah")
            .lastName("Reeves")
            .gender("FEMALE")
            .dob("2-FEB-2002")
            .isParty(true)
            .build());
    }

    private Applicant expectedApplicant() {
        return Applicant.builder()
            .name(null)
            .contactName("Jane Smith")
            .jobTitle("Legal adviser")
            .address(Address.builder()
                .addressLine1("160 Tooley St")
                .addressLine2("Tooley road")
                .addressLine3("Tooley")
                .postTown("Limerick")
                .county("Galway")
                .country("Ireland")
                .postcode(null)
                .build())
            .mobileNumber("2020202020")
            .telephoneNumber("02120202020")
            .email("jane@smith.com")
            .build();
    }

    private CaseData prepareCaseData(LocalDate date) throws IOException {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(date);
        return caseData;
    }
}
