package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
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
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
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
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;

public class CaseDataGeneratorHelper {

    private static final DateFormatterService DATE_FORMATTER_SERVICE = new DateFormatterService();
    private static final String FORMAT_STYLE = "h:mma, d MMMM yyyy";

    private CaseDataGeneratorHelper() {
        // NO-OP
    }

    public static HearingBooking createHearingBooking(LocalDateTime startDate, LocalDateTime endDate) {
        return HearingBooking.builder()
            .startDate(startDate)
            .venue("Venue")
            .endDate(endDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Law")
                .legalAdvisorName("Peter Parker")
                .build())
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

    public static Others createOthers() {
        return Others.builder()
            .firstOther(Other.builder()
                .birthplace("Newry")
                .childInformation("Child suffers from ADD")
                .DOB("02/02/05")
                .gender("Male")
                .name("Kyle Stafford")
                .telephone("02838882404")
                .address(Address.builder()
                    .addressLine1("1 Some street")
                    .addressLine2("Some road")
                    .postTown("some town")
                    .postcode("BT66 7RR")
                    .county("Some county")
                    .country("UK")
                    .build())
                .build())
            .additionalOthers(ImmutableList.of(
                Element.<Other>builder()
                    .value(Other.builder()
                        .birthplace("Craigavon")
                        .DOB("02/02/05")
                        .gender("Female")
                        .name("Sarah Simpson")
                        .telephone("02838882404")
                        .address(Address.builder()
                            .addressLine1("1 Some street")
                            .addressLine2("Some road")
                            .postTown("some town")
                            .postcode("BT66 7RR")
                            .county("Some county")
                            .country("UK")
                            .build())
                        .build())
                    .build()
            )).build();
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
                    .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Peter Parker",
                        "Judy", null, HER_HONOUR_JUDGE))
                    .build())
                .build(),
            Element.<C21Order>builder()
                .id(UUID.randomUUID())
                .value(C21Order.builder()
                    .orderTitle("Winter is here")
                    .orderDetails("Westeros")
                    .orderDate(DATE_FORMATTER_SERVICE.formatLocalDateTimeBaseUsingFormat(
                        LocalDateTime.now().plusDays(59), FORMAT_STYLE))
                    .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Baratheon",
                        "Tyrion Lannister", "Lannister", HIS_HONOUR_JUDGE))
                    .document(createDocumentReference(randomUUID().toString()))
                    .build())
                .build(),
            Element.<C21Order>builder()
                .id(UUID.randomUUID())
                .value(C21Order.builder()
                    .orderTitle("Black Sails")
                    .orderDetails("Long John Silver")
                    .orderDate(DATE_FORMATTER_SERVICE.formatLocalDateTimeBaseUsingFormat(
                        LocalDateTime.now().plusDays(60), FORMAT_STYLE))
                    .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Edward Teach",
                        "Captain Flint", "Scott", DEPUTY_DISTRICT_JUDGE))
                    .document(DocumentReference.builder()
                        .filename("C21 3.pdf")
                        .url("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079")
                        .binaryUrl("http://dm-store:8080/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary")
                        .build())
                    .build())
                .build());
    }

    public static List<Element<HearingBooking>> createHearingBookings(final LocalDateTime startDate,
                                                                      final LocalDateTime endDate) {
        return ImmutableList.of(Element.<HearingBooking>builder()
            .id(randomUUID())
            .value(createHearingBooking(startDate, endDate))
            .build());
    }

    public static DocumentReference createDocumentReference(final String id) {
        final String documentUrl = "http://" + String.join("/", "dm-store:8080", "documents", id);
        return DocumentReference.builder()
            .filename(RandomStringUtils.randomAlphabetic(12) + ".pdf")
            .url(documentUrl)
            .binaryUrl(documentUrl + "/binary")
            .build();
    }

    public static JudgeAndLegalAdvisor createJudgeAndLegalAdvisor(final String legalAdvisorName,
                                                                  final String judgeFullName,
                                                                  final String judgeLastName,
                                                                  final JudgeOrMagistrateTitle judgeTitle) {
        return JudgeAndLegalAdvisor.builder()
            .legalAdvisorName(legalAdvisorName)
            .judgeLastName(judgeLastName)
            .judgeFullName(judgeFullName)
            .judgeTitle(judgeTitle)
            .build();
    }

    public static List<Element<Direction>> createElementCollection(Direction direction) {
        return ImmutableList.of(
            Element.<Direction>builder()
                .value(direction)
                .build()
        );
    }

    public static Direction createDirection(DirectionAssignee assignee) {
        return Direction.builder()
            .directionText("Mock direction text")
            .assignee(assignee)
            .build();
    }

    public static Direction createCustomDirection(DirectionAssignee assignee) {
        return Direction.builder()
            .directionText("Mock direction text")
            .assignee(assignee)
            .readOnly("No")
            .custom("Yes")
            .build();
    }

    public static Direction createUnassignedDirection() {
        return createDirection(null);
    }

    public static List<Element<Direction>> createCmoDirections() {
        return ImmutableList.of(
            Element.<Direction>builder()
                .value(createCustomDirection(ALL_PARTIES))
                .build(),
            Element.<Direction>builder()
                .value(createCustomDirection(LOCAL_AUTHORITY))
                .build(),
            Element.<Direction>builder()
                .value(createCustomDirection(CAFCASS))
                .build(),
            Element.<Direction>builder()
                .value(createCustomDirection(COURT))
                .build(),
            Element.<Direction>builder()
                .value(createCustomDirection(PARENTS_AND_RESPONDENTS))
                .build(),
            Element.<Direction>builder()
                .value(createCustomDirection(OTHERS))
                .build());
    }
}
