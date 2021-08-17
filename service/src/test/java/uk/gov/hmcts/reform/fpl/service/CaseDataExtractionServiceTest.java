package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalTime.NOON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.PHONE;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.VIDEO;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.DEFAULT_PRE_ATTENDANCE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisJudge;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;

@ExtendWith(MockitoExtension.class)
class CaseDataExtractionServiceTest {

    @Mock
    private CourtService courtService;

    @Spy
    private HearingVenueLookUpService hearingVenueLookUpService = new HearingVenueLookUpService(new ObjectMapper());

    @InjectMocks
    private CaseDataExtractionService service;

    private HearingBooking hearingBooking;

    @Test
    void shouldReturnAnEmptyStringWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        Optional<String> hearingDate = service.getHearingDateIfHearingsOnSameDay(hearingBooking);

        assertThat(hearingDate).isEmpty();
    }

    @Test
    void shouldReturnTheFormattedDateWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        Optional<String> hearingDate = service.getHearingDateIfHearingsOnSameDay(hearingBooking);

        assertThat(hearingDate).contains("11 December 2020");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWithDatesWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String hearingTime = service.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("11 December, 3:30pm - 12 December, 4:30pm");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String hearingTime = service.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("3:30pm - 4:30pm");
    }

    @Test
    void shouldReturnCourtNameWhenValidLocalAuthority() {
        final CaseData caseData = CaseData.builder().build();

        when(courtService.getCourtName(caseData)).thenReturn(DEFAULT_LA_COURT);

        assertThat(service.getCourtName(caseData)).isEqualTo(DEFAULT_LA_COURT);
    }

    @Test
    void shouldReturnChildrenDetailsWhenPopulatedChildren() {
        List<Element<Child>> listChildren = wrapElements(Child.builder()
            .party(ChildParty.builder()
                .firstName("John")
                .lastName("Smith")
                .gender("Male")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .build())
            .build());

        DocmosisChild expectedChild = DocmosisChild.builder()
            .name("John Smith")
            .gender("Male")
            .dateOfBirth("1 January 2020")
            .build();

        assertThat(service.getChildrenDetails(listChildren)).containsOnly(expectedChild);
    }

    @Test
    void shouldReturnChildrenDetailsWhenPopulatedChildrenDoesNotHaveDob() {
        List<Element<Child>> listChildren = wrapElements(Child.builder()
            .party(ChildParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build());

        DocmosisChild expectedChild = DocmosisChild.builder()
            .name("John Smith")
            .dateOfBirth(null)
            .build();

        assertThat(service.getChildrenDetails(listChildren)).containsOnly(expectedChild);
    }

    @Nested
    class ApplicantName {

        @Test
        void shouldGetApplicantNameFromLocalAuthorityIfPresent() {

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .name("Local authority organisation")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority))
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder().organisationName("Applicant organisation").build())
                    .build()))
                .build();

            assertThat(service.getApplicantName(caseData)).isEqualTo("Local authority organisation");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyApplicantNameWhenLocalAuthorityNameIsMissing(String localAuthorityName) {

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .name(localAuthorityName)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority))
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder().organisationName("Applicant organisation").build())
                    .build()))
                .build();

            assertThat(service.getApplicantName(caseData)).isEmpty();
        }

        @Test
        void shouldGetApplicantNameFromLegacyApplicantWhenNoLocalAuthority() {

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder().organisationName(LOCAL_AUTHORITY_1_NAME).build())
                    .build()))
                .build();

            assertThat(service.getApplicantName(caseData)).isEqualTo(LOCAL_AUTHORITY_1_NAME);
        }

        @Test
        void shouldReturnEmptyStringForApplicantNameWhenNoApplicantName() {

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().build()))
                .build();

            assertThat(service.getApplicantName(caseData)).isEqualTo(StringUtils.EMPTY);
        }
    }

    @Test
    void shouldGetRespondentsNameAndRelationshipWhenListOfRespondents() {
        List<Element<Respondent>> listRespondents = wrapElements(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .relationshipToChild("Father")
                .build())
            .build());

        DocmosisRespondent expectedRespondent = DocmosisRespondent.builder()
            .name("John Smith")
            .relationshipToChild("Father")
            .build();

        assertThat(service.getRespondentsNameAndRelationship(listRespondents)).containsOnly(expectedRespondent);
    }

    @Test
    void shouldGetJudgeAndLegalAdvisorWhenFullyPopulated() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getJudgeAndLegalAdvisor();

        DocmosisJudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName("His Honour Judge Smith")
            .legalAdvisorName("Jane Doe")
            .build();

        assertThat(service.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor)).isEqualTo(expectedJudgeAndLegalAdvisor);
    }

    @Test
    void shouldGetJudgeAndLegalAdvisorWhenNoInformationEntered() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        DocmosisJudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName(StringUtils.EMPTY)
            .legalAdvisorName(StringUtils.EMPTY)
            .build();

        assertThat(service.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor)).isEqualTo(expectedJudgeAndLegalAdvisor);
    }

    @Test
    void shouldGetDefaultPreAttendance() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .attendance(List.of(VIDEO))
            .attendanceDetails(null)
            .preAttendanceDetails(null)
            .startDate(LocalDateTime.of(2020, 12, 11, 15, 30))
            .endDate(LocalDateTime.of(2020, 12, 11, 16, 30))
            .build();

        DocmosisHearingBooking expectedHearing = DocmosisHearingBooking.builder()
            .hearingDate("11 December 2020")
            .hearingTime("3:30pm - 4:30pm")
            .hearingAttendance("Remote - video call")
            .preHearingAttendance(DEFAULT_PRE_ATTENDANCE)
            .hearingVenue("Remote hearing at Venue. Details and instructions will be sent by the local court.")
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getEndDate(), DATE_TIME))
            .build();

        assertThat(service.getHearingBookingData(hearingBooking)).isEqualTo(expectedHearing);
    }

    @Test
    void shouldGetHearingBookingDataWhenHearingBookingEndingOnSameDay() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .attendance(List.of(VIDEO))
            .attendanceDetails(null)
            .preAttendanceDetails("10 minutes before hearing")
            .startDate(LocalDateTime.of(2020, 12, 11, 15, 30))
            .endDate(LocalDateTime.of(2020, 12, 11, 16, 30))
            .build();

        DocmosisHearingBooking expectedHearing = DocmosisHearingBooking.builder()
            .hearingDate("11 December 2020")
            .hearingTime("3:30pm - 4:30pm")
            .hearingAttendance("Remote - video call")
            .preHearingAttendance("10 minutes before hearing")
            .hearingVenue("Remote hearing at Venue. Details and instructions will be sent by the local court.")
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getEndDate(), DATE_TIME))
            .build();

        assertThat(service.getHearingBookingData(hearingBooking)).isEqualTo(expectedHearing);
    }

    @Test
    void shouldGetHearingBookingDataWhenHearingBookingWhenEndingOnDifferentDay() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .attendance(List.of(PHONE))
            .attendanceDetails("+44 777 777 777")
            .startDate(LocalDateTime.of(2020, 12, 11, 15, 30))
            .endDate(LocalDateTime.of(2020, 12, 12, 16, 30))
            .build();

        DocmosisHearingBooking expectedHearing = DocmosisHearingBooking.builder()
            .hearingDate("")
            .hearingTime("11 December, 3:30pm - 12 December, 4:30pm")
            .hearingAttendance("Remote - phone call")
            .hearingAttendanceDetails("+44 777 777 777")
            .preHearingAttendance("30 minutes before the hearing")
            .hearingVenue("Remote hearing at Venue. Details and instructions will be sent by the local court.")
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getEndDate(), DATE_TIME))
            .build();

        assertThat(service.getHearingBookingData(hearingBooking)).isEqualTo(expectedHearing);
    }

    @Test
    void shouldGetEmptyHearingBookingWhenNoHearing() {
        assertThat(service.getHearingBookingData(null))
            .isEqualTo(DocmosisHearingBooking.builder().build());
    }

    @Test
    void shouldReturnRemoteVenueInstructionsWhenHearingIsRemoteWithKnownVenue() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .attendance(List.of(VIDEO))
            .attendanceDetails("Join: https://remote-hearing.gov.uk/1")
            .build();

        DocmosisHearingBooking expectedHearing = DocmosisHearingBooking.builder()
            .hearingDate("11 December 2020")
            .hearingTime("3:30pm - 4:30pm")
            .hearingAttendance("Remote - video call")
            .hearingAttendanceDetails("Join: https://remote-hearing.gov.uk/1")
            .preHearingAttendance("30 minutes before the hearing")
            .hearingVenue("Remote hearing at Venue. Details and instructions will be sent by the local court.")
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getEndDate(), DATE_TIME))
            .build();

        assertThat(service.getHearingBookingData(hearingBooking)).isEqualTo(expectedHearing);
    }

    @Test
    void shouldReturnRemoteVenueInstructionsWhenHearingIsRemoteWithUnknownVenue() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .attendance(List.of(VIDEO))
            .attendanceDetails(null)
            .venue("OTHER")
            .venueCustomAddress(Address.builder().addressLine1("some building").addressLine2("somewhere").build())
            .build();

        DocmosisHearingBooking expectedHearing = DocmosisHearingBooking.builder()
            .hearingDate("11 December 2020")
            .hearingTime("3:30pm - 4:30pm")
            .hearingAttendance("Remote - video call")
            .preHearingAttendance("30 minutes before the hearing")
            .hearingVenue("Remote hearing at some building. Details and instructions will be sent by the local court.")
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getEndDate(), DATE_TIME))
            .build();

        DocmosisHearingBooking hearingBookingData = service.getHearingBookingData(hearingBooking);
        assertThat(hearingBookingData).isEqualTo(expectedHearing);
    }

    @Test
    void shouldReturnRemoteVenueInstructionsWhenHearingIsRemoteWithUnknownPreviousVenue() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .attendance(List.of(PHONE, VIDEO))
            .attendanceDetails("Phone: +44 777 777 777, Video: https://remote-hearing.gov.uk/1")
            .venue("OTHER")
            .customPreviousVenue("some building, somewhere")
            .previousHearingVenue(PreviousHearingVenue.builder()
                .previousVenue("some building, somewhere")
                .usePreviousVenue("Yes")
                .build())
            .build();

        DocmosisHearingBooking expectedHearing = DocmosisHearingBooking.builder()
            .hearingDate("11 December 2020")
            .hearingTime("3:30pm - 4:30pm")
            .hearingAttendance("Remote - phone call, remote - video call")
            .hearingAttendanceDetails("Phone: +44 777 777 777, Video: https://remote-hearing.gov.uk/1")
            .preHearingAttendance("30 minutes before the hearing")
            .hearingVenue("Remote hearing at some building. Details and instructions will be sent by the local court.")
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getEndDate(), DATE_TIME))
            .build();

        DocmosisHearingBooking hearingBookingData = service.getHearingBookingData(hearingBooking);
        assertThat(hearingBookingData).isEqualTo(expectedHearing);
    }

    @Test
    void shouldReturnRemoteVenueInstructionsWhenHearingIsInPersonWithUnknownPreviousVenue() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .attendance(List.of(IN_PERSON))
            .attendanceDetails("Room: 1")
            .venue("OTHER")
            .customPreviousVenue("some building, somewhere")
            .previousHearingVenue(PreviousHearingVenue.builder()
                .previousVenue("some building, somewhere")
                .usePreviousVenue("Yes")
                .build())
            .build();

        DocmosisHearingBooking expectedHearing = DocmosisHearingBooking.builder()
            .hearingDate("11 December 2020")
            .hearingTime("3:30pm - 4:30pm")
            .hearingAttendance("In person")
            .hearingAttendanceDetails("Room: 1")
            .preHearingAttendance("30 minutes before the hearing")
            .hearingVenue("some building, somewhere")
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
            .hearingStartDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getStartDate(), DATE_TIME))
            .hearingEndDate(formatLocalDateTimeBaseUsingFormat(hearingBooking.getEndDate(), DATE_TIME))
            .build();

        DocmosisHearingBooking hearingBookingData = service.getHearingBookingData(hearingBooking);
        assertThat(hearingBookingData).isEqualTo(expectedHearing);
    }

    @Test
    void shouldBuildBaseDirectionWithCorrectIndexAndConfig() {
        String title = "Example title";

        Direction direction = getDirection(title);

        DirectionConfiguration config = DirectionConfiguration.builder()
            .assignee(DirectionAssignee.LOCAL_AUTHORITY)
            .title(title)
            .display(Display.builder().delta("0").due(Display.Due.BY).templateDateFormat(TIME_DATE).build())
            .build();

        assertThat(service.baseDirection(direction, 1, List.of(config)))
            .usingRecursiveComparison()
            .isEqualTo(expectedDirection(title));
    }

    @Test
    void shouldBuildBaseDirectionWithCorrectIndexWhenNoConfig() {
        String title = "Example title";

        Direction direction = getDirection(title);

        assertThat(service.baseDirection(direction, 1))
            .usingRecursiveComparison()
            .isEqualTo(expectedDirection(title));
    }

    @Test
    void shouldBuildBaseDirectionAndTrimDirectionTextStringWhenWhitespace() {
        String title = "Example title";
        String directionText = " " + "Example description" + " ";

        Direction direction = getDirection(title, directionText);

        assertThat(service.baseDirection(direction, 1))
            .usingRecursiveComparison()
            .isEqualTo(expectedDirection(title));
    }

    @Test
    void shouldBuildBaseDirectionWithUnknownValueWhenNoDirectionCompleteByDate() {
        Direction direction = getDirectionWithNoCompleteByDate();

        assertThat(service.baseDirection(direction, 1))
            .usingRecursiveComparison()
            .isEqualTo(DocmosisDirection.builder()
                .assignee(DirectionAssignee.LOCAL_AUTHORITY)
                .title("1. Example title")
                .body("Example description"));
    }

    @Test
    void shouldReturnExpectedDocmosisJudgeWhenJudgeAndLegalAdvisorGiven() {
        assertThat(service.getAllocatedJudge(JudgeAndLegalAdvisor.from(testJudge())))
            .isEqualTo(testDocmosisJudge());
    }

    @Nested
    class FormatHearingAttendance {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNullWhenAttendanceNotSpecified(List<HearingAttendance> attendance) {
            final HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(attendance)
                .build();

            assertThat(service.getHearingAttendance(hearingBooking)).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(HearingAttendance.class)
        void shouldReturnFormattedAttendanceWhenOnlyOneAttendancesSpecified(HearingAttendance attendance) {
            final HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(List.of(attendance))
                .build();

            assertThat(service.getHearingAttendance(hearingBooking)).contains(attendance.getLabel());
        }

        @Test
        void shouldReturnFormattedAttendanceWhenMultipleAttendancesSpecified() {
            final HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(List.of(IN_PERSON, VIDEO, PHONE))
                .build();

            assertThat(service.getHearingAttendance(hearingBooking))
                .contains("In person, remote - video call, remote - phone call");
        }
    }

    private DocmosisDirection.Builder expectedDirection(String title) {
        return DocmosisDirection.builder()
            .assignee(DirectionAssignee.LOCAL_AUTHORITY)
            .title("1. " + title + " by 10:00am, 1 January 2099")
            .body("Example description");
    }

    private Direction getDirection(String title) {
        return getDirection(title, "Example description");
    }

    private Direction getDirection(String title, String directionText) {
        return Direction.builder()
            .assignee(DirectionAssignee.LOCAL_AUTHORITY)
            .directionType(title)
            .directionText(directionText)
            .dateToBeCompletedBy(LocalDateTime.of(2099, 1, 1, 10, 0, 0))
            .build();
    }

    private Direction getDirectionWithNoCompleteByDate() {
        return Direction.builder()
            .assignee(DirectionAssignee.LOCAL_AUTHORITY)
            .directionType("Example title")
            .directionText("Example description")
            .build();
    }

    private JudgeAndLegalAdvisor getJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeLastName("Smith")
            .judgeTitle(HIS_HONOUR_JUDGE)
            .legalAdvisorName("Jane Doe")
            .build();
    }

    private HearingBooking createHearingBookingWithTimesOnSameDay() {
        return createHearingBooking(LocalDateTime.of(2020, 12, 11, 15, 30),
            LocalDateTime.of(2020, 12, 11, 16, 30));
    }

    private HearingBooking createHearingBookingWithTimesOnDifferentDays() {
        return createHearingBooking(LocalDateTime.of(2020, 12, 11, 15, 30),
            LocalDateTime.of(2020, 12, 12, 16, 30));
    }
}
