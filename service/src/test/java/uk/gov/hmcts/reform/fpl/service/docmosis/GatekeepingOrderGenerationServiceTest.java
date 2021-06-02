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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedApplicants;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, HearingVenueLookUpService.class,
    LookupTestConfig.class, GatekeepingOrderGenerationService.class,
    CaseDataExtractionService.class, FixedTimeConfiguration.class
})
class GatekeepingOrderGenerationServiceTest {

    private static final long CASE_NUMBER = 1234123412341234L;
    private static final String FORMATTED_CASE_NUMBER = "1234-1234-1234-1234";

    @MockBean
    private CalendarService calendarService;

    @Autowired
    private Time time;

    @Autowired
    private GatekeepingOrderGenerationService underTest;

    @BeforeEach
    void setup() {
        given(calendarService.getWorkingDayFrom(any(LocalDate.class), anyInt())).willReturn(LocalDate.now());
    }

    @Test
    void shouldGeneratedSealedOrder() {
        DocmosisStandardDirectionOrder templateData = underTest.getTemplateData(fullCaseData());
        DocmosisStandardDirectionOrder expectedData = fullDocmosisOrder();
        assertThat(templateData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    private DocmosisStandardDirectionOrder fullDocmosisOrder() {
        LocalDate today = time.now().toLocalDate();

        return DocmosisStandardDirectionOrder.builder()
            .ccdCaseNumber(FORMATTED_CASE_NUMBER)
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("Her Honour Judge Smith")
                .legalAdvisorName("Bob Ross")
                .build())
            .courtName(DEFAULT_LA_COURT)
            .familyManCaseNumber("123")
            .dateOfIssue("29 November 2019")
            .complianceDeadline(formatLocalDateToString(today.plusWeeks(26), LONG))
            .children(getExpectedChildren())
            .directions(List.of())
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
            .applicantName("Bran Stark")
            .crest("[userImage:crest.png]")
            .courtseal("[userImage:familycourtseal.png]")
            .build();
    }

    private CaseData fullCaseData() {
        LocalDate today = time.now().toLocalDate();

        return CaseData.builder()
            .id(CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren(today))
            .hearingDetails(createHearingBookings())
            .dateSubmitted(LocalDate.now())
            .respondents1(createRespondents())
            .applicants(createPopulatedApplicants())
            .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Smith")
                .legalAdvisorName("Bob Ross")
                .build())
            .saveOrSendGatekeepingOrder(SaveOrSendGatekeepingOrder.builder()
                .dateOfIssue(LocalDate.of(2019, 11, 29))
                .orderStatus(SEALED)
                .build())
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
