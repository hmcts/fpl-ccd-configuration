package uk.gov.hmcts.reform.fpl.service.robotics;

import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.fpl.model.robotics.Address;
import uk.gov.hmcts.reform.fpl.model.robotics.Applicant;
import uk.gov.hmcts.reform.fpl.model.robotics.Child;
import uk.gov.hmcts.reform.fpl.model.robotics.Respondent;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.model.robotics.Solicitor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

public class SampleRoboticsTestDataHelper {
    private static LocalDate NOW = LocalDate.now();

    private SampleRoboticsTestDataHelper() {
    }

    public static RoboticsData expectedRoboticsData(final String applicationType) {
        return RoboticsData.builder()
            .caseNumber("12345")
            .applicationType(applicationType)
            .feePaid(BigDecimal.valueOf(2055.00))
            .children(expectedChildren())
            .respondents(expectedRespondents())
            .solicitor(Solicitor.builder()
                .firstName("Brian")
                .lastName("Banks")
                .build())
            .harmAlleged(true)
            .internationalElement(true)
            .allocation("Circuit judge")
            .issueDate(formatLocalDateToString(NOW, "dd-MM-yyyy"))
            .applicant(expectedApplicant())
            .owningCourt(11)
            .caseId(12345L)
            .build();
    }

    public static RoboticsData invalidRoboticsDataWithZeroOwningCourt() {
        return expectedRoboticsData(CARE_ORDER.getLabel()).toBuilder()
            .owningCourt(0)
            .build();
    }

    public static Set<Respondent> expectedRespondents() {
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
                    .postcode("BT22 2345")
                    .country("UK")
                    .build())
                .relationshipToChild("Cousin")
                .dob("2-APR-1933")
                .confidential(false)
                .build(),
            Respondent.builder()
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
                .confidential(false)
                .build(),
            Respondent.builder()
                .firstName("James")
                .lastName("Smith")
                .gender(null)
                .address(Address.builder()
                    .addressLine1("Unit 12")
                    .addressLine2("Transa Way")
                    .addressLine3("Hillsborough")
                    .postTown("Lurgan")
                    .county("Down")
                    .postcode("BT26 6RJ")
                    .country("United Kingdom")
                    .build())
                .relationshipToChild("Brother")
                .dob("2-APR-1933")
                .confidential(false)
                .build(),
            Respondent.builder()
                .firstName("Billy")
                .lastName("Grant")
                .gender("MALE")
                .address(Address.builder()
                    .addressLine1("Flat 90")
                    .addressLine2("Surrey street")
                    .addressLine3("Surrey road")
                    .postTown("Surrey")
                    .county("Croydon")
                    .postcode("BT22 2345")
                    .country("UK")
                    .build())
                .relationshipToChild(null)
                .dob("2-APR-1933")
                .confidential(true)
                .build());
    }

    public static Set<Child> expectedChildren() {
        return ImmutableSet.of(Child.builder()
                .firstName("Sarah")
                .lastName("Reeves")
                .gender("FEMALE")
                .dob("2-FEB-2002")
                .isParty(false)
                .build(),
            Child.builder()
                .firstName("Tom")
                .lastName("Reeves")
                .gender("MALE")
                .dob("15-JUN-2018")
                .isParty(false)
                .build());
    }

    public static Applicant expectedApplicant() {
        return Applicant.builder()
            .name("London Borough of Southwark")
            .contactName("Jane Smith")
            .jobTitle("Legal adviser")
            .address(Address.builder()
                .addressLine1("160 Tooley St")
                .addressLine2("Tooley road")
                .addressLine3("Tooley")
                .postTown("Limerick")
                .county("Galway")
                .country("Ireland")
                .postcode("SE1 2QH")
                .build())
            .mobileNumber("2020202020")
            .telephoneNumber("02120202020")
            .email("jane@smith.com")
            .build();
    }

}
