package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalTime.NOON;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.format.FormatStyle.LONG;
import static java.util.Locale.UK;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
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
    LookupTestConfig.class, StandardDirectionOrderGenerationService.class, HearingBookingService.class,
    CommonDirectionService.class, CommonCaseDataExtractionService.class, FixedTimeConfiguration.class
})
class StandardDirectionOrderGenerationServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Family Court";
    private LocalDate today;

    @Autowired
    private Time time;

    @MockBean
    private UserDetailsService userDetailsService;

    @InjectMocks
    private CommonDirectionService commonDirectionService;

    @Autowired
    private StandardDirectionOrderGenerationService standardDirectionOrderGenerationService;

    @BeforeEach
    void setUp() {
        given(userDetailsService.getUserName()).willReturn("Emma Taylor");
        today = time.now().toLocalDate();
    }

    //TODO: there needs to be some clarity around what should happen when values are missing from template.
    // emptyCaseData is unrealistic scenario. FPLA-1486
    @Test
    void shouldMapEmptyCaseDataForDraftSDO() throws IOException {
        Order order = Order.builder()
            .dateOfIssue("29 November 2019")
            .build();

        DocmosisStandardDirectionOrder template = standardDirectionOrderGenerationService
            .getTemplateData(CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .dateSubmitted(today)
                .standardDirectionOrder(order)
                .build());

        assertThat(template).isEqualToComparingFieldByField(DocmosisStandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("")
                .legalAdvisorName("")
                .build())
            .courtName(COURT_NAME)
            .familyManCaseNumber(null)
            .dateOfIssue(order.getDateOfIssue())
            .complianceDeadline(formatLocalDateToString(today.plusWeeks(26), LONG))
            .children(emptyList())
            .hearingBooking(DocmosisHearingBooking.builder().build())
            .respondents(emptyList())
            .respondentsProvided(false)
            .directions(emptyList())
            .applicantName("")
            .crest("[userImage:crest.png]")
            .draftbackground("[userImage:draft-watermark.png]")
            .build());
    }

    @Test
    void shouldMapDirectionsForDraftSDOWhenAllAssignees() throws IOException {
        DocmosisStandardDirectionOrder templateData = standardDirectionOrderGenerationService
            .getTemplateData(CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .dateSubmitted(today)
                .standardDirectionOrder(Order.builder().directions(getDirections()).build())
                .build());

        assertThat(templateData.getDirections()).containsAll(expectedDirections());
    }

    @Test
    void shouldMapCaseDataWhenEmptyListValues() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .familyManCaseNumber("123")
            .children1(emptyList())
            .dateSubmitted(today)
            .respondents1(emptyList())
            .applicants(emptyList())
            .standardDirectionOrder(createStandardDirectionOrders(today.atStartOfDay(), DRAFT))
            .build();

        DocmosisStandardDirectionOrder template = standardDirectionOrderGenerationService
            .getTemplateData(caseData);

        assertThat(template).isEqualToComparingFieldByField(DocmosisStandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("Her Honour Judge Smith")
                .legalAdvisorName("Bob Ross")
                .build())
            .courtName(COURT_NAME)
            .familyManCaseNumber("123")
            .dateOfIssue("29 November 2019")
            .complianceDeadline(formatLocalDateToString(today.plusWeeks(26), LONG))
            .children(emptyList())
            .hearingBooking(DocmosisHearingBooking.builder().build())
            .respondents(emptyList())
            .respondentsProvided(false)
            .directions(getExpectedDirections())
            .applicantName("")
            .crest("[userImage:crest.png]")
            .draftbackground("[userImage:draft-watermark.png]")
            .build());
    }

    @Test
    void shouldMapCompleteCaseDataForSDOTemplate() throws IOException {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren(today))
            .hearingDetails(createHearingBookings())
            .dateSubmitted(today)
            .respondents1(createRespondents())
            .applicants(createPopulatedApplicants())
            .standardDirectionOrder(createStandardDirectionOrders(today.atStartOfDay(), SEALED))
            .build();

        DocmosisStandardDirectionOrder template = standardDirectionOrderGenerationService
            .getTemplateData(caseData);

        assertThat(template).isEqualToComparingFieldByField(DocmosisStandardDirectionOrder.builder()
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
            .build());
    }

    private List<DocmosisDirection> getExpectedDirections() {
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
        List<Element<Direction>> directions = commonDirectionService.numberDirections(getDirections());

        return directions.stream()
            .map(direction -> DocmosisDirection.builder()
                .title(direction.getValue().getDirectionType() + " by unknown")
                .assignee(direction.getValue().getAssignee())
                .build())
            .collect(toList());
    }

    private List<DocmosisChild> getExpectedChildren() {
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
        return wrapElements(createHearingBooking(today.atStartOfDay(), today.atTime(NOON)));
    }
}
