package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35aSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class C35aSupervisionOrderDocumentParameterGeneratorTest {
    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final String CHILD_GRAMMAR = "child";
    private static final String CHILDREN_GRAMMAR = "children";
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final Child CHILD = mock(Child.class);
    private static final GeneratedOrderType TYPE = GeneratedOrderType.SUPERVISION_ORDER;
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final LocalDateTime NEXT_WEEK_DATE_TIME = time.now().plusDays(7);
    private String dayOrdinalSuffix;
    private String courtOrderMessage;

    private final ChildrenService childrenService = mock(ChildrenService.class);
    private final LocalAuthorityNameLookupConfiguration laNameLookup =
        mock(LocalAuthorityNameLookupConfiguration.class);
    private final OrderDetailsWithEndTypeGenerator orderDetailsWithEndTypeGenerator =
        new OrderDetailsWithEndTypeGenerator(
            childrenService,
            laNameLookup);

    private C35aSupervisionOrderDocumentParameterGenerator underTest =
        new C35aSupervisionOrderDocumentParameterGenerator(
            orderDetailsWithEndTypeGenerator
        );

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C35A_SUPERVISION_ORDER);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    @Test
    void shouldReturnContentForSingleChildAndSpecifiedDate() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithDateSpecified();

        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        courtOrderMessage = getSingularChildMessageDate(formattedDate);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);


        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndSpecifiedDate() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithDateSpecified();
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        courtOrderMessage = getMultipleChildMessageDate(formattedDate);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForSingleChildAndMonthsSpecified() {
        Integer numOfMonths = 4;
        LocalDateTime futureDate = time.now().plusMonths(numOfMonths);
        dayOrdinalSuffix = getDayOfMonthSuffix(futureDate.getDayOfMonth());
        CaseData caseData = buildCaseDataWithMonthsSpecified(numOfMonths);

        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            futureDate,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        courtOrderMessage = getSingularChildMessageMonths(formattedDate, numOfMonths);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnContentForChildrenAndMonthsSpecified() {
        Integer numOfMonths = 4;
        LocalDateTime futureDate = time.now().plusMonths(numOfMonths);
        dayOrdinalSuffix = getDayOfMonthSuffix(futureDate.getDayOfMonth());
        CaseData caseData = buildCaseDataWithMonthsSpecified(numOfMonths);

        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            futureDate,
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        courtOrderMessage = getMultipleChildMessageMonths(formattedDate, numOfMonths);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnMessageForChildAndSetDateAndTime() {
        CaseData caseData = buildCaseDataWithDateTimeSpecified();
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_TIME_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );
        String courtOrderMessage = getSingularChildMessageDate(formattedDate);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnMessageForChildrenAndSetDateAndTime() {
        dayOrdinalSuffix = getDayOfMonthSuffix(NEXT_WEEK_DATE_TIME.getDayOfMonth());
        CaseData caseData = buildCaseDataWithDateTimeSpecified();
        String formattedDate = formatLocalDateTimeBaseUsingFormat(
            NEXT_WEEK_DATE_TIME,
            String.format(DATE_TIME_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix)
        );

        courtOrderMessage = getMultipleChildMessageDate(formattedDate);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(courtOrderMessage)
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private String getMultipleChildMessageDate(String formattedDate) {
        return "The Court orders " + LA_NAME
            + " supervises the " + CHILDREN_GRAMMAR
            + " until " + formattedDate + ".";
    }

    private String getMultipleChildMessageMonths(String formattedDate, int numOfMonths) {
        return "The Court orders " + LA_NAME
            + " supervises the " + CHILDREN_GRAMMAR
            + " for " + numOfMonths + " months from the date of this order"
            + " until " + formattedDate + ".";
    }

    private String getSingularChildMessageDate(String formattedDate) {
        return "The Court orders " + LA_NAME
            + " supervises the " + CHILD_GRAMMAR
            + " until " + formattedDate + ".";
    }

    private String getSingularChildMessageMonths(String formattedDate, int numOfMonths) {
        return "The Court orders " + LA_NAME
            + " supervises the " + CHILD_GRAMMAR
            + " for " + numOfMonths + " months from the date of this order"
            + " until " + formattedDate + ".";
    }

    private C35aSupervisionOrderDocmosisParameters.C35aSupervisionOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParameters() {
        return C35aSupervisionOrderDocmosisParameters.builder()
            .orderTitle(Order.C35A_SUPERVISION_ORDER.getTitle())
            .orderType(TYPE)
            .furtherDirections(FURTHER_DIRECTIONS);
    }

    private CaseData buildCaseDataWithDateSpecified() {
        return CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersType(C35A_SUPERVISION_ORDER)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(NEXT_WEEK_DATE_TIME.toLocalDate())
                .build())
            .build();
    }

    private CaseData buildCaseDataWithDateTimeSpecified() {

        return CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersType(C35A_SUPERVISION_ORDER)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(NEXT_WEEK_DATE_TIME)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithMonthsSpecified(Integer numOfMonths) {
        return CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(time.now().toLocalDate())
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersType(C35A_SUPERVISION_ORDER)
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(numOfMonths)
                .build())
            .build();
    }
}
