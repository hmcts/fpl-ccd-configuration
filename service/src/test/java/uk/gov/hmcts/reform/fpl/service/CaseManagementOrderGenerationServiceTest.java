package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRecital;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentative;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentedBy;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_1;
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_2;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_4;
import static uk.gov.hmcts.reform.fpl.service.StandardDirectionOrderGenerationService.DEFAULT;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildCaseDataForCMODocmosisGeneration;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderGenerationService.class})
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, DraftCMOService.class, CommonCaseDataExtractionService.class,
    CommonDirectionService.class, HearingVenueLookUpService.class, HearingBookingService.class,
    JsonOrdersLookupService.class, StandardDirectionOrderGenerationService.class, LookupTestConfig.class,
    FixedTimeConfiguration.class
})
class CaseManagementOrderGenerationServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String COMPLETION_DATE_AND_TIME = "by 10:00am, 1 January 2099";

    @Autowired
    private CaseManagementOrderGenerationService service;

    @Test
    void shouldReturnEmptyMapValuesWhenCaseDataIsEmpty() {
        DocmosisCaseManagementOrder templateData = service.getTemplateData(baseCaseData().build());

        assertThat(templateData).isEqualToComparingFieldByField(caseManagementOrderWithEmptyFields(templateData));
    }

    @Test
    void directionsShouldFormatAsExpectedWhenMultipleRespondentsAndOthers() {
        DocmosisCaseManagementOrder templateData = service.getTemplateData(caseDataWithDirections());

        assertThat(templateData.getDirections()).containsExactly(getDocmosisDirections());
    }

    @Test
    void directionShouldRemainGroupedByRespondentWhenMultipleDirectionsForDifferentRespondents() {
        List<Element<Direction>> respondentDirections = wrapElements(
            direction(RESPONDENT_4, "Direction title 6"),
            direction(RESPONDENT_1, "Direction title 2"),
            direction(RESPONDENT_2, "Direction title 4"),
            direction(RESPONDENT_1, "Direction title 3"),
            direction(RESPONDENT_2, "Direction title 5"));

        CaseData caseData = caseDataWithRespondentDirections(respondentDirections);

        DocmosisCaseManagementOrder templateData = service.getTemplateData(caseData);

        assertThat(templateData.getDirections()).containsExactly(correctlyOrderedDirections());
    }

    //TODO: this test can probably factor in the above two tests, whoever the buildCaseDataForCMODocmosisGeneration
    // method and the methods it uses internally are heavily intertwined.
    @Test
    void shouldReturnFullyPopulatedMapWhenCompleteCaseDetailsAreProvided() {
        DocmosisCaseManagementOrder templateData = service.getTemplateData(buildCaseDataForCMODocmosisGeneration(NOW));

        //template data needs to be passed in for the draft and court seal image assertions.
        assertThat(templateData).isEqualToComparingFieldByField(expectedCaseManagementOrder(templateData));
    }

    private CaseData.CaseDataBuilder baseCaseData() {
        return CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(emptyList())
            .dateSubmitted(NOW.toLocalDate())
            .respondents1(emptyList())
            .applicants(emptyList())
            .schedule(Schedule.builder().includeSchedule("No").build());
    }

    private CaseData caseDataWithRespondentDirections(List<Element<Direction>> respondentDirections) {
        return baseCaseData()
            .respondentDirectionsCustomCMO(respondentDirections)
            .allPartiesCustomCMO(wrapElements(direction()))
            .build();
    }

    private DocmosisCaseManagementOrder caseManagementOrderWithEmptyFields(DocmosisCaseManagementOrder templateData) {
        return DocmosisCaseManagementOrder.builder()
            .representatives(List.of(DocmosisRepresentative.builder()
                .name(DEFAULT)
                .representedBy(List.of(DocmosisRepresentedBy.builder()
                    .name(DEFAULT)
                    .email(DEFAULT)
                    .phoneNumber(DEFAULT)
                    .build()))
                .build()))
            .schedule(Schedule.builder().includeSchedule("No").build())
            .recitals(emptyList())
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName(DEFAULT)
                .legalAdvisorName("")
                .build())
            .courtName("Family Court")
            .familyManCaseNumber("123")
            .dateOfIssue(formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG))
            .complianceDeadline(formatLocalDateToString(NOW.plusWeeks(26).toLocalDate(), FormatStyle.LONG))
            .respondents(emptyList())
            .children(emptyList())
            .applicantName("")
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate("This will appear on the issued CMO")
                .hearingVenue("This will appear on the issued CMO")
                .preHearingAttendance("This will appear on the issued CMO")
                .hearingTime("This will appear on the issued CMO")
                .build())
            .directions(emptyList())
            .crest(templateData.getCrest())
            .draftbackground(templateData.getDraftbackground())
            .build();
    }

    private DocmosisDirection expectedDirection(DirectionAssignee assignee, String header, String title) {
        return DocmosisDirection.builder()
            .assignee(assignee)
            .header(header)
            .title(title + COMPLETION_DATE_AND_TIME)
            .body("Mock direction text")
            .build();
    }

    private DocmosisDirection[] correctlyOrderedDirections() {
        return new DocmosisDirection[]{
            expectedDirection(ALL_PARTIES, null, "2. Direction title 1 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, "For Respondent 1", "3. Direction title 2 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, null, "4. Direction title 3 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, "For Respondent 2", "5. Direction title 4 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, null, "6. Direction title 5 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, "For Respondent 4", "7. Direction title 6 ")
        };
    }

    private DocmosisDirection[] getDocmosisDirections() {
        return new DocmosisDirection[]{
            expectedDirection(ALL_PARTIES, null, "2. Direction title 1 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, "For Respondent 1", "3. Direction title 2 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, null, "4. Direction title 3 "),
            expectedDirection(PARENTS_AND_RESPONDENTS, "For Respondent 2", "5. Direction title 4 "),
            expectedDirection(OTHERS, "For Person 1", "6. Direction title 5 "),
            expectedDirection(OTHERS, null, "7. Direction title 6 "),
            expectedDirection(OTHERS, "For Other person 1", "8. Direction title 7 ")
        };
    }

    private List<DocmosisDirection> expectedDirections() {
        return List.of(
            expectedDirection(ALL_PARTIES, null, "2. Direction title "),
            expectedDirection(LOCAL_AUTHORITY, null, "3. Direction title "),
            expectedDirection(PARENTS_AND_RESPONDENTS, "For Respondent 1", "4. Direction title "),
            expectedDirection(CAFCASS, null, "5. Direction title "),
            expectedDirection(OTHERS, "For Person 1", "6. Direction title "),
            expectedDirection(COURT, null, "7. Direction title ")
        );
    }

    private CaseData caseDataWithDirections() {
        return baseCaseData()
            .respondentDirectionsCustomCMO(respondentDirections())
            .otherPartiesDirectionsCustomCMO(otherDirections())
            .allPartiesCustomCMO(wrapElements(direction()))
            .build();
    }

    private List<Element<Direction>> respondentDirections() {
        return wrapElements(
            direction(RESPONDENT_1, "Direction title 2"),
            direction(RESPONDENT_1, "Direction title 3"),
            direction(RESPONDENT_2, "Direction title 4"));
    }

    private List<Element<Direction>> otherDirections() {
        return wrapElements(
            direction(OTHER_1, "Direction title 5"),
            direction(OTHER_1, "Direction title 6"),
            direction(OTHER_2, "Direction title 7"));
    }

    private Direction.DirectionBuilder getBaseDirection(String title, Direction.DirectionBuilder builder) {
        return builder
            .directionType(title)
            .directionText("Mock direction text")
            .dateToBeCompletedBy(LocalDateTime.of(2099, 1, 1, 10, 0, 0));
    }

    private Direction direction() {
        return getBaseDirection("Direction title 1", Direction.builder()).build();
    }

    private Direction direction(ParentsAndRespondentsDirectionAssignee specificRespondent, String title) {
        return getBaseDirection(title, Direction.builder().parentsAndRespondentsAssignee(specificRespondent)).build();
    }

    private Direction direction(OtherPartiesDirectionAssignee specificOther, String title) {
        return getBaseDirection(title, Direction.builder().otherPartiesAssignee(specificOther)).build();
    }

    private DocmosisCaseManagementOrder expectedCaseManagementOrder(DocmosisCaseManagementOrder templateData) {
        String hearingDateOnDifferentDays = "";

        return DocmosisCaseManagementOrder.builder()
            .courtName("Family Court")
            .familyManCaseNumber("123")
            .dateOfIssue(formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG))
            .complianceDeadline(formatLocalDateToString(NOW.toLocalDate().plusWeeks(26), FormatStyle.LONG))
            .children(getExpectedChildren())
            .numberOfChildren(getExpectedChildren().size())
            .applicantName("Bran Stark")
            .respondents(getExpectedRespondents())
            .respondentsProvided(true)
            .representatives(getExpectedRepresentatives())
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate(hearingDateOnDifferentDays)
                .hearingVenue("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW")
                .preHearingAttendance(formatLocalDateTimeBaseUsingFormat(NOW.minusHours(1), "d MMMM yyyy, h:mma"))
                .hearingTime(getHearingTime())
                .build())
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("Her Honour Judge Law")
                .legalAdvisorName("Peter Parker")
                .build())
            .directions(expectedDirections())
            .recitals(getExpectedRecital())
            .recitalsProvided(true)
            .schedule(getExpectedSchedule())
            .scheduleProvided(true)
            .crest(templateData.getCrest())
            .draftbackground(templateData.getDraftbackground())
            .courtseal(templateData.getCourtseal())
            .build();
    }

    private List<DocmosisRepresentative> getExpectedRepresentatives() {
        return List.of(
            DocmosisRepresentative.builder()
                .name("Bran Stark")
                .representedBy(List.of(
                    DocmosisRepresentedBy.builder()
                        .name("Bruce Wayne")
                        .email("bruce-wayne@notbatman.com")
                        .phoneNumber("07700900304")
                        .build()))
                .build(),
            DocmosisRepresentative.builder()
                .name("Timothy Jones")
                .representedBy(List.of(
                    DocmosisRepresentedBy.builder()
                        .name("George Rep 1 (TJ)")
                        .email("1TJ@representatives.com")
                        .phoneNumber("+44 79000001")
                        .build(),
                    DocmosisRepresentedBy.builder()
                        .name("George Rep 2 (TJ)")
                        .email("2TJ@representatives.com")
                        .phoneNumber("+44 79000002")
                        .build()))
                .build(),
            DocmosisRepresentative.builder()
                .name("Sarah Simpson")
                .representedBy(List.of(
                    DocmosisRepresentedBy.builder()
                        .name("George Rep 1 (SS)")
                        .email("1SS@representatives.com")
                        .phoneNumber("+44 79000001")
                        .build()))
                .build(),
            DocmosisRepresentative.builder()
                .name("Kyle Stafford")
                .representedBy(List.of(
                    DocmosisRepresentedBy.builder()
                        .name("Barbara Rep 1 (K)")
                        .email("1K@representatives.com")
                        .phoneNumber("+44 71000001")
                        .build()))
                .build());
    }

    private Schedule getExpectedSchedule() {
        return Schedule.builder()
            .allocation("An allocation")
            .alternativeCarers("Alternatives")
            .application("An application")
            .childrensCurrentArrangement("Current arrangement")
            .includeSchedule("Yes")
            .keyIssues("Key Issues")
            .partiesPositions("Some positions")
            .threshold("threshold")
            .timetableForChildren("time goes by")
            .timetableForProceedings("so slowly")
            .todaysHearing("slowly")
            .build();
    }

    private List<DocmosisRecital> getExpectedRecital() {
        return List.of(
            DocmosisRecital.builder()
                .title("A title")
                .body("A description")
                .build());
    }

    private List<DocmosisChild> getExpectedChildren() {
        return List.of(
            DocmosisChild.builder()
                .name("Bran Stark")
                .gender("Boy")
                .dateOfBirth(formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG))
                .build(),
            DocmosisChild.builder()
                .name("Sansa Stark")
                .gender("Boy")
                .dateOfBirth(formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG))
                .build(),
            DocmosisChild.builder()
                .name("Jon Snow")
                .gender("Girl")
                .dateOfBirth(formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG))
                .build());
    }

    private List<DocmosisRespondent> getExpectedRespondents() {
        return List.of(
            DocmosisRespondent.builder()
                .name("Timothy Jones")
                .relationshipToChild("Father")
                .build(),
            DocmosisRespondent.builder()
                .name("Sarah Simpson")
                .relationshipToChild("Mother")
                .build());
    }

    private String getHearingTime() {
        return String.format("%s - %s", formatLocalDateTimeBaseUsingFormat(NOW, "d MMMM, h:mma"),
            formatLocalDateTimeBaseUsingFormat(NOW.plusDays(1), "d MMMM, h:mma"));
    }
}
