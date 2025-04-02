package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CaseDataGeneratorHelper {

    private static final String TELEPHONE = "02838882404";

    private CaseDataGeneratorHelper() {
        // NO-OP
    }

    public static HearingBooking createHearingBooking(LocalDateTime startDate, LocalDateTime endDate) {
        return HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(startDate)
            .venue("Venue")
            .endDate(endDate)
            .attendance(List.of(IN_PERSON))
            .attendanceDetails("Room: 123")
            .preAttendanceDetails("30 minutes before the hearing")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Law")
                .legalAdvisorName("Peter Parker")
                .build())
            .noticeOfHearing(DocumentReference.builder()
                .filename("fileName")
                .binaryUrl("binary_url")
                .url("www.url.com")
                .build())
            .build();
    }

    public static HearingBooking createHearingBooking(LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      HearingType hearingType,
                                                      UUID cmoID) {
        return createHearingBooking(startDate, endDate).toBuilder()
            .type(hearingType)
            .caseManagementOrderId(cmoID)
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
                        .address(address())
                        .email(EmailAddress.builder()
                            .email("BranStark@hMCTS.net")
                            .build())
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber(TELEPHONE)
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
                        .address(address())
                        .email(EmailAddress.builder()
                            .email("Harrykane@hMCTS.net")
                            .build())
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber(TELEPHONE)
                            .contactDirection("Sansa Stark")
                            .build())
                        .build())
                    .build())
                .build());
    }

    public static List<Element<Child>> createPopulatedChildren(LocalDate dateOfBirth) {
        return ElementUtils.wrapElements(
            Child.builder()
                .party(ChildParty.builder()
                    .firstName("Bran")
                    .lastName("Stark")
                    .gender(ChildGender.BOY)
                    .dateOfBirth(dateOfBirth)
                    .build())
                .build(),
            Child.builder()
                .party(ChildParty.builder()
                    .firstName("Sansa")
                    .lastName("Stark")
                    .gender(ChildGender.BOY)
                    .dateOfBirth(dateOfBirth)
                    .build())
                .build(),
            Child.builder()
                .party(ChildParty.builder()
                    .firstName("Jon")
                    .lastName("Snow")
                    .gender(ChildGender.GIRL)
                    .dateOfBirth(dateOfBirth)
                    .build())
                .build()
        );
    }

    public static StandardDirectionOrder createStandardDirectionOrders(LocalDateTime today, OrderStatus status) {
        return StandardDirectionOrder.builder()
            .dateOfIssue("29 November 2019")
            .directions(wrapElements(Direction.builder()
                    .directionType("Test SDO type 1")
                    .directionText("Test body 1")
                    .directionNeeded(YES.getValue())
                    .dateToBeCompletedBy(today)
                    .assignee(ALL_PARTIES)
                    .build(),
                Direction.builder()
                    .directionType("Test SDO type 2")
                    .directionText("Test body 2")
                    .directionNeeded(YES.getValue())
                    .dateToBeCompletedBy(today)
                    .assignee(ALL_PARTIES)
                    .build(),
                Direction.builder()
                    .directionNeeded(NO.getValue())
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
        return wrapElements(
            Respondent.builder().party(
                RespondentParty.builder()
                    .firstName("Timothy")
                    .lastName("Jones")
                    .relationshipToChild("Father")
                    .build())
                .build(),
            Respondent.builder().party(
                RespondentParty.builder()
                    .firstName("Sarah")
                    .lastName("Simpson")
                    .relationshipToChild("Mother")
                    .build())
                .build()
        );
    }

    public static Document createDocumentWithStatus(DocumentStatus status) {
        return Document.builder()
            .documentStatus(ofNullable(status).map(DocumentStatus::getLabel).orElse(null))
            .typeOfDocument(DocumentReference.builder()
                .filename("Mock file")
                .build())
            .build();
    }

    public static List<Element<Other>> createOthers() {
        return createOthers(randomUUID());
    }

    public static List<Element<Other>> createOthers(UUID otherPerson1Uuid) {
        return List.of(
            element(Other.builder()
                .childInformation("Child suffers from ADD")
                .dateOfBirth("2005-06-04")
                .firstName("Kyle Stafford")
                .telephone(TELEPHONE)
                .address(address())
                .build()),
            element(otherPerson1Uuid, Other.builder()
                .dateOfBirth("2002-02-05")
                .firstName("Sarah Simpson")
                .telephone(TELEPHONE)
                .address(address())
                .build()));
    }

    public static DynamicList buildDynamicListFromOthers(List<Element<Other>> others, int selected) {
        List<DynamicListElement> listItems = others.stream()
            .map(otherElement -> DynamicListElement.builder()
                .code(otherElement.getId())
                .label(otherElement.getValue().getFullName())
                .build())
            .toList();
        return DynamicList.builder()
            .listItems(listItems)
            .value(listItems.get(selected))
            .build();
    }

    public static List<Element<GeneratedOrder>> createOrders(DocumentReference lastOrderDocumentReference) {
        final String orderType = "Blank order (C21)";
        return ElementUtils.wrapElements(
            GeneratedOrder.builder()
                .others(List.of(element(Other.builder()
                    .address(Address.builder().build())
                    .build())))
                .type(orderType)
                .title("Example Order")
                .details(
                    "Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                .date(formatLocalDateTimeBaseUsingFormat(LocalDateTime.now().plusDays(57), TIME_DATE))
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Peter Parker",
                    "Judy", null, HER_HONOUR_JUDGE))
                .build(),
            GeneratedOrder.builder()
                .type(orderType)
                .title("Winter is here")
                .details("Westeros")
                .date(formatLocalDateTimeBaseUsingFormat(LocalDateTime.now().plusDays(59), TIME_DATE))
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Baratheon",
                    "Tyrion Lannister", "Lannister", HIS_HONOUR_JUDGE))
                .document(createDocumentReference(randomUUID().toString()))
                .build(),
            GeneratedOrder.builder()
                .type(orderType)
                .title("Black Sails")
                .details("Long John Silver")
                .date(formatLocalDateTimeBaseUsingFormat(LocalDateTime.now().plusDays(60), TIME_DATE))
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor("Edward Teach",
                    "Captain Flint", "Scott", DEPUTY_DISTRICT_JUDGE))
                .document(lastOrderDocumentReference)
                .build()
        );
    }

    public static List<Element<HearingBooking>> createHearingBookings(final LocalDateTime startDate,
                                                                      final LocalDateTime endDate) {
        return ImmutableList.of(Element.<HearingBooking>builder()
            .id(randomUUID())
            .value(createHearingBooking(startDate, endDate))
            .build());
    }

    public static List<Element<HearingBooking>> createHearingBookingsFromInitialDate(LocalDateTime date) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .value(createHearingBooking(date.plusDays(5), date.plusDays(6)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .value(createHearingBooking(date.plusDays(2), date.plusDays(3)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                .value(createHearingBooking(date, date.plusDays(1)))
                .build()
        );
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

    public static List<Element<Representative>> createRepresentatives(
        RepresentativeServingPreferences servingPreferences) {
        return wrapElements(Representative.builder()
            .email("abc@example.com")
            .fullName("Jon Snow")
            .servingPreferences(servingPreferences)
            .build());
    }

    private static Address address() {
        return Address.builder()
            .addressLine1("1 Some street")
            .addressLine2("Some road")
            .postTown("some town")
            .postcode("BT66 7RR")
            .county("Some county")
            .country("UK")
            .build();
    }
}
