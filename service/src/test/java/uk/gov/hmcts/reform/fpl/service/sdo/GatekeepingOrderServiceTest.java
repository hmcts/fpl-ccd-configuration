package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.docmosis.GatekeepingOrderGenerationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.DueDateType.DATE;
import static uk.gov.hmcts.reform.fpl.enums.DueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JacksonAutoConfiguration.class, CaseConverter.class, GatekeepingOrderService.class})
class GatekeepingOrderServiceTest {
    private static final String NEXT_STEPS = "## Next steps\n\n"
        + "Your order will be saved as a draft in 'Draft orders'.\n\n"
        + "You cannot seal and send the order until adding:\n\n";

    private static final Document DOCUMENT = testDocument();
    private static final DocumentReference REFERENCE = buildFromDocument(DOCUMENT);

    @MockBean
    private DocumentService documentService;

    @MockBean
    private GatekeepingOrderGenerationService gatekeepingOrderGenerationService;

    @MockBean
    private OrdersLookupService ordersLookupService;

    @MockBean
    private CalendarService calendarService;

    @Autowired
    private GatekeepingOrderService underTest;

    @BeforeEach
    void setUp() {
        given(documentService.getDocumentFromDocmosisOrderTemplate(any(), eq(SDO))).willReturn(DOCUMENT);
    }

    @Test
    void shouldNotBuildNextStepsLabelWhenAllRequiredInformationPresent() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
                .build())
            .build();

        GatekeepingOrderSealDecision expected = GatekeepingOrderSealDecision.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(null)
            .build();

        assertThat(underTest.buildSealDecisionPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoHearingDetails() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
                .build())
            .build();

        GatekeepingOrderSealDecision expected = GatekeepingOrderSealDecision.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the first hearing details")
            .build();

        assertThat(underTest.buildSealDecisionPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoAllocatedJudge() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("No")
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Nelson")
                    .build())
                .build())
            .build();

        GatekeepingOrderSealDecision expected = GatekeepingOrderSealDecision.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the allocated judge")
            .build();

        assertThat(underTest.buildSealDecisionPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoIssuingJudge() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .build();

        GatekeepingOrderSealDecision expected = GatekeepingOrderSealDecision.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the judge issuing the order")
            .build();

        assertThat(underTest.buildSealDecisionPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenAllFieldsMandatoryFieldsMissing() {
        CaseData caseData = CaseData.builder().build();

        GatekeepingOrderSealDecision expected = GatekeepingOrderSealDecision.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the first hearing details\n\n"
                + "* the allocated judge\n\n"
                + "* the judge issuing the order")
            .build();

        assertThat(underTest.buildSealDecisionPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldSetAllocatedJudgeLabel() {
        Judge allocatedJudge = Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Hastings")
            .build();
        JudgeAndLegalAdvisor issuingJudge = JudgeAndLegalAdvisor.builder()
            .build();

        JudgeAndLegalAdvisor expectedJudge = issuingJudge.toBuilder()
            .allocatedJudgeLabel("Case assigned to: His Honour Judge Hastings")
            .build();

        assertThat(underTest.setAllocatedJudgeLabel(allocatedJudge, issuingJudge)).isEqualTo(expectedJudge);
    }

    @Test
    void getNoticeOfProceedingsTemplatesWithNoOthers() {
        CaseData caseData = CaseData.builder().build();

        assertThat(underTest.getNoticeOfProceedingsTemplates(caseData)).isEqualTo(List.of(C6));
    }

    @Test
    void getNoticeOfProceedingsTemplatesWithOthers() {
        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(mock(Other.class)).build()).build();

        assertThat(underTest.getNoticeOfProceedingsTemplates(caseData)).isEqualTo(List.of(C6, C6A));
    }


    private DirectionConfiguration directionConfiguration(DirectionType type, int days) {
        return DirectionConfiguration.builder()
            .id(type)
            .assignee(ALL_PARTIES)
            .title(format("title - %s", type))
            .text(format("text - %s", type))
            .display(Display
                .builder()
                .due(Display.Due.ON)
                .delta("" + (-1 * days))
                .build())
            .build();
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldCreateDirectionFromConfAndSetDefaultDueDateTypeWhenNoHearingPresent(DirectionType type) {

        final DirectionConfiguration directionConfiguration = directionConfiguration(type, 1);

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of("sdoDirectionsForAll", List.of(type))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        StandardDirection expectedDirection = StandardDirection.builder()
            .type(directionConfiguration.getId())
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(1)
            .showDateOnly(YesNo.from(directionConfiguration.getDisplay().isShowDateOnly()))
            .dateToBeCompletedBy(null)
            .dueDateType(DAYS)
            .build();

        assertThat(caseDetails.getData().get("sdoDirection-" + type)).isEqualTo(expectedDirection);
    }


    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldCreateDirectionFromConfAndSetDefaultDatesFromHearing(DirectionType type) {

        Integer dueDateDaysBeforeHearing = 2;
        LocalDateTime hearingDate = LocalDateTime.of(2050, JANUARY, 10, 12, 0, 0);
        LocalDateTime directionDueDate = LocalDateTime.of(2050, JANUARY, 8, 0, 0, 0);

        HearingBooking hearing1 = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(hearingDate)
            .build();

        HearingBooking hearing2 = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(hearingDate.plusDays(1))
            .build();

        DirectionConfiguration directionConfiguration = directionConfiguration(type, dueDateDaysBeforeHearing);

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);
        when(calendarService.getWorkingDayFrom(hearingDate.toLocalDate(), -2))
            .thenReturn(directionDueDate.toLocalDate());

        CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "sdoDirectionsForAll", List.of(type),
                "hearingDetails", wrapElements(hearing1, hearing2))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        StandardDirection expectedDirection = StandardDirection.builder()
            .type(directionConfiguration.getId())
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(dueDateDaysBeforeHearing)
            .showDateOnly(YesNo.from(directionConfiguration.getDisplay().isShowDateOnly()))
            .dateToBeCompletedBy(directionDueDate)
            .dueDateType(DAYS)
            .build();

        assertThat(caseDetails.getData().get("sdoDirection-" + type)).isEqualTo(expectedDirection);

    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldCreateDirectionFromConfAndSetDefaultDatesFromHearingWhenDefaultDaysBeforeHearingIs0(DirectionType type) {

        LocalDateTime hearingDate = LocalDateTime.of(2050, JANUARY, 10, 12, 0, 0);
        LocalDateTime directionDueDate = LocalDateTime.of(2050, JANUARY, 10, 0, 0, 0);

        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(hearingDate)
            .build();

        DirectionConfiguration directionConfiguration = directionConfiguration(type, 0);

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "sdoDirectionsForAll", List.of(type),
                "hearingDetails", wrapElements(hearing))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        StandardDirection expectedDirection = StandardDirection.builder()
            .type(type)
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(0)
            .showDateOnly(YesNo.from(directionConfiguration.getDisplay().isShowDateOnly()))
            .dateToBeCompletedBy(directionDueDate)
            .dueDateType(DAYS)
            .build();

        assertThat(caseDetails.getData().get("sdoDirection-" + type)).isEqualTo(expectedDirection);

        verifyNoInteractions(calendarService);
    }


    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldGetDirectionFromDraft(DirectionType type) {
        StandardDirection draftDirection = StandardDirection.builder()
            .type(type)
            .title("title")
            .description("Text")
            .assignee(COURT)
            .daysBeforeHearing(0)
            .showDateOnly(YesNo.YES)
            .dateToBeCompletedBy(LocalDateTime.now())
            .dueDateType(DATE)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "standardDirections", wrapElements(draftDirection),
                "sdoDirectionsForAll", List.of(type))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        assertThat(caseDetails.getData().get("sdoDirection-" + type)).isEqualTo(draftDirection);

        verifyNoInteractions(ordersLookupService, calendarService);
    }


    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldUpdateStandardDirections(DirectionType type) {

        DirectionConfiguration directionConfiguration = directionConfiguration(type, 0);

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);

        StandardDirection oldDirectionDraft = StandardDirection.builder()
            .type(type)
            .title("title")
            .description("Text")
            .assignee(COURT)
            .daysBeforeHearing(0)
            .showDateOnly(YesNo.YES)
            .dateToBeCompletedBy(LocalDateTime.now())
            .dueDateType(DATE)
            .build();


        StandardDirection newDirectionDraft = StandardDirection.builder()
            .daysBeforeHearing(10)
            .dueDateType(DAYS)
            .build();


        StandardDirection expectedDirection = StandardDirection.builder()
            .type(type)
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(newDirectionDraft.getDaysBeforeHearing())
            .showDateOnly(YesNo.from(directionConfiguration.getDisplay().isShowDateOnly()))
            .dateToBeCompletedBy(null)
            .dueDateType(DAYS)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "standardDirections", wrapElements(oldDirectionDraft),
                "sdoDirection-" + type, newDirectionDraft,
                "sdoDirectionsForAll", List.of(type))))
            .build();

        CaseData cd = underTest.updateStandardDirections(caseDetails);

        assertThat(cd.getGatekeepingOrderEventData().getStandardDirections())
            .extracting(Element::getValue)
            .containsExactly(expectedDirection);

        verifyNoInteractions(calendarService);
    }


}
