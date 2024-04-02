package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C33InterimCareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C33_INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class C33InterimCareOrderDocumentParameterGeneratorTest {
    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final String CHILD_GRAMMAR = "child is";
    private static final String CHILDREN_GRAMMAR = "children are";
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Sheffield City Council";
    private static final GeneratedOrderType TYPE = GeneratedOrderType.CARE_ORDER;
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final String EXCLUSION_DETAILS = "reasons are x y and z";
    private static final String ORDER_HEADER = "Care order restrictions";
    private static final String ORDER_MESSAGE = "Care order message";
    private static final Map<String, String> CHILD_CONTEXT_ELEMENTS = new HashMap<>(Map.of(
        "childOrChildren", "child",
        "childIsOrAre", "is",
        "localAuthorityName", LA_NAME));

    private static final Map<String, String> CHILDREN_CONTEXT_ELEMENTS = new HashMap<>(Map.of(
        "childOrChildren", "children",
        "childIsOrAre", "are",
        "localAuthorityName", LA_NAME));

    private static final LocalDateTime NEXT_WEEK_DATE_TIME = time.now().plusDays(7);
    private String dayOrdinalSuffix;
    private String courtOrderMessage;

    private final ManageOrderDocumentService manageOrderDocumentService = mock(ManageOrderDocumentService.class);
    private final OrderMessageGenerator orderMessageGenerator = mock(OrderMessageGenerator.class);
    private final LocalAuthorityNameLookupConfiguration laNameLookup =
        mock(LocalAuthorityNameLookupConfiguration.class);

    private C33InterimCareOrderDocumentParameterGenerator underTest =
        new C33InterimCareOrderDocumentParameterGenerator(
            laNameLookup, orderMessageGenerator, new OrderDetailsWithEndTypeGenerator(manageOrderDocumentService)
        );

    @BeforeEach
    void setUp() {
        when(orderMessageGenerator.getCareOrderRestrictions(any())).thenReturn(ORDER_MESSAGE);
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
    }

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C33_INTERIM_CARE_ORDER);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    @Test
    void shouldReturnContentForChildAndSpecifiedDateWithExclusion() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithDateSpecified(true);

        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildAndSpecifiedDateWithExclusionEvenWhenCaseLocalAuthorityDoesntExist() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithNoCaseLocalAuthority();

        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildAndSpecifiedDateWithOutException() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithDateSpecified(false);

        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndSpecifiedDateWithExclusion() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithDateSpecified(true);
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILDREN_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILDREN_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndSpecifiedDateWithoutException() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithDateSpecified(false);
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILDREN_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILDREN_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildAndSpecifiedDateAndTimeWithExclusion() {
        CaseData caseData = buildCaseDataWithDateTimeSpecified(true);
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_TIME_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );
        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildAndSpecifiedDateAndTimeWithOutExclusion() {
        CaseData caseData = buildCaseDataWithDateTimeSpecified(false);
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_TIME_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );
        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndSpecifiedDateAndTimeWithExclusion() {
        CaseData caseData = buildCaseDataWithDateTimeSpecified(true);
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_TIME_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );
        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILDREN_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILDREN_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndSpecifiedDateAndTimeWithoutException() {
        CaseData caseData = buildCaseDataWithDateTimeSpecified(false);
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_TIME_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );
        String courtOrderMessage = getChildMessageForDate(formattedDate, CHILDREN_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILDREN_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildAndEndOfProceedingsWithExclusion() {

        CaseData caseData = buildCaseDataWithEndOfProceedings(true);

        courtOrderMessage = getChildMessageForEndOfProceedings(CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildAndEndOfProceedingsWithOutException() {

        CaseData caseData = buildCaseDataWithEndOfProceedings(false);

        courtOrderMessage = getChildMessageForEndOfProceedings(CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndEndOfProceedingsWithExclusion() {

        CaseData caseData = buildCaseDataWithEndOfProceedings(true);

        courtOrderMessage = getChildMessageForEndOfProceedings(CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndEndOfProceedingsWithoutException() {

        CaseData caseData = buildCaseDataWithEndOfProceedings(false);

        courtOrderMessage = getChildMessageForEndOfProceedings(CHILD_GRAMMAR);

        when(manageOrderDocumentService.commonContextElements(caseData)).thenReturn(CHILD_CONTEXT_ELEMENTS);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private String getChildMessageForDate(String formattedDate, String childGrammar) {
        return "The Court orders that the " + childGrammar
            + " placed in the care of " + LA_NAME
            + " until " + formattedDate + ".";
    }

    private String getChildMessageForEndOfProceedings(String childGrammar) {
        return "The Court orders that the " + childGrammar
            + " placed in the care of " + LA_NAME
            + " until the end of the proceedings or until a further order is made.";
    }

    private C33InterimCareOrderDocmosisParameters.C33InterimCareOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParameters(boolean hasExclusionDetails) {
        if (hasExclusionDetails) {
            return C33InterimCareOrderDocmosisParameters.builder()
                .orderTitle(Order.C33_INTERIM_CARE_ORDER.getTitle())
                .orderType(TYPE)
                .orderHeader(ORDER_HEADER)
                .orderMessage(ORDER_MESSAGE)
                .furtherDirections(FURTHER_DIRECTIONS)
                .exclusionClause(EXCLUSION_DETAILS)
                .localAuthorityName(LA_NAME);
        } else {
            return C33InterimCareOrderDocmosisParameters.builder()
                .orderTitle(Order.C33_INTERIM_CARE_ORDER.getTitle())
                .orderType(TYPE)
                .orderHeader(ORDER_HEADER)
                .orderMessage(ORDER_MESSAGE)
                .furtherDirections(FURTHER_DIRECTIONS)
                .localAuthorityName(LA_NAME);
        }

    }

    private CaseData buildCaseDataWithDateSpecified(boolean hasExclusion) {
        if (hasExclusion) {
            return CaseData.builder()
                .caseLocalAuthority(LA_CODE)
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersApprovalDate(time.now().toLocalDate())
                    .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                    .manageOrdersExclusionDetails(EXCLUSION_DETAILS)
                    .manageOrdersType(C33_INTERIM_CARE_ORDER)
                    .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY)
                    .manageOrdersSetDateEndDate(NEXT_WEEK_DATE_TIME.toLocalDate())
                    .build())
                .build();
        } else {
            return CaseData.builder()
                .caseLocalAuthority(LA_CODE)
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersApprovalDate(time.now().toLocalDate())
                    .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                    .manageOrdersType(C33_INTERIM_CARE_ORDER)
                    .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY)
                    .manageOrdersSetDateEndDate(NEXT_WEEK_DATE_TIME.toLocalDate())
                    .build())
                .build();
        }
    }

    private CaseData buildCaseDataWithNoCaseLocalAuthority() {
        return CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LA_NAME)
                .build()))
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(time.now().toLocalDate())
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersExclusionDetails(EXCLUSION_DETAILS)
                .manageOrdersType(C33_INTERIM_CARE_ORDER)
                .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(NEXT_WEEK_DATE_TIME.toLocalDate())
                .build())
            .build();
    }

    private CaseData buildCaseDataWithDateTimeSpecified(boolean hasExclusionDetails) {
        if (hasExclusionDetails) {
            return CaseData.builder()
                .caseLocalAuthority(LA_CODE)
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersApprovalDate(time.now().toLocalDate())
                    .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                    .manageOrdersType(C33_INTERIM_CARE_ORDER)
                    .manageOrdersExclusionDetails(EXCLUSION_DETAILS)
                    .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY_AND_TIME)
                    .manageOrdersSetDateAndTimeEndDate(NEXT_WEEK_DATE_TIME)
                    .build())
                .build();
        } else {
            return CaseData.builder()
                .caseLocalAuthority(LA_CODE)
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersApprovalDate(time.now().toLocalDate())
                    .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                    .manageOrdersType(C33_INTERIM_CARE_ORDER)
                    .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY_AND_TIME)
                    .manageOrdersSetDateAndTimeEndDate(NEXT_WEEK_DATE_TIME)
                    .build())
                .build();
        }
    }

    private CaseData buildCaseDataWithEndOfProceedings(boolean hasExclusionDetails) {
        if (hasExclusionDetails) {
            return CaseData.builder()
                .caseLocalAuthority(LA_CODE)
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersApprovalDate(time.now().toLocalDate())
                    .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                    .manageOrdersExclusionDetails(EXCLUSION_DETAILS)
                    .manageOrdersType(C33_INTERIM_CARE_ORDER)
                    .manageOrdersEndDateTypeWithEndOfProceedings(END_OF_PROCEEDINGS)
                    .build())
                .build();
        } else {
            return CaseData.builder()
                .caseLocalAuthority(LA_CODE)
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersApprovalDate(time.now().toLocalDate())
                    .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                    .manageOrdersType(C33_INTERIM_CARE_ORDER)
                    .manageOrdersEndDateTypeWithEndOfProceedings(END_OF_PROCEEDINGS)
                    .build())
                .build();
        }

    }
}
