package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;

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
            .judgeTitle("HHJ")
            .judgeName("Judith Law")
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
                .build(),
            Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Jon")
                        .lastName("Snow")
                        .build())
                    .build())
                .build());
    }

    public static Order createStandardDirectionOrders(LocalDateTime today, OrderStatus status) {
        return Order.builder()
            .directions(ImmutableList.of(
                Element.<Direction>builder()
                    .id(UUID.randomUUID())
                    .value(Direction.builder()
                        .directionType("Test SDO type 1")
                        .directionText("Test body 1")
                        .dateToBeCompletedBy(today)
                        .assignee(ALL_PARTIES)
                        .build())
                    .build(),
                Element.<Direction>builder()
                    .id(UUID.randomUUID())
                    .value(Direction.builder()
                        .directionType("Test SDO type 2")
                        .directionText("Test body 2")
                        .dateToBeCompletedBy(today)
                        .assignee(ALL_PARTIES)
                        .build())
                    .build()
            ))
            .orderStatus(status)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Smith")
                .legalAdvisorName("Bob Ross")
                .build())
            .build();
    }

    public static List<Element<Respondent>> createRespondents() {
        return ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Timothy")
                        .lastName("Jones")
                        .relationshipToChild("Father")
                        .build())
                    .build())
                .build(),
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Sarah")
                        .lastName("Simpson")
                        .relationshipToChild("Mother")
                        .build())
                    .build())
                .build()
        );
    }
}
