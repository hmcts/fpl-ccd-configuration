package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.docmosis.GatekeepingOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testHearing;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudgeAndLegalAdviser;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JacksonAutoConfiguration.class, CaseConverter.class, GatekeepingOrderService.class,
    FixedTimeConfiguration.class})
class GatekeepingOrderServiceTest {
    private static final String NEXT_STEPS = "## Next steps\n\n"
        + "Your order will be saved as a draft in 'Draft orders'.\n\n"
        + "You cannot seal and send the order until adding:\n\n";

    private static final Document DOCUMENT = testDocument();
    private static final DocumentReference REFERENCE = buildFromDocument(DOCUMENT);

    @Autowired
    Time time;

    @MockBean
    private UserService userService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private DocumentSealingService sealingService;

    @MockBean
    private OrdersLookupService ordersLookupService;

    @MockBean
    private GatekeepingOrderGenerationService gatekeepingOrderGenerationService;

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
            .dateOfIssue(LocalDate.now())
            .build();

        assertThat(underTest.buildSealDecision(caseData)).isEqualTo(expected);
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
            .dateOfIssue(LocalDate.now())
            .build();

        assertThat(underTest.buildSealDecision(caseData)).isEqualTo(expected);
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
            .dateOfIssue(LocalDate.now())
            .build();

        assertThat(underTest.buildSealDecision(caseData)).isEqualTo(expected);
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
            .dateOfIssue(LocalDate.now())
            .build();

        assertThat(underTest.buildSealDecision(caseData)).isEqualTo(expected);
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
            .dateOfIssue(LocalDate.now())
            .build();

        assertThat(underTest.buildSealDecision(caseData)).isEqualTo(expected);
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

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldCreateDirectionFromConfAndSetDefaultDueDateTypeWhenNoHearingPresent(DirectionType type) {

        final DirectionConfiguration directionConfiguration = directionConfiguration(type, 1);

        final CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of("directionsForAllParties", List.of(type))))
            .build();

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);

        underTest.populateStandardDirections(caseDetails);

        final StandardDirection expectedDirection = StandardDirection.builder()
            .type(directionConfiguration.getType())
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(1)
            .dateToBeCompletedBy(null)
            .dueDateType(DAYS)
            .build();

        assertThat(caseDetails.getData().get("direction-" + type)).isEqualTo(expectedDirection);
    }


    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldCreateDirectionFromConfAndSetDefaultDatesFromHearing(DirectionType type) {

        final int dueDateDaysBeforeHearing = 2;
        final LocalDateTime hearingDate = LocalDateTime.of(2050, 1, 10, 12, 0, 0);
        final LocalDateTime directionDueDate = LocalDateTime.of(2050, 1, 8, 0, 0, 0);

        final HearingBooking hearing1 = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(hearingDate)
            .build();

        final HearingBooking hearing2 = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(hearingDate.plusDays(1))
            .build();

        final DirectionConfiguration directionConfiguration = directionConfiguration(type, dueDateDaysBeforeHearing);

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);
        when(calendarService.getWorkingDayFrom(hearingDate.toLocalDate(), -2))
            .thenReturn(directionDueDate.toLocalDate());

        final CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "directionsForAllParties", List.of(type),
                "hearingDetails", wrapElements(hearing1, hearing2))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        final StandardDirection expectedDirection = StandardDirection.builder()
            .type(directionConfiguration.getType())
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(dueDateDaysBeforeHearing)
            .dateToBeCompletedBy(directionDueDate)
            .dueDateType(DAYS)
            .build();

        assertThat(caseDetails.getData().get("direction-" + type)).isEqualTo(expectedDirection);
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldCreateDirectionFromConfAndSetDefaultDatesFromHearingWhenDefaultDaysBeforeHearingIs0(DirectionType type) {

        final LocalDateTime hearingDate = LocalDateTime.of(2050, 1, 10, 12, 0, 0);
        final LocalDateTime directionDueDate = LocalDateTime.of(2050, 1, 10, 0, 0, 0);

        final HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(hearingDate)
            .build();

        final DirectionConfiguration directionConfiguration = directionConfiguration(type, 0);

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);

        final CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "directionsForAllParties", List.of(type),
                "hearingDetails", wrapElements(hearing))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        final StandardDirection expectedDirection = StandardDirection.builder()
            .type(type)
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(0)
            .dateToBeCompletedBy(directionDueDate)
            .dueDateType(DAYS)
            .build();

        assertThat(caseDetails.getData().get("direction-" + type)).isEqualTo(expectedDirection);

        verifyNoInteractions(calendarService);
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldGetStandardDirectionsFromDraft(DirectionType type) {
        final StandardDirection draftDirection = StandardDirection.builder()
            .type(type)
            .title("title")
            .description("Text")
            .assignee(COURT)
            .daysBeforeHearing(0)
            .dateToBeCompletedBy(LocalDateTime.now())
            .dueDateType(DATE)
            .build();

        final CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "standardDirectionOrder", StandardDirectionOrder.builder()
                    .standardDirections(wrapElements(draftDirection))
                    .build(),
                "directionsForAllParties", List.of(type))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        assertThat(caseDetails.getData().get("direction-" + type)).isEqualTo(draftDirection);

        verifyNoInteractions(ordersLookupService, calendarService);
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldGetCurrentStandardDirection(DirectionType type) {
        final StandardDirection currentStandardDirection = StandardDirection.builder()
            .type(type)
            .title("title")
            .description("Text")
            .assignee(COURT)
            .daysBeforeHearing(0)
            .dateToBeCompletedBy(LocalDateTime.now())
            .dueDateType(DATE)
            .build();

        final StandardDirection draftedStandardDirection = StandardDirection.builder()
            .type(type)
            .title("title 2")
            .description("Text 2")
            .assignee(COURT)
            .daysBeforeHearing(1)
            .dateToBeCompletedBy(LocalDateTime.now().minusDays(1))
            .dueDateType(DAYS)
            .build();

        final CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                type.getFieldName(), currentStandardDirection,
                "standardDirectionOrder", StandardDirectionOrder.builder()
                    .standardDirections(wrapElements(draftedStandardDirection))
                    .build(),
                "directionsForAllParties", List.of(type))))
            .build();

        underTest.populateStandardDirections(caseDetails);

        assertThat(caseDetails.getData().get("direction-" + type)).isEqualTo(currentStandardDirection);

        verifyNoInteractions(ordersLookupService, calendarService);
    }

    @ParameterizedTest
    @EnumSource(value = DirectionType.class)
    void shouldUpdateStandardDirections(DirectionType type) {

        final DirectionConfiguration directionConfiguration = directionConfiguration(type, 0);

        final StandardDirection oldDirectionDraft = StandardDirection.builder()
            .type(type)
            .title("title")
            .description("Text")
            .assignee(COURT)
            .daysBeforeHearing(0)
            .dateToBeCompletedBy(LocalDateTime.now())
            .dueDateType(DATE)
            .build();

        final StandardDirection newDirectionDraft = StandardDirection.builder()
            .daysBeforeHearing(10)
            .dueDateType(DAYS)
            .build();

        final StandardDirection expectedDirection = StandardDirection.builder()
            .type(type)
            .title(directionConfiguration.getTitle())
            .description(directionConfiguration.getText())
            .assignee(directionConfiguration.getAssignee())
            .daysBeforeHearing(newDirectionDraft.getDaysBeforeHearing())
            .dateToBeCompletedBy(null)
            .dueDateType(DAYS)
            .build();

        final CaseDetails caseDetails = CaseDetails.builder()
            .data(newHashMap(Map.of(
                "standardDirections", wrapElements(oldDirectionDraft),
                "direction-" + type, newDirectionDraft,
                "directionsForAllParties", List.of(type))))
            .build();

        when(ordersLookupService.getDirectionConfiguration(type)).thenReturn(directionConfiguration);

        final CaseData updatedCase = underTest.updateStandardDirections(caseDetails);

        assertThat(updatedCase.getGatekeepingOrderEventData().getStandardDirections())
            .extracting(Element::getValue)
            .containsExactly(expectedDirection);

        verifyNoInteractions(calendarService);
    }

    private static DirectionConfiguration directionConfiguration(DirectionType type, int days) {
        return DirectionConfiguration.builder()
            .type(type)
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

    @Nested
    class SealDecisionForServiceRoute {

        final GatekeepingOrderRoute serviceRoute = GatekeepingOrderRoute.SERVICE;
        final Document generatedDocument = TestDataHelper.testDocument();

        final DocumentReference generatedOrder = DocumentReference.builder()
            .url(generatedDocument.links.self.href)
            .binaryUrl(generatedDocument.links.binary.href)
            .filename(generatedDocument.originalDocumentName)
            .build();

        @BeforeEach
        void init() {
            final DocmosisStandardDirectionOrder docmosisOrder = DocmosisStandardDirectionOrder.builder()
                .ccdCaseNumber("1")
                .build();

            when(gatekeepingOrderGenerationService.getTemplateData(any()))
                .thenReturn(docmosisOrder);
            when(documentService.getDocumentFromDocmosisOrderTemplate(docmosisOrder, SDO))
                .thenReturn(generatedDocument);
        }

        @Test
        void shouldPrepareSealDecisionWhenNoHearingAndNoAllocatedJudgeAndNotIssuingJudge() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(serviceRoute)
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(generatedOrder)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the allocated judge\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);

            verify(gatekeepingOrderGenerationService).getTemplateData(caseData);
        }

        @Test
        void shouldPrepareSealDecisionWhenAllocatedJudgeIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(serviceRoute)
                .allocatedJudge(testJudge())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(generatedOrder)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);

            verify(gatekeepingOrderGenerationService).getTemplateData(caseData);
        }

        @Test
        void shouldPrepareSealDecisionWhenFirstCaseManagementHearingIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(serviceRoute)
                .hearingDetails(wrapElements(testHearing(CASE_MANAGEMENT)))
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(generatedOrder)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the allocated judge\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);

            verify(gatekeepingOrderGenerationService).getTemplateData(caseData);
        }

        @Test
        void shouldPrepareSealDecisionWhenIssuingJudgeIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(serviceRoute)
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderIssuingJudge(testJudgeAndLegalAdviser())
                    .build())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(generatedOrder)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the allocated judge")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);

            verify(gatekeepingOrderGenerationService).getTemplateData(caseData);
        }

        @Test
        void shouldPrepareSealDecisionWhenAllRequiredInformationIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(serviceRoute)
                .allocatedJudge(testJudge())
                .hearingDetails(wrapElements(testHearing()))
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderIssuingJudge(testJudgeAndLegalAdviser())
                    .build())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(generatedOrder)
                .nextSteps(null)
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);

            verify(gatekeepingOrderGenerationService).getTemplateData(caseData);
        }
    }

    @Nested
    class SealDecisionForUploadRoute {

        final GatekeepingOrderRoute uploadRoute = GatekeepingOrderRoute.UPLOAD;
        final DocumentReference replacementSDO = testDocumentReference();
        final DocumentReference preparedSDO = testDocumentReference();
        final DocumentReference currentSDO = testDocumentReference();

        @AfterEach
        void tearDown() {
            verifyNoInteractions(gatekeepingOrderGenerationService, documentService);
        }

        @Test
        void shouldUseReplacementOrderWhenPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(replacementSDO)
                .preparedSDO(preparedSDO)
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .currentSDO(currentSDO)
                    .build())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(replacementSDO)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the allocated judge\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldUsePreviouslyPreparedOrderWhenReplacementOrderNotPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(null)
                .preparedSDO(preparedSDO)
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .currentSDO(null)
                    .build())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(preparedSDO)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the allocated judge\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldUsePreviouslyCurrentOrderWhenNoPreviouseOrderNorReplacement() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(null)
                .preparedSDO(null)
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .currentSDO(currentSDO)
                    .build())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(currentSDO)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the allocated judge\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldPrepareSealDecisionWhenNoHearingAndNoAllocatedJudgeAndNotIssuingJudge() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(replacementSDO)
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(replacementSDO)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the allocated judge\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldPrepareSealDecisionWhenAllocatedJudgeIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(replacementSDO)
                .allocatedJudge(testJudge())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(replacementSDO)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldPrepareSealDecisionWhenFirstCaseManagementHearingIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(replacementSDO)
                .hearingDetails(wrapElements(testHearing(CASE_MANAGEMENT)))
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(replacementSDO)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the allocated judge\n\n"
                    + "* the judge issuing the order")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldPrepareSealDecisionWhenIssuingJudgeIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(replacementSDO)
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderIssuingJudge(testJudgeAndLegalAdviser())
                    .build())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(replacementSDO)
                .nextSteps("## Next steps\n\n"
                    + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                    + "You cannot seal and send the order until adding:\n\n"
                    + "* the first hearing details\n\n"
                    + "* the allocated judge")
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }

        @Test
        void shouldPrepareSealDecisionWhenAllRequiredInformationIsPresent() {
            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderRouter(uploadRoute)
                .replacementSDO(replacementSDO)
                .allocatedJudge(testJudge())
                .hearingDetails(wrapElements(testHearing(CASE_MANAGEMENT)))
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderIssuingJudge(testJudgeAndLegalAdviser())
                    .build())
                .build();

            final GatekeepingOrderSealDecision actualSealDecision = underTest.buildSealDecision(caseData);

            final GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
                .draftDocument(replacementSDO)
                .nextSteps(null)
                .dateOfIssue(time.now().toLocalDate())
                .orderStatus(null)
                .build();

            assertThat(actualSealDecision).isEqualTo(expectedSealDecision);
        }
    }

    @Nested
    class OrderFromUploadedFile {

        final DocumentReference uploadedOrder = TestDataHelper.testDocumentReference();
        final DocumentReference sealedOrder = TestDataHelper.testDocumentReference();

        final String userName = "John Smith";

        @BeforeEach
        void init() {
            when(userService.getUserName()).thenReturn(userName);
            when(sealingService.sealDocument(uploadedOrder)).thenReturn(sealedOrder);
        }

        @Test
        void shouldBuildGatekeepingOrderWhenDecisionIsToKeepOrderAsDraft() {
            final GatekeepingOrderSealDecision sealDecision = GatekeepingOrderSealDecision.builder()
                .orderStatus(DRAFT)
                .draftDocument(uploadedOrder)
                .dateOfIssue(time.now().toLocalDate())
                .build();

            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderSealDecision(sealDecision)
                    .build())
                .build();

            final StandardDirectionOrder actualOrder = underTest.buildOrderFromUploadedFile(caseData);

            final StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder()
                .orderStatus(DRAFT)
                .dateOfUpload(time.now().toLocalDate())
                .uploader(userName)
                .orderDoc(uploadedOrder)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
                .build();

            assertThat(actualOrder).isEqualTo(expectedOrder);

            verifyNoInteractions(sealingService);
        }

        @Test
        void shouldBuildGatekeepingOrderWhenDecisionIsToSealOrder() {

            final GatekeepingOrderSealDecision sealDecision = GatekeepingOrderSealDecision.builder()
                .orderStatus(SEALED)
                .draftDocument(uploadedOrder)
                .build();

            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderSealDecision(sealDecision)
                    .build())
                .build();

            final StandardDirectionOrder actualOrder = underTest.buildOrderFromUploadedFile(caseData);

            final StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder()
                .orderStatus(SEALED)
                .dateOfUpload(time.now().toLocalDate())
                .uploader(userName)
                .orderDoc(sealedOrder)
                .lastUploadedOrder(uploadedOrder)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
                .build();

            assertThat(actualOrder).isEqualTo(expectedOrder);

            verify(sealingService).sealDocument(uploadedOrder);
        }
    }

    @Nested
    class OrderFromGeneratedFile {

        final DocumentReference draftOrder = TestDataHelper.testDocumentReference();

        final Document generatedDocument = TestDataHelper.testDocument();

        final DocumentReference generatedOrder = DocumentReference.builder()
            .url(generatedDocument.links.self.href)
            .binaryUrl(generatedDocument.links.binary.href)
            .filename(generatedDocument.originalDocumentName)
            .build();

        @BeforeEach
        void init() {
            final DocmosisStandardDirectionOrder docmosisOrder = DocmosisStandardDirectionOrder.builder()
                .ccdCaseNumber("1")
                .build();

            when(gatekeepingOrderGenerationService.getTemplateData(any()))
                .thenReturn(docmosisOrder);
            when(documentService.getDocumentFromDocmosisOrderTemplate(docmosisOrder, SDO))
                .thenReturn(generatedDocument);
        }

        @Test
        void shouldBuildGatekeepingOrderWhenDecisionIsToKeepOrderAsDraft() {
            final GatekeepingOrderSealDecision sealDecision = GatekeepingOrderSealDecision.builder()
                .orderStatus(DRAFT)
                .draftDocument(draftOrder)
                .build();

            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderSealDecision(sealDecision)
                    .build())
                .build();

            final StandardDirectionOrder actualOrder = underTest.buildOrderFromGeneratedFile(caseData);

            final StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder()
                .orderStatus(DRAFT)
                .orderDoc(draftOrder)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
                .build();

            assertThat(actualOrder).isEqualTo(expectedOrder);

            verifyNoInteractions(sealingService, gatekeepingOrderGenerationService, documentService);
        }

        @Test
        void shouldBuildGatekeepingOrderWhenDecisionIsToSealOrder() {
            final GatekeepingOrderSealDecision sealDecision = GatekeepingOrderSealDecision.builder()
                .orderStatus(SEALED)
                .draftDocument(draftOrder)
                .dateOfIssue(time.now().toLocalDate())
                .build();

            final CaseData caseData = CaseData.builder()
                .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                    .gatekeepingOrderSealDecision(sealDecision)
                    .build())
                .build();

            final StandardDirectionOrder actualOrder = underTest.buildOrderFromGeneratedFile(caseData);

            final StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder()
                .orderStatus(SEALED)
                .orderDoc(generatedOrder)
                .unsealedDocumentCopy(draftOrder)
                .dateOfIssue(formatLocalDateToString(time.now().toLocalDate(), DateFormatterHelper.DATE))
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
                .build();

            assertThat(actualOrder).isEqualTo(expectedOrder);

            verifyNoInteractions(sealingService);
            verify(gatekeepingOrderGenerationService).getTemplateData(caseData);
        }

    }
}
