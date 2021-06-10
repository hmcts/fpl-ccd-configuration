package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionTemplate;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
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
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence.REMOTE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisJudge;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;

class CaseDataExtractionServiceTest {
    private final CaseDataExtractionService service = new CaseDataExtractionService(
        new LookupTestConfig().courtLookupConfiguration(),
        new HearingVenueLookUpService(new ObjectMapper())
    );
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

        assertThat(hearingDate.orElse("")).isEqualTo("11 December 2020");
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
    void shouldReturnAFormattedDateWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String prehearingAttendance = service.extractPrehearingAttendance(hearingBooking);

        assertThat(prehearingAttendance).isEqualTo("11 December 2020, 2:30pm");
    }

    @Test
    void shouldReturnAFormattedTimeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String prehearingAttendance = service.extractPrehearingAttendance(hearingBooking);

        assertThat(prehearingAttendance).isEqualTo("2:30pm");
    }

    @Test
    void shouldReturnCourtNameWhenValidLocalAuthority() {
        assertThat(service.getCourtName(LOCAL_AUTHORITY_1_CODE)).isEqualTo(DEFAULT_LA_COURT);
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

    @Test
    void shouldReturnApplicantNameWhenListOfApplicants() {

        List<Element<Applicant>> listApplicants = wrapElements(Applicant.builder()
            .party(ApplicantParty.builder().organisationName(LOCAL_AUTHORITY_1_NAME).build())
            .build());

        assertThat(service.getApplicantName(listApplicants)).isEqualTo(LOCAL_AUTHORITY_1_NAME);
    }

    @Test
    void shouldReturnEmptyStringForApplicantNameWhenNoApplicantName() {
        List<Element<Applicant>> listApplicants = wrapElements(Applicant.builder().build());

        assertThat(service.getApplicantName(listApplicants)).isEqualTo(StringUtils.EMPTY);
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
    void shouldGetHearingBookingDataWhenHearingBookingEndingOnSameDay() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        DocmosisHearingBooking expectedHearing = getExpectedHearingBooking(
            "11 December 2020", "3:30pm - 4:30pm", "2:30pm"
        );

        assertThat(service.getHearingBookingData(hearingBooking)).isEqualTo(expectedHearing);
    }

    @Test
    void shouldGetHearingBookingDataWhenHearingBookingWhenEndingOnDifferentDay() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        DocmosisHearingBooking expectedHearing = getExpectedHearingBooking(
            StringUtils.EMPTY, "11 December, 3:30pm - 12 December, 4:30pm", "11 December 2020, 2:30pm"
        );

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
            .presence(REMOTE)
            .build();

        DocmosisHearingBooking expectedHearing = getExpectedRemoteHearingBooking(
            "11 December 2020", "3:30pm - 4:30pm", "2:30pm"
        );

        assertThat(service.getHearingBookingData(hearingBooking)).isEqualTo(expectedHearing);
    }

    @Test
    void shouldReturnRemoteVenueInstructionsWhenHearingIsRemoteWithUnknownVenue() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .presence(REMOTE)
            .venue("OTHER")
            .venueCustomAddress(Address.builder().addressLine1("some building").addressLine2("somewhere").build())
            .build();

        DocmosisHearingBooking expectedHearing = getExpectedRemoteHearingBooking(
            "11 December 2020", "3:30pm - 4:30pm", "2:30pm", "some building"
        );

        DocmosisHearingBooking hearingBookingData = service.getHearingBookingData(hearingBooking);
        assertThat(hearingBookingData).isEqualTo(expectedHearing);
    }

    @Test
    void shouldReturnRemoteVenueInstructionsWhenHearingIsRemoteWithUnknownPreviousVenue() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .presence(REMOTE)
            .venue("OTHER")
            .customPreviousVenue("some building, somewhere")
            .previousHearingVenue(PreviousHearingVenue.builder()
                .previousVenue("some building, somewhere")
                .usePreviousVenue("Yes")
                .build())
            .build();

        DocmosisHearingBooking expectedHearing = getExpectedRemoteHearingBooking(
            "11 December 2020", "3:30pm - 4:30pm", "2:30pm", "some building"
        );

        DocmosisHearingBooking hearingBookingData = service.getHearingBookingData(hearingBooking);
        assertThat(hearingBookingData).isEqualTo(expectedHearing);
    }

    @Test
    void shouldReturnRemoteVenueInstructionsWhenHearingIsInPersonWithUnknownPreviousVenue() {
        hearingBooking = createHearingBookingWithTimesOnSameDay().toBuilder()
            .presence(IN_PERSON)
            .venue("OTHER")
            .customPreviousVenue("some building, somewhere")
            .previousHearingVenue(PreviousHearingVenue.builder()
                .previousVenue("some building, somewhere")
                .usePreviousVenue("Yes")
                .build())
            .build();

        DocmosisHearingBooking expectedHearing = getExpectedHearingBooking(
            "11 December 2020", "3:30pm - 4:30pm", "2:30pm", "some building, somewhere"
        );

        DocmosisHearingBooking hearingBookingData = service.getHearingBookingData(hearingBooking);
        assertThat(hearingBookingData).isEqualTo(expectedHearing);
    }

    @Test
    void shouldBuildBaseDirectionWithCorrectIndexAndConfig() {
        String title = "Example title";

        StandardDirectionTemplate direction = getDirection(title);

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

        StandardDirectionTemplate direction = getDirection(title);

        assertThat(service.baseDirection(direction, 1))
            .usingRecursiveComparison()
            .isEqualTo(expectedDirection(title));
    }

    @Test
    void shouldBuildBaseDirectionAndTrimDirectionTextStringWhenWhitespace() {
        String title = "Example title";
        String directionText = " " + "Example description" + " ";

        StandardDirectionTemplate direction = getDirection(title, directionText);

        assertThat(service.baseDirection(direction, 1))
            .usingRecursiveComparison()
            .isEqualTo(expectedDirection(title));
    }

    @Test
    void shouldBuildBaseDirectionWithUnknownValueWhenNoDirectionCompleteByDate() {
        StandardDirectionTemplate direction = getDirectionWithNoCompleteByDate();

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

    private DocmosisDirection.Builder expectedDirection(String title) {
        return DocmosisDirection.builder()
            .assignee(DirectionAssignee.LOCAL_AUTHORITY)
            .title("1. " + title + " by 10:00am, 1 January 2099")
            .body("Example description");
    }

    private StandardDirectionTemplate getDirection(String title) {
        return getDirection(title, "Example description");
    }

    private StandardDirectionTemplate getDirection(String title, String directionText) {
        return StandardDirectionTemplate.builder()
            .assignee(DirectionAssignee.LOCAL_AUTHORITY)
            .directionType(title)
            .directionText(directionText)
            .dateToBeCompletedBy(LocalDateTime.of(2099, 1, 1, 10, 0, 0))
            .build();
    }

    private StandardDirectionTemplate getDirectionWithNoCompleteByDate() {
        return StandardDirectionTemplate.builder()
            .assignee(DirectionAssignee.LOCAL_AUTHORITY)
            .directionType("Example title")
            .directionText("Example description")
            .build();
    }

    private DocmosisHearingBooking getExpectedRemoteHearingBooking(String date, String time, String attendance) {
        return getExpectedRemoteHearingBooking(date, time, attendance, "Venue");
    }

    private DocmosisHearingBooking getExpectedRemoteHearingBooking(String date, String time, String attendance,
                                                                   String venue) {
        return getExpectedHearingBooking(
            date, time, attendance,
            format("Remote hearing at %s. Link and instructions will be sent by the local court.", venue)
        );
    }

    private DocmosisHearingBooking getExpectedHearingBooking(String date, String time, String attendance) {
        return getExpectedHearingBooking(
            date, time, attendance, "Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW"
        );
    }

    private DocmosisHearingBooking getExpectedHearingBooking(String date, String time,
                                                             String attendance, String venue) {
        return DocmosisHearingBooking.builder()
            .hearingDate(date)
            .hearingTime(time)
            .preHearingAttendance(attendance)
            .hearingVenue(venue)
            .hearingJudgeTitleAndName("Her Honour Judge Law")
            .hearingLegalAdvisorName("Peter Parker")
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
