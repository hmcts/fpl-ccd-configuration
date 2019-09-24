package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CaseDataGeneratorHelper {
    private CaseDataGeneratorHelper() {
        // NO-OP
    }

    public static HearingBooking createHearingBooking(LocalDate date) {
        return HearingBooking.builder()
            .date(date)
            .venue("Venue")
            .preHearingAttendance("08.15am")
            .time("09.15am")
            .build();
    }

    public static List<Element<Applicant>> createPopulatedApplicants() {
        return ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .leadApplicantIndicator("No")
                    .party(ApplicantParty.builder()
                        .organisationName("Bran Stark")
                        .build())
                    .build())
                .build(),
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .leadApplicantIndicator("No")
                    .party(ApplicantParty.builder()
                        .organisationName("Sansa Stark")
                        .build())
                    .build())
                .build());
    }

    public static List<Element<Child>> createPopulatedChildren() {
        return ImmutableList.of(
            Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Bran")
                        .lastName("Stark")
                        .gender("Male")
                        .dateOfBirth(LocalDate.now())
                        .build())
                    .build())
                .build(),
            Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Sansa")
                        .lastName("Stark")
                        .build())
                    .build())
                .build());
    }


    public static Order createStandardDirectionOrders() {
        return Order.builder()
            .directions(ImmutableList.of(
                Element.<Direction>builder()
                    .id(UUID.randomUUID())
                    .value(Direction.builder()
                        .type("Test SDO type")
                        .text("Test body 1")
                        .completeBy(LocalDateTime.now())
                        .build())
                    .build(),
                Element.<Direction>builder()
                    .id(UUID.randomUUID())
                    .value(Direction.builder()
                        .type("Test SDO type")
                        .text("Test body 2")
                        .completeBy(LocalDateTime.now())
                        .build())
                    .build()
            )).build();
    }
}
