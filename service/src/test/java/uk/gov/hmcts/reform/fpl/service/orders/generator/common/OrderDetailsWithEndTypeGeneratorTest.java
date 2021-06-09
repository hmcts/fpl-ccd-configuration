package uk.gov.hmcts.reform.fpl.service.orders.generator.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class OrderDetailsWithEndTypeGeneratorTest {

    private static final String LA_CODE = "LACODE";
    private static final String LA_NAME = "LA_NAME";
    private static final LocalDate MANAGE_ORDERS_SET_DATE_END_DATE = LocalDate.of(2012, 12, 20);
    private static final LocalDateTime MANAGE_ORDERS_SET_DATE_END_DATE_TIME = LocalDateTime.of(2012,
        12,
        20,
        13,
        21,
        03);
    private static final int MONTHS_END_DATE = 4;
    private static final LocalDate APPROVAL_DATE = LocalDate.of(2013, 11, 8);

    private final ChildrenService childrenService = mock(ChildrenService.class);
    private final LocalAuthorityNameLookupConfiguration laNameLookup = mock(
        LocalAuthorityNameLookupConfiguration.class);

    private final OrderDetailsWithEndTypeGenerator underTest =
        new OrderDetailsWithEndTypeGenerator(childrenService, laNameLookup);

    @BeforeEach
    void setUp() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
    }

    @Test
    void testEmptyTemplate() {
        String actual = underTest.orderDetails(CALENDAR_DAY,
            OrderDetailsWithEndTypeMessages.builder().messageWithSpecifiedTime("").build(),
            CaseData.builder()
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersSetDateEndDate(MANAGE_ORDERS_SET_DATE_END_DATE)
                    .build())
                .caseLocalAuthority(LA_CODE).build());

        assertThat(actual).isEqualTo("");

    }

    @Test
    void testTemplateWithLocalAuthorityName() {
        String actual = underTest.orderDetails(CALENDAR_DAY,
            OrderDetailsWithEndTypeMessages.builder()
                .messageWithSpecifiedTime("blah ${localAuthorityName} blah")
                .build(),
            CaseData.builder()
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersSetDateEndDate(MANAGE_ORDERS_SET_DATE_END_DATE)
                    .build())
                .caseLocalAuthority(LA_CODE).build());

        assertThat(actual).isEqualTo("blah LA_NAME blah");

    }

    @Test
    void testTemplateWithChildOrChildrenSingleChild() {

        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersSetDateEndDate(MANAGE_ORDERS_SET_DATE_END_DATE)
                .build())
            .build();
        when(childrenService.getSelectedChildren(caseData)).thenReturn(List.of(element(mock(Child.class))));

        String actual = underTest.orderDetails(CALENDAR_DAY,
            OrderDetailsWithEndTypeMessages.builder()
                .messageWithSpecifiedTime("blah ${childIsOrAre} ${childOrChildren} blah")
                .build(),
            caseData);

        assertThat(actual).isEqualTo("blah is child blah");

    }

    @Test
    void testTemplateWithChildOrChildrenMultipleChildren() {

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersSetDateEndDate(MANAGE_ORDERS_SET_DATE_END_DATE)
                .build())
            .caseLocalAuthority(LA_CODE).build();
        when(childrenService.getSelectedChildren(caseData)).thenReturn(List.of(
            element(mock(Child.class)),
            element(mock(Child.class))
        ));

        String actual = underTest.orderDetails(CALENDAR_DAY,
            OrderDetailsWithEndTypeMessages.builder()
                .messageWithSpecifiedTime("blah ${childIsOrAre} ${childOrChildren} blah")
                .build(),
            caseData);

        assertThat(actual).isEqualTo("blah are children blah");

    }

    @Test
    void testTemplateWithEndDate() {

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersSetDateEndDate(MANAGE_ORDERS_SET_DATE_END_DATE)
                .build())
            .caseLocalAuthority(LA_CODE)
            .build();

        String actual = underTest.orderDetails(CALENDAR_DAY,
            OrderDetailsWithEndTypeMessages.builder()
                .messageWithSpecifiedTime("blah ${endDate} blah")
                .build(),
            caseData);

        assertThat(actual).isEqualTo("blah 20th December 2012 blah");

    }

    @Test
    void testTemplateWithEndOfProceedings() {

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersSetDateAndTimeEndDate(MANAGE_ORDERS_SET_DATE_END_DATE_TIME)
                .build())
            .caseLocalAuthority(LA_CODE)
            .build();

        String actual = underTest.orderDetails(END_OF_PROCEEDINGS,
            OrderDetailsWithEndTypeMessages.builder()
                .messageWithEndOfProceedings(
                    "blah end of proceedings ${childIsOrAre} ${childOrChildren} ${localAuthorityName} blah")
                .build(),
            caseData);

        assertThat(actual).isEqualTo("blah end of proceedings are children LA_NAME blah");

    }

    @Test
    void testTemplateWithNumberOfMonths() {

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(APPROVAL_DATE)
                .manageOrdersSetMonthsEndDate(MONTHS_END_DATE)
                .build())
            .caseLocalAuthority(LA_CODE)
            .build();

        String actual = underTest.orderDetails(NUMBER_OF_MONTHS,
            OrderDetailsWithEndTypeMessages.builder()
                .messageWithNumberOfMonths(
                    "blah months ${numOfMonths} ${endDate} ${childIsOrAre} ${childOrChildren} ${localAuthorityName} "
                        + "blah"
                ).build(),
            caseData);

        assertThat(actual).isEqualTo("blah months 4 8th March 2014 are children LA_NAME blah");

    }


}
