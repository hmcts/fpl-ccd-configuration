package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.DEFAULT;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.DATE;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildCaseDataForCMODocmosisGeneration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CMODocmosisTemplateDataGenerationService.class})
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, DraftCMOService.class, CommonCaseDataExtractionService.class,
    DateFormatterService.class, CommonDirectionService.class, HearingVenueLookUpService.class,
    HearingBookingService.class, JsonOrdersLookupService.class, CaseDataExtractionService.class, LookupTestConfig.class
})
class CMODocmosisTemplateDataGenerationServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String COURT_NAME = "Family Court";
    private static final String HEARING_VENUE = "Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW";

    @Autowired
    private CMODocmosisTemplateDataGenerationService service;

    @Test
    void shouldReturnEmptyMapValuesWhenCaseDataIsEmpty() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(emptyList())
            .dateSubmitted(NOW.toLocalDate())
            .respondents1(emptyList())
            .applicants(emptyList())
            .schedule(Schedule.builder().includeSchedule("No").build())
            .caseManagementOrder(CaseManagementOrder.builder().build())
            .build();

        DocmosisCaseManagementOrder templateData = service.getCaseManagementOrderData(caseData);

        assertThat(templateData).isEqualToComparingFieldByField(caseManagementOrderWithEmptyFields(templateData));
    }

    @Test
    void shouldReturnFullyPopulatedMapWhenCompleteCaseDetailsAreProvided() throws IOException {
        CaseData caseData = buildCaseDataForCMODocmosisGeneration(NOW);
        DocmosisCaseManagementOrder templateData = service.getCaseManagementOrderData(caseData);

        //template data needs to be passed in for the draft and court seal image assertions.
        assertThat(templateData).isEqualToComparingFieldByField(expectedCaseManagementOrder(templateData));
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
            .caseManagementNumber(1)
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
            .draftbackground(templateData.getDraftbackground())
            .build();
    }

    private DocmosisCaseManagementOrder expectedCaseManagementOrder(DocmosisCaseManagementOrder templateData) {
        return DocmosisCaseManagementOrder.builder()
            .courtName(COURT_NAME)
            .familyManCaseNumber("123")
            //need to update document from generationDate -> dateOfIssue
            .dateOfIssue(formatLocalDateToString(NOW.toLocalDate(), FormatStyle.LONG))
            .complianceDeadline(formatLocalDateToString(NOW.toLocalDate().plusWeeks(26), FormatStyle.LONG))
            .children(getExpectedChildren())
            .numberOfChildren(getExpectedChildren().size())
            .applicantName("Bran Stark")
            .respondents(getExpectedRespondents())
            .respondentsProvided(true)
            .representatives(getExpectedRepresentatives())
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate(formatLocalDateToString(LocalDate.now(), DATE))
                .hearingVenue(HEARING_VENUE)
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
            .draftbackground(templateData.getDraftbackground())
            .courtseal(templateData.getCourtseal())
            .caseManagementNumber(2)
            .build();
    }

    private List<DocmosisDirection> expectedDirections() {
        return List.of(
            getExpectedDirection(1, ALL_PARTIES, null),
            getExpectedDirection(2, LOCAL_AUTHORITY, null),
            getExpectedDirection(3, PARENTS_AND_RESPONDENTS, "Respondent 1"),
            getExpectedDirection(4, PARENTS_AND_RESPONDENTS, null),
            getExpectedDirection(5, CAFCASS, null),
            getExpectedDirection(6, OTHERS, "Person 1"),
            getExpectedDirection(7, COURT, null));
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

    private List<Map<String, Object>> getEmptyRepresentativeList() {
        return List.of(
            Map.of(
                "name", DEFAULT,
                "representedBy", List.of(
                    Map.of("representativeName", DEFAULT,
                        "representativeEmail", DEFAULT,
                        "representativePhoneNumber", DEFAULT)))
        );
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

    private DocmosisDirection getExpectedDirection(int index, DirectionAssignee assignee, String header) {
        String directionTitle = String.format("%d. Direction title by 10:00am, 1 January 2099", index);

        return ofNullable(header)
            .map(x -> DocmosisDirection.builder()
                .header(String.format("For %s", x))
                .assignee(assignee)
                .title(directionTitle)
                .body("Mock direction text")
                .build())
            .orElse(DocmosisDirection.builder()
                .assignee(assignee)
                .title(directionTitle)
                .body("Mock direction text")
                .build());
    }

    private String getHearingTime() {
        return String.format("%s - %s", formatLocalDateTimeBaseUsingFormat(NOW, "d MMMM, h:mma"),
            formatLocalDateTimeBaseUsingFormat(NOW.plusDays(1), "d MMMM, h:mma"));
    }
}
