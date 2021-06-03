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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35B_INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C35bISODocumentParameterGeneratorTest {
    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final LocalDateTime NEXT_WEEK_DATE_TIME = time.now().plusDays(7);
    private static final Child CHILD = mock(Child.class);
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Test Sheffield City Council";
    private static final String FURTHER_DIRECTIONS = "Test Further directions";
    private static final LocalDate TEST_APPROVAL_DATE = LocalDate.of(2021,6,1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2021,6,2);

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
    public void shouldReturnDocmosisParametersForOneChild() {
        int numOfChildren = 1;
        CaseData caseData = buildCaseData(LA_CODE);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren);

        assertThat(parameters).isEqualTo(buildDocmosisParameters(manageOrdersEventData, CALENDAR_DAY, numOfChildren));
    }

    @Test
    public void shouldReturnDocmosisParametersForMultipleChrildren() {
        int numOfChildren = 4;
        CaseData caseData = buildCaseData(LA_CODE);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        DocmosisParameters parameters = underTest.docmosisParameters(manageOrdersEventData, LA_CODE, LA_NAME, numOfChildren);

        assertThat(parameters).isEqualTo(buildDocmosisParameters(manageOrdersEventData, CALENDAR_DAY, numOfChildren));
    }

    private C35bInterimSupervisionOrderDocmosisParameters buildDocmosisParameters(ManageOrdersEventData manageOrdersEventData, ManageOrdersEndDateType type, int numOfChildren) {
        String content;
        String childContent = (numOfChildren == 1) ? "child" : "children";

        switch (type) {
            // The DATE_WITH_ORDINAL_SUFFIX format ignores the time, so that it will not display even if captured.
            case CALENDAR_DAY:
                content = "The Court orders Test Sheffield City Council supervises the " + childContent + " until 2nd June 2021.";
                break;
            case CALENDAR_DAY_AND_TIME:
                content = "The Court orders Test Sheffield City Council supervises the " + childContent + " until 2nd June 2021 at 9.";
                break;
            case END_OF_PROCEEDINGS:
                content = "The Court orders Test Sheffield City Council supervises the " + childContent + " until " +
                    "the end of the proceedings or until a further order is made.";
                break;
            default:
                throw new IllegalStateException("Unexpected order event data type: " + type);
        }

        return C35bInterimSupervisionOrderDocmosisParameters.builder()
            .orderTitle(ORDER.getTitle())
            .orderType(GeneratedOrderType.SUPERVISION_ORDER)
            .furtherDirections(FURTHER_DIRECTIONS)
            .orderDetails(content)
            .build();
    }

    public static CaseData buildCaseData(String laCode) {
        return CaseData.builder()
            .caseLocalAuthority(laCode)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(TEST_APPROVAL_DATE)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(TEST_END_DATE)
                .build())
            .build();
    }
}
