package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35bInterimSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35B_INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C35bISODocumentParameterGeneratorTest {
    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final Child CHILD = mock(Child.class);
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Test Sheffield City Council";
    private static final String TEST_FURTHER_DIRECTIONS = "Test Further directions";
    private static final LocalDate TEST_APPROVAL_DATE = LocalDate.of(2021,6,1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2021,6,2);
    private static final LocalDateTime TEST_END_DATE_TIME = LocalDateTime.of(2021,6,2,9,0);

    private static final DocmosisTemplates TEMPLATE = DocmosisTemplates.ORDER;
    private static final Order ORDER = C35B_INTERIM_SUPERVISION_ORDER;
    private static C35bInterimSupervisionOrderDocmosisParameters c35bInterimSupervisionOrderDocmosisParameters;

    @Mock
    private ChildrenService childrenService;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C35bISODocumentParameterGenerator underTest;

    @Test
    void shouldReturnAcceptedOrder() {
        Order order = underTest.accept();

        assertThat(order).isEqualTo(ORDER);
    }

    @Test
    public void shouldReturnTemplate() {
        DocmosisTemplates returnedTemplate = underTest.template();

        assertThat(TEMPLATE).isEqualTo(returnedTemplate);
    }

    @Test
    public void shouldReturnDocmosisParametersForOneChildAndCalendayDay() {
        int numOfChildren = 1;
        CaseData caseData = buildCaseData(LA_CODE, CALENDAR_DAY);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(
            manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren
        );

        assertThat(parameters).isEqualTo(buildDocmosisParameters(manageOrdersEventData, CALENDAR_DAY, numOfChildren));
    }

    @Test
    public void shouldReturnDocmosisParametersForMultipleChrildrenAndCalendayDay() {
        int numOfChildren = 4;
        CaseData caseData = buildCaseData(LA_CODE, CALENDAR_DAY);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(
            manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren
        );

        assertThat(parameters)
            .isEqualTo(buildDocmosisParameters(manageOrdersEventData, CALENDAR_DAY, numOfChildren));
    }

    @Test
    public void shouldReturnDocmosisParametersForOneChildAndCalendayDayTime() {
        int numOfChildren = 1;
        CaseData caseData = buildCaseData(LA_CODE, CALENDAR_DAY_AND_TIME);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(
            manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren
        );

        assertThat(parameters)
            .isEqualTo(buildDocmosisParameters(manageOrdersEventData, CALENDAR_DAY_AND_TIME, numOfChildren));
    }

    @Test
    public void shouldReturnDocmosisParametersForMultipleChrildrenAndCalendayDayTime() {
        int numOfChildren = 4;
        CaseData caseData = buildCaseData(LA_CODE, CALENDAR_DAY_AND_TIME);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(
            manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren
        );

        assertThat(parameters)
            .isEqualTo(buildDocmosisParameters(manageOrdersEventData, CALENDAR_DAY_AND_TIME, numOfChildren));
    }

    @Test
    public void shouldReturnDocmosisParametersForOneChildAndEndOfProceedings() {
        int numOfChildren = 1;
        CaseData caseData = buildCaseData(LA_CODE, END_OF_PROCEEDINGS);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(
            manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren
        );

        assertThat(parameters)
            .isEqualTo(buildDocmosisParameters(manageOrdersEventData, END_OF_PROCEEDINGS, numOfChildren));
    }

    @Test
    public void shouldReturnDocmosisParametersForMultipleChrildrenAndEndOfProceedings() {
        int numOfChildren = 4;
        CaseData caseData = buildCaseData(LA_CODE, END_OF_PROCEEDINGS);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(
            manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren
        );

        assertThat(parameters)
            .isEqualTo(buildDocmosisParameters(manageOrdersEventData, END_OF_PROCEEDINGS, numOfChildren));
    }

    private C35bInterimSupervisionOrderDocmosisParameters buildDocmosisParameters(
        ManageOrdersEventData manageOrdersEventData, ManageOrdersEndDateType type, int numOfChildren) {

        String formatString;
        LocalDateTime orderExpiration;

        String content;
        String childContent = (numOfChildren == 1) ? "child" : "children";
        final String dayOrdinalSuffix = getDayOfMonthSuffix(TEST_END_DATE.getDayOfMonth());

        switch (type) {
            // The DATE_WITH_ORDINAL_SUFFIX format ignores the time, so that it will not display even if captured.
            case CALENDAR_DAY:
                formatString = DATE_WITH_ORDINAL_SUFFIX;
                orderExpiration = LocalDateTime.of(TEST_END_DATE, LocalTime.MIDNIGHT);

                content = format("The Court orders %s supervises the %s until %s.",
                    LA_NAME,
                    childContent,
                    formatLocalDateTimeBaseUsingFormat(
                        TEST_END_DATE_TIME,
                        String.format(formatString, dayOrdinalSuffix)
                    )
                );
                break;
            case CALENDAR_DAY_AND_TIME:
                formatString = DATE_TIME_WITH_ORDINAL_SUFFIX;
                orderExpiration = TEST_END_DATE_TIME;

                content = format("The Court orders %s supervises the %s until %s.",
                    LA_NAME,
                    childContent,
                    formatLocalDateTimeBaseUsingFormat(
                        TEST_END_DATE_TIME,
                        String.format(formatString, dayOrdinalSuffix)
                    )
                );
                break;
            case END_OF_PROCEEDINGS:
                content = format("The Court orders %s supervises the %s until " +
                        "the end of the proceedings or until a further order is made.", LA_NAME, childContent);
                break;
            default:
                throw new IllegalStateException("Unexpected order event data type: " + type);
        }

        return C35bInterimSupervisionOrderDocmosisParameters.builder()
            .orderTitle(ORDER.getTitle())
            .orderType(GeneratedOrderType.SUPERVISION_ORDER)
            .furtherDirections(TEST_FURTHER_DIRECTIONS)
            .orderDetails(content)
            .build();
    }

    public static CaseData buildCaseData(String laCode, ManageOrdersEndDateType type) {
        return CaseData.builder()
            .caseLocalAuthority(laCode)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(TEST_APPROVAL_DATE)
                .manageOrdersFurtherDirections(TEST_FURTHER_DIRECTIONS)
                .manageOrdersEndDateTypeWithEndOfProceedings(type)
                .manageOrdersSetDateEndDate(TEST_END_DATE)
                .manageOrdersSetDateAndTimeEndDate(TEST_END_DATE_TIME)
                .build())
            .build();
    }
}
