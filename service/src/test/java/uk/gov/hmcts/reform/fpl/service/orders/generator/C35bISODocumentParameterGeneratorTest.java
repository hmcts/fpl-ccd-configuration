package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35B_INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.buildCaseData;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.buildCaseDataWithCalendarDaySpecified;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.buildCaseDataWithDateTimeSpecified;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.getChildSupervisionMessageWithDate;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.commons.ManageOrdersHelper.getChildSupervisionMessageWithEndOfProceedings;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith({MockitoExtension.class})
class C35bISODocumentParameterGeneratorTest {
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
}
