package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.JsonOrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static java.time.LocalTime.NOON;
import static java.time.format.FormatStyle.LONG;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ASK_FOR_DISCLOSURE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CUSTOM;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedApplicants;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, HearingVenueLookUpService.class,
    LookupTestConfig.class, GatekeepingOrderGenerationService.class, CaseDataExtractionService.class,
    FixedTimeConfiguration.class, CourtService.class, HighCourtAdminEmailLookupConfiguration.class
})
class GatekeepingOrderGenerationServiceTest {

    private static final long CASE_NUMBER = 1234123412341234L;
    private static final String FORMATTED_CASE_NUMBER = "1234-1234-1234-1234";

    @Autowired
    private Time time;

    @Autowired
    private GatekeepingOrderGenerationService underTest;

    @Test
    void shouldGenerateSealedOrder() {
        DocmosisStandardDirectionOrder templateData = underTest.getTemplateData(caseDataForSealed());
        DocmosisStandardDirectionOrder expectedData = fullSealedOrder();

        assertThat(templateData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateSealedOrderWithLegacyApplicant() {
        DocmosisStandardDirectionOrder templateData = underTest.getTemplateData(caseDataForSealedWithLegacyApplicant());
        DocmosisStandardDirectionOrder expectedData = fullSealedOrderFromLegacyApplicant();

        assertThat(templateData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateSealedOrderWithListedHearing() {
        CaseData testData = caseDataForSealed();

        LocalDate testDate = time.now().toLocalDate().plusDays(10);
        Element<HearingBooking> selectedHearing =
            element(createHearingBooking(testDate.atStartOfDay(), testDate.atTime(NOON)));

        testData.addHearingBooking(selectedHearing);
        testData = testData.toBuilder().selectedHearingId(selectedHearing.getId()).build();

        DocmosisStandardDirectionOrder templateData = underTest.getTemplateData(testData);
        DocmosisStandardDirectionOrder expectedData = fullSealedOrder();
        expectedData = expectedData.toBuilder()
                .hearingBooking(expectedData.getHearingBooking().toBuilder()
                    .hearingDate(formatLocalDateToString(testDate, LONG))
                    .hearingStartDate(formatLocalDateTimeBaseUsingFormat(testDate.atStartOfDay(), DATE_TIME))
                    .hearingEndDate(formatLocalDateTimeBaseUsingFormat(testDate.atTime(NOON), DATE_TIME))
                    .build()).build();

        assertThat(templateData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateDraftOrder() {
        DocmosisStandardDirectionOrder templateData = underTest.getTemplateData(caseDataForDraft());
        DocmosisStandardDirectionOrder expectedData = fullDraftOrder();

        assertThat(templateData).isEqualTo(expectedData);
    }

    @Test
    void shouldGenerateDraftOrderWithLegacyApplicant() {
        DocmosisStandardDirectionOrder templateData = underTest.getTemplateData(caseDataForDraftWithLegacyApplicant());
        DocmosisStandardDirectionOrder expectedData = fullDraftOrderFromLegacyApplicant();

        assertThat(templateData).isEqualTo(expectedData);
    }

    private DocmosisStandardDirectionOrder fullSealedOrder() {
        return fullSealedOrderFromLegacyApplicant().toBuilder()
            .applicantName("Local authority name")
            .build();
    }

    private DocmosisStandardDirectionOrder fullSealedOrderFromLegacyApplicant() {
        return baseDocmosisOrder().toBuilder()
            .courtseal("[userImage:familycourtseal.png]")
            .dateOfIssue("29 November 2019")
            .isUrgentOrder(false)
            .orderDocumentTitle("Standard Directions Order")
            .build();
    }

    private DocmosisStandardDirectionOrder fullDraftOrder() {
        return fullDraftOrderFromLegacyApplicant().toBuilder()
            .applicantName("Local authority name")
            .orderDocumentTitle("Urgent Directions Order")
            .isUrgentOrder(true)
            .build();
    }

    private DocmosisStandardDirectionOrder fullDraftOrderFromLegacyApplicant() {
        return baseDocmosisOrder().toBuilder()
            .draftbackground("[userImage:draft-watermark.png]")
            .dateOfIssue("<date will be added on issue>")
            .orderDocumentTitle("Urgent Directions Order")
            .build();
    }

    private CaseData caseDataForSealed() {
        return caseDataForSealedWithLegacyApplicant().toBuilder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .designated("Yes")
                .name("Local authority name")
                .build()))
            .build();
    }

    private CaseData caseDataForSealedWithLegacyApplicant() {
        return baseCaseData().toBuilder()
            .gatekeepingOrderEventData(baseCaseData().getGatekeepingOrderEventData().toBuilder()
                .gatekeepingOrderSealDecision(GatekeepingOrderSealDecision.builder()
                    .dateOfIssue(LocalDate.of(2019, 11, 29))
                    .orderStatus(SEALED)
                    .build())
                .build())
            .gatekeepingOrderRouter(SERVICE)
            .build();
    }

    private CaseData caseDataForDraft() {
        return caseDataForDraftWithLegacyApplicant().toBuilder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .designated("Yes")
                .name("Local authority name")
                .build()))
            .urgentDirectionsRouter(SERVICE)
            .build();
    }

    private CaseData caseDataForDraftWithLegacyApplicant() {
        return baseCaseData().toBuilder()
            .gatekeepingOrderEventData(baseCaseData().getGatekeepingOrderEventData().toBuilder()
                .gatekeepingOrderSealDecision(GatekeepingOrderSealDecision.builder()
                    .orderStatus(DRAFT)
                    .build())
                .build())
            .build();
    }

    private DocmosisStandardDirectionOrder baseDocmosisOrder() {
        LocalDate today = time.now().toLocalDate();

        return DocmosisStandardDirectionOrder.builder()
            .ccdCaseNumber(FORMATTED_CASE_NUMBER)
            .judgeAndLegalAdvisor(DocmosisJudgeAndLegalAdvisor.builder()
                .judgeTitleAndName("Her Honour Judge Smith")
                .legalAdvisorName("Bob Ross")
                .build())
            .courtName(DEFAULT_LA_COURT)
            .familyManCaseNumber("123")
            .complianceDeadline(formatLocalDateToString(today.plusWeeks(26), LONG))
            .children(getExpectedChildren())
            .directions(getExpectedDirections())
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate(formatLocalDateToString(today, LONG))
                .hearingVenue("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW")
                .hearingAttendance("In person")
                .hearingAttendanceDetails("Room: 123")
                .preHearingAttendance("30 minutes before the hearing")
                .hearingTime("12:00am - 12:00pm")
                .hearingJudgeTitleAndName("Her Honour Judge Law")
                .hearingLegalAdvisorName("Peter Parker")
                .hearingStartDate(formatLocalDateTimeBaseUsingFormat(LocalDate.now().atStartOfDay(), DATE_TIME))
                .hearingEndDate(formatLocalDateTimeBaseUsingFormat(LocalDate.now().atTime(NOON), DATE_TIME))
                .build())
            .respondents(getExpectedRespondents())
            .respondentsProvided(true)
            .applicantName("Bran Stark")
            .crest("[userImage:crest.png]")
            .build();
    }

    private CaseData baseCaseData() {
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
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .customDirections(wrapElements(getCustomDirections()))
                .standardDirections(wrapElements(getStandardDirections()))
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Smith")
                    .legalAdvisorName("Bob Ross")
                    .build())
                .build())
            .build();
    }

    private List<CustomDirection> getCustomDirections() {
        return List.of(
            CustomDirection.builder()
                .type(CUSTOM)
                .title("First custom direction title")
                .description("First custom direction description")
                .assignee(LOCAL_AUTHORITY)
                .dueDateType(DATE)
                .dateToBeCompletedBy(LocalDateTime.of(2030, Month.JUNE, 14, 8, 50, 0))
                .build(),
            CustomDirection.builder()
                .type(CUSTOM)
                .title("Second custom direction title")
                .description("Second custom direction description")
                .assignee(CAFCASS)
                .dueDateType(DAYS)
                .daysBeforeHearing(1)
                .build(),
            CustomDirection.builder()
                .type(CUSTOM)
                .title("Arrange interpreters")
                .description("At all hearings, the court must arrange an interpreter for Louise Laurent in French.")
                .assignee(COURT)
                .dueDateType(DAYS)
                .daysBeforeHearing(0)
                .build());
    }

    private List<StandardDirection> getStandardDirections() {
        return List.of(
            StandardDirection.builder()
                .type(ASK_FOR_DISCLOSURE)
                .title("Ask for disclosure")
                .description("Serve requests for disclosure on any third parties")
                .assignee(LOCAL_AUTHORITY)
                .dueDateType(DATE)
                .dateToBeCompletedBy(LocalDateTime.of(2030, 5, 10, 12, 0, 0))
                .build(),
            StandardDirection.builder()
                .type(REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS)
                .title("Request help to take part in proceedings")
                .description("Make an application to the court if you believe any party or witness needs help")
                .assignee(ALL_PARTIES)
                .dueDateType(DATE)
                .dateToBeCompletedBy(LocalDateTime.of(2030, 5, 10, 12, 0, 0))
                .build(),
            StandardDirection.builder()
                .type(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE)
                .title("Request permission for expert evidence")
                .description("Your request must be in line with Family Procedure Rules part 25")
                .assignee(ALL_PARTIES)
                .dueDateType(DAYS)
                .daysBeforeHearing(3)
                .build());
    }

    private List<DocmosisDirection> getExpectedDirections() {
        return List.of(
            DocmosisDirection.builder()
                .assignee(ALL_PARTIES)
                .title("2. Request help to take part in proceedings by 12:00pm, 10 May 2030")
                .body("Make an application to the court if you believe any party or witness needs help")
                .build(),
            DocmosisDirection.builder()
                .assignee(ALL_PARTIES)
                .title("3. Request permission for expert evidence 3 working days before the hearing")
                .body("Your request must be in line with Family Procedure Rules part 25")
                .build(),
            DocmosisDirection.builder()
                .assignee(LOCAL_AUTHORITY)
                .title("4. Ask for disclosure on 12:00pm, 10 May 2030")
                .body("Serve requests for disclosure on any third parties")
                .build(),
            DocmosisDirection.builder()
                .assignee(LOCAL_AUTHORITY)
                .title("5. First custom direction title by 14 June 2030 at 8:50am")
                .body("First custom direction description")
                .build(),
            DocmosisDirection.builder()
                .assignee(CAFCASS)
                .title("6. Second custom direction title 1 working day before the hearing")
                .body("Second custom direction description")
                .build(),
            DocmosisDirection.builder()
                .assignee(COURT)
                .title("7. Arrange interpreters by the day of the hearing")
                .body("At all hearings, the court must arrange an interpreter for Louise Laurent in French.")
                .build()
        );
    }

    private List<DocmosisChild> getExpectedChildren() {
        LocalDate today = time.now().toLocalDate();

        return List.of(
            DocmosisChild.builder()
                .name("Bran Stark")
                .gender("Male")
                .dateOfBirth(formatLocalDateToString(today, LONG))
                .build(),
            DocmosisChild.builder()
                .name("Sansa Stark")
                .gender("Male")
                .dateOfBirth(formatLocalDateToString(today, LONG))
                .build(),
            DocmosisChild.builder()
                .name("Jon Snow")
                .gender("Female")
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
