package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.JsonOrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.time.LocalTime.NOON;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.format.FormatStyle.LONG;
import static java.util.Locale.UK;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedApplicants;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createStandardDirectionOrders;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, HearingVenueLookUpService.class,
    LookupTestConfig.class, StandardDirectionOrderGenerationService.class,
    CaseDataExtractionService.class, FixedTimeConfiguration.class
})
class StandardDirectionOrderGenerationServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Family Court";

    @MockBean
    private CalendarService calendarService;

    @Autowired
    private Time time;

    @Autowired
    private StandardDirectionOrderGenerationService service;

    @BeforeEach
    void setup() {
        given(calendarService.getWorkingDayFrom(any(LocalDate.class), anyInt())).willReturn(LocalDate.now());

    }

    @Test
    void shouldMapDirectionsForDraftSDOWhenAllAssignees() {
        StandardDirectionOrder order = StandardDirectionOrder.builder().directions(getDirections()).build();
        DocmosisStandardDirectionOrder templateData = service.getTemplateData(getCaseData(order));

        assertThat(templateData.getDirections()).containsAll(expectedDirections());
    }

    @Test
    void shouldNotAddDirectionsMarkedNotNeededToDocmosisObject() {
        Direction notNeededDirection = Direction.builder().directionNeeded("No").build();
        StandardDirectionOrder order = StandardDirectionOrder.builder()
            .directions(wrapElements(notNeededDirection))
            .build();

        DocmosisStandardDirectionOrder template = service.getTemplateData(getCaseData(order));

        assertThat(template.getDirections()).isEmpty();
    }

    @Test
    void shouldMapCaseDataWhenEmptyListValues() {
        CaseData caseData = caseDataWithEmptyListValues();

        DocmosisStandardDirectionOrder template = service.getTemplateData(caseData);

        assertThat(template)
            .isEqualToComparingFieldByField(docmosisOrder(
                "Her Honour Judge Smith",
                "Bob Ross",
                "123",
                "29 November 2019",
                getExpectedDirections()));
    }

    @Test
    void shouldMapCompleteCaseDataForSDOTemplate() {
        DocmosisStandardDirectionOrder template = service.getTemplateData(fullCaseData());

        assertThat(template).isEqualToComparingFieldByField(fullDocmosisOrder());
    }

    private CaseData getCaseData(StandardDirectionOrder order) {
        LocalDate today = time.now().toLocalDate();

        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .dateSubmitted(today)
            .standardDirectionOrder(order)
            .applicants(getEmptyApplicants())
            .build();
    }

    private List<Element<Applicant>> getEmptyApplicants() {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder().build())
            .build());
    }

    private DocmosisStandardDirectionOrder emptyDocmosisOrder(StandardDirectionOrder order) {
        return docmosisOrder("", "", null, order.getDateOfIssue(), emptyList());
    }

    private CaseData caseDataWithEmptyListValues() {
        LocalDate today = time.now().toLocalDate();

        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .familyManCaseNumber("123")
            .children1(emptyList())
            .dateSubmitted(today)
            .respondents1(emptyList())
            .applicants(getEmptyApplicants())
            .standardDirectionOrder(createStandardDirectionOrders(today.atStartOfDay(), DRAFT))
            .build();
    }

    private DocmosisStandardDirectionOrder docmosisOrder(String judgeTitleAndName,
                                                         String legalAdvisorName,
                                                         String familyManCaseNumber,
                                                         String dateOfIssue,
                                                         List<DocmosisDirection> expectedDirections) {
        LocalDate today = time.now().toLocalDate();

        return DocmosisStandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName(judgeTitleAndName)
                .legalAdvisorName(legalAdvisorName)
                .build())
            .courtName(COURT_NAME)
            .familyManCaseNumber(familyManCaseNumber)
            .dateOfIssue(dateOfIssue)
            .complianceDeadline(formatLocalDateToString(today.plusWeeks(26), LONG))
            .children(emptyList())
            .hearingBooking(DocmosisHearingBooking.builder().build())
            .respondents(emptyList())
            .respondentsProvided(false)
            .directions(expectedDirections)
            .applicantName("")
            .crest("[userImage:crest.png]")
            .draftbackground("[userImage:draft-watermark.png]")
            .build();
    }

    private DocmosisStandardDirectionOrder fullDocmosisOrder() {
        LocalDate today = time.now().toLocalDate();

        return DocmosisStandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("Her Honour Judge Smith")
                .legalAdvisorName("Bob Ross")
                .build())
            .courtName(COURT_NAME)
            .familyManCaseNumber("123")
            .dateOfIssue("29 November 2019")
            .complianceDeadline(formatLocalDateToString(today.plusWeeks(26), LONG))
            .children(getExpectedChildren())
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate(formatLocalDateToString(today, LONG))
                .hearingVenue("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW")
                .preHearingAttendance("11:00pm")
                .hearingTime("12:00am - 12:00pm")
                .hearingJudgeTitleAndName("Her Honour Judge Law")
                .hearingLegalAdvisorName("Peter Parker")
                .build())
            .respondents(getExpectedRespondents())
            .respondentsProvided(true)
            .directions(getExpectedDirections())
            .applicantName("Bran Stark")
            .crest("[userImage:crest.png]")
            .courtseal("[userImage:familycourtseal.png]")
            .build();
    }

    private CaseData fullCaseData() {
        LocalDate today = time.now().toLocalDate();

        return CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren(today))
            .hearingDetails(createHearingBookings())
            .dateSubmitted(LocalDate.now())
            .respondents1(createRespondents())
            .applicants(createPopulatedApplicants())
            .standardDirectionOrder(createStandardDirectionOrders(today.atStartOfDay(), SEALED))
            .build();
    }

    private List<DocmosisDirection> getExpectedDirections() {
        LocalDate today = time.now().toLocalDate();

        return List.of(
            DocmosisDirection.builder()
                .assignee(ALL_PARTIES)
                .title("2. Test SDO type 1 on " + today.atStartOfDay().format(ofPattern(DATE_TIME_AT, UK)))
                .body("Test body 1")
                .build(),
            DocmosisDirection.builder()
                .assignee(ALL_PARTIES)
                .title("3. Test SDO type 2 by " + today.atStartOfDay().format(ofPattern(TIME_DATE, UK)))
                .body("Test body 2")
                .build());
    }

    private List<Element<Direction>> getDirections() {
        return Stream.of(DirectionAssignee.values())
            .map(assignee -> element(Direction.builder()
                .directionType("Direction")
                .assignee(assignee)
                .build()))
            .collect(toList());
    }

    private List<DocmosisDirection> expectedDirections() {
        AtomicInteger at = new AtomicInteger(2);

        return getDirections().stream()
            .map(direction -> DocmosisDirection.builder()
                .title(at.getAndIncrement() + ". " + direction.getValue().getDirectionType())
                .assignee(direction.getValue().getAssignee())
                .build())
            .collect(toList());
    }

    private List<DocmosisChild> getExpectedChildren() {
        LocalDate today = time.now().toLocalDate();

        return List.of(
            DocmosisChild.builder()
                .name("Bran Stark")
                .gender("Boy")
                .dateOfBirth(formatLocalDateToString(today, LONG))
                .build(),
            DocmosisChild.builder()
                .name("Sansa Stark")
                .gender("Boy")
                .dateOfBirth(formatLocalDateToString(today, LONG))
                .build(),
            DocmosisChild.builder()
                .name("Jon Snow")
                .gender("Girl")
                .dateOfBirth(formatLocalDateToString(today, LONG))
                .build()
        );
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
                .build()
        );
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        LocalDate today = time.now().toLocalDate();

        return wrapElements(createHearingBooking(today.atStartOfDay(), today.atTime(NOON)));
    }
}
