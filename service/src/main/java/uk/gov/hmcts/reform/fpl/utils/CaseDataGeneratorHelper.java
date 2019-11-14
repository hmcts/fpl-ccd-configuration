package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;

public class CaseDataGeneratorHelper {

    private static final DateFormatterService DATE_FORMATTER_SERVICE = new DateFormatterService();
    private static final String FORMAT_STYLE = "h:mma, d MMMM yyyy";

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
                .id(randomUUID())
                .value(Applicant.builder()
                    .leadApplicantIndicator("No")
                    .party(ApplicantParty.builder()
                        .organisationName("Bran Stark")
                        .jobTitle("Judge")
                        .address(Address.builder()
                            .addressLine1("1 Some street")
                            .addressLine2("Some road")
                            .postTown("some town")
                            .postcode("BT66 7RR")
                            .county("Some county")
                            .country("UK")
                            .build())
                        .email(EmailAddress.builder()
                            .email("BranStark@hMCTS.net")
                            .build())
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("02838882404")
                            .contactDirection("Bran Stark")
                            .build())
                        .build())
                    .build())
                .build(),
            Element.<Applicant>builder()
                .id(randomUUID())
                .value(Applicant.builder()
                    .leadApplicantIndicator("No")
                    .party(ApplicantParty.builder()
                        .organisationName("Sansa Stark")
                        .jobTitle("Judge")
                        .address(Address.builder()
                            .addressLine1("1 Some street")
                            .addressLine2("Some road")
                            .postTown("some town")
                            .postcode("BT66 7RR")
                            .county("Some county")
                            .country("UK")
                            .build())
                        .email(EmailAddress.builder()
                            .email("Harrykane@hMCTS.net")
                            .build())
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("02838882404")
                            .contactDirection("Sansa Stark")
                            .build())
                        .build())
                    .build())
                .build());
    }

    public static List<Element<Child>> createPopulatedChildren() {
        return ImmutableList.of(
            Element.<Child>builder()
                .id(randomUUID())
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
                .id(randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Sansa")
                        .lastName("Stark")
                        .build())
                    .build())
                .build(),
            Element.<Child>builder()
                .id(randomUUID())
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
                    .id(randomUUID())
                    .value(Direction.builder()
                        .directionType("Test SDO type 1")
                        .directionText("Test body 1")
                        .dateToBeCompletedBy(today)
                        .assignee(ALL_PARTIES)
                        .build())
                    .build(),
                Element.<Direction>builder()
                    .id(randomUUID())
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
                .id(randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Timothy")
                        .lastName("Jones")
                        .relationshipToChild("Father")
                        .build())
                    .build())
                .build(),
            Element.<Respondent>builder()
                .id(randomUUID())
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

    public static Document createDocumentWithAttachedFile() {
        return Document.builder()
            .documentStatus(ATTACHED.getLabel())
            .typeOfDocument(DocumentReference.builder()
                .filename("Mock file")
                .build())
            .build();
    }

    public static List<Element<C21Order>> createC21Orders() {
        return ImmutableList.of(
            Element.<C21Order>builder()
                .value(C21Order.builder()
                    .orderTitle("Example Order")
                    .orderDetails(
                        "Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                    .orderDate(DATE_FORMATTER_SERVICE.formatLocalDateTimeBaseUsingFormat(
                        LocalDateTime.now().plusDays(57), FORMAT_STYLE))
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .legalAdvisorName("Peter Parker")
                        .judgeLastName("Judy")
                        .judgeTitle(HER_HONOUR_JUDGE)
                        .build())
                    .build())
                .build(),
            Element.<C21Order>builder()
                .id(UUID.randomUUID())
                .value(C21Order.builder()
                    .orderTitle(RandomStringUtils.randomAlphabetic(10))
                    .orderDetails(RandomStringUtils.randomAlphabetic(10))
                    .orderDate(DATE_FORMATTER_SERVICE.formatLocalDateTimeBaseUsingFormat(
                        LocalDateTime.now().plusDays(59), FORMAT_STYLE))
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .legalAdvisorName(RandomStringUtils.randomAlphabetic(10))
                        .judgeFullName(RandomStringUtils.randomAlphabetic(10))
                        .judgeLastName(RandomStringUtils.randomAlphabetic(10))
                        .judgeTitle(HER_HONOUR_JUDGE)
                        .build())
                    .document(DocumentReference.builder()
                        .filename(RandomStringUtils.randomAlphabetic(10) + ".pdf")
                        .url("http://" + String.join("/", "dm-store:8080", "documents",
                            UUID.randomUUID().toString()))
                        .binaryUrl("http://" + String.join("/", "dm-store:8080", "documents",
                            UUID.randomUUID().toString(), "binary"))
                        .build())
                    .build())
                .build(),
            Element.<C21Order>builder()
                .id(UUID.randomUUID())
                .value(C21Order.builder()
                    .orderTitle(RandomStringUtils.randomAlphabetic(10))
                    .orderDetails(RandomStringUtils.randomAlphabetic(10))
                    .orderDate(DATE_FORMATTER_SERVICE.formatLocalDateTimeBaseUsingFormat(
                        LocalDateTime.now().plusDays(56), FORMAT_STYLE))
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .legalAdvisorName(RandomStringUtils.randomAlphabetic(10))
                        .judgeFullName(RandomStringUtils.randomAlphabetic(10))
                        .judgeLastName(RandomStringUtils.randomAlphabetic(10))
                        .judgeTitle(HER_HONOUR_JUDGE)
                        .build())
                    .document(DocumentReference.builder()
                        .filename(RandomStringUtils.randomAlphabetic(10) + ".pdf")
                        .url("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079")
                        .binaryUrl("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary")
                        .build())
                    .build())
                .build());
    }

    public static List<Element<HearingBooking>> createHearingBookings(final LocalDate date) {
        return ImmutableList.of(Element.<HearingBooking>builder()
            .id(randomUUID())
            .value(createHearingBooking(date))
            .build());
    }
}
