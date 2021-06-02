package uk.gov.hmcts.reform.fpl.service.orders.generator.generics;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35aSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35bInterimSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.buildCaseData;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.buildCaseDataWithCalendarDaySpecified;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.buildCaseDataWithDateTimeSpecified;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.getChildSupervisionMessageWithDate;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.getChildSupervisionMessageWithEndOfProceedings;

class ManageOrderWithEndOfProceedingsDocumentParameterGeneratorTest {
    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final LocalDateTime NEXT_WEEK_DATE_TIME = time.now().plusDays(7);
    private static final Child CHILD = mock(Child.class);
    private static final String CHILD_GRAMMAR = "child";
    private static final String CHILDREN_GRAMMAR = "children";
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Sheffield City Council";
    private static final String FURTHER_DIRECTIONS = "Further directions ";
    private String dayOrdinalSuffix;
    private String courtOrderMessage;

    // Instance Check
    private static final DocmosisTemplates TEMPLATE = DocmosisTemplates.ORDER;
    private static final Order ORDER = Order.C35B_INTERIM_SUPERVISION_ORDER;
    private static C35bInterimSupervisionOrderDocmosisParameters c35bInterimSupervisionOrderDocmosisParameters;

    @Mock
    private ChildrenService childrenService;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    private final ManageOrderWithEndOfProceedingsDocumentParameterGenerator instance = getTestInstance();

    private ManageOrderWithEndOfProceedingsDocumentParameterGenerator getTestInstance() {
        return new ManageOrderWithEndOfProceedingsDocumentParameterGenerator(childrenService,laNameLookup) {

            @Override
            public Order accept() {
                return ORDER;
            }

            @Override
            public DocmosisParameters generate(CaseData caseData) {
                return super.generate(caseData);
            }

            @Override
            protected DocmosisParameters docmosisParameters(ManageOrdersEventData eventData,
                                                            String localAuthorityCode,
                                                            String localAuthorityName,
                                                            List<Element<Child>> selectedChildren) {
                return C35bInterimSupervisionOrderDocmosisParameters.builder()
                    .orderTitle(ORDER.getTitle())
                    .orderType(GeneratedOrderType.SUPERVISION_ORDER)
                    .orderDetails(orderDetails(selectedChildren.size(), localAuthorityName, eventData))
                    .furtherDirections(eventData.getManageOrdersFurtherDirections())
                    .build();
            }

            @Override
            public DocmosisTemplates template() {
                return TEMPLATE;
            }

            @Override
            protected String orderDetails(
                int numOfChildren, String localAuthorityName, ManageOrdersEventData eventData) {
                return super.orderDetails(numOfChildren, localAuthorityName, eventData);
            }
        };
    }

    @Test
    void shouldReturnOrderDetailsForChildWithEndDateType_CalendarDay() {
        CaseData caseData = buildCaseDataWithCalendarDaySpecified(LA_CODE);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        int numberOfChildren = 1;

        ManageOrdersEventData manageOrdersEventData = buildManageOrdersCaseData(CALENDAR_DAY);

        assertThat(instance.orderDetails(numberOfChildren,LA_NAME, manageOrdersEventData))
            .isEqualTo(getChildSupervisionMessageWithDate(LA_NAME, numberOfChildren, false, eventData));
    }

    @Test
    void shouldReturnOrderDetailsForChildrenWithEndDateType_CalendarDay() {
        CaseData caseData = buildCaseDataWithCalendarDaySpecified(LA_CODE);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        int numberOfChildren = 2;

        ManageOrdersEventData manageOrdersEventData = buildManageOrdersCaseData(CALENDAR_DAY);

        assertThat(instance.orderDetails(numberOfChildren,LA_NAME, manageOrdersEventData))
            .isEqualTo(getChildSupervisionMessageWithDate(LA_NAME, numberOfChildren, false, eventData));
    }

    @Test
    void shouldReturnOrderDetailsForChildWithEndDateType_CalendarDayAndTime() {
        CaseData caseData = buildCaseDataWithDateTimeSpecified(LA_CODE);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        int numberOfChildren = 1;

        ManageOrdersEventData manageOrdersEventData = buildManageOrdersCaseData(CALENDAR_DAY_AND_TIME);

        assertThat(instance.orderDetails(numberOfChildren,LA_NAME, manageOrdersEventData))
            .isEqualTo(getChildSupervisionMessageWithDate(LA_NAME, numberOfChildren, true, eventData));
    }

    @Test
    void shouldReturnOrderDetailsForChildrenWithEndDateType_CalendarDayAndTime() {
        CaseData caseData = buildCaseDataWithDateTimeSpecified(LA_CODE);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        int numberOfChildren = 2;

        ManageOrdersEventData manageOrdersEventData = buildManageOrdersCaseData(CALENDAR_DAY_AND_TIME);

        assertThat(instance.orderDetails(numberOfChildren,LA_NAME, manageOrdersEventData))
            .isEqualTo(getChildSupervisionMessageWithDate(LA_NAME, numberOfChildren,true, eventData));
    }

    @Test
    void shouldReturnOrderDetailsForChildWithEndDateType_EndOfProceedings() {
        CaseData caseData = buildCaseData(LA_CODE, END_OF_PROCEEDINGS);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        int numberOfChildren = 1;

        ManageOrdersEventData manageOrdersEventData = buildManageOrdersCaseData(END_OF_PROCEEDINGS);

        assertThat(instance.orderDetails(numberOfChildren,LA_NAME, manageOrdersEventData))
            .isEqualTo(getChildSupervisionMessageWithEndOfProceedings(LA_NAME, numberOfChildren));
    }

    @Test
    void shouldReturnOrderDetailsForChildrenWithEndDateType_EndOfProceedings() {
        CaseData caseData = buildCaseData(LA_CODE, END_OF_PROCEEDINGS);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        int numberOfChildren = 2;

        ManageOrdersEventData manageOrdersEventData = buildManageOrdersCaseData(END_OF_PROCEEDINGS);

        assertThat(instance.orderDetails(numberOfChildren,LA_NAME, manageOrdersEventData))
            .isEqualTo(getChildSupervisionMessageWithEndOfProceedings(LA_NAME, numberOfChildren));
    }


    private C35aSupervisionOrderDocmosisParameters.C35aSupervisionOrderDocmosisParametersBuilder<?,?>
        expectedCommonParameters() {
        return C35aSupervisionOrderDocmosisParameters.builder()
            .orderTitle(any())
            .orderType(any())
            .furtherDirections(FURTHER_DIRECTIONS);
    }

    private ManageOrdersEventData buildManageOrdersCaseDataDate() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(time.now())
            .manageOrdersEndDateTypeWithEndOfProceedings(END_OF_PROCEEDINGS)
            .manageOrdersChildrenDescription("first1 last1")
            .manageOrdersFurtherDirections("test further directions")
            .build();

        return manageOrdersEventData;
    }

    private ManageOrdersEventData buildManageOrdersCaseData(ManageOrdersEndDateType type) {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(time.now())
            .manageOrdersEndDateTypeWithEndOfProceedings(type)
            .manageOrdersSetDateEndDate(time.now().plusDays(7).toLocalDate())
            .manageOrdersSetDateAndTimeEndDate(time.now().plusDays(7))
            .manageOrdersFurtherDirections("test further directions")
            .build();

        return manageOrdersEventData;
    }

    private ManageOrdersEventData buildManageOrdersCaseDataEndOfProceedings() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(time.now())
            .manageOrdersEndDateTypeWithEndOfProceedings(END_OF_PROCEEDINGS)
            .manageOrdersFurtherDirections("test further directions")
            .build();
        return manageOrdersEventData;
    }

}
