package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C26SecureAccommodationOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Month.AUGUST;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.GIRL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.Jurisdiction.ENGLAND;
import static uk.gov.hmcts.reform.fpl.enums.Jurisdiction.WALES;
import static uk.gov.hmcts.reform.fpl.enums.ReasonForSecureAccommodation.ABSCOND;
import static uk.gov.hmcts.reform.fpl.enums.ReasonForSecureAccommodation.INJURY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C26_SECURE_ACCOMMODATION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

class C26SecureAccommodationOrderDocumentParameterGeneratorTest {

    private static final String WELSH_ACT_NAME = "Section 119 of the Social Services and Wellbeing (Wales) Act 2014";
    private static final String ENGLISH_ACT_NAME = "Section 25 Children Act 1989";
    private static final String NEW_PARAGRAPH = "\n\n";
    private static final String COURT_AUTHORISATION_PREFIX =
        "The Court authorises Test County to keep the child in secure accommodation ";
    private static final String INJURY_ADVISORY_TEXT = "This order has been made on the ground that if the child is "
        + "kept in any other accommodation the child is likely to injure herself or other persons.";
    private static final String ABSCONDENCE_ADVISORY_TEXT = "This order has been made on the ground that the child "
        + "has a history of absconding and is likely to abscond from any other accommodation, and if the child "
        + "absconds he is likely to suffer significant harm.";
    private static final String CHILD_NOT_REPRESENTED_ADVISORY_TEXT = "The Court was satisfied that the child, not "
        + "being legally represented, has been informed of their right to apply for legal aid and having had the "
        + "opportunity to apply, had refused or failed to apply.";

    private LocalAuthorityNameLookupConfiguration mockLocalAuthorityNameLookupConfiguration;
    private ChildrenSmartSelector mockChildrenSmartSelector;

    private OrderDetailsWithEndTypeGenerator orderDetailsWithEndTypeGenerator;

    private C26SecureAccommodationOrderDocumentParameterGenerator underTest;

    @BeforeEach
    void setUp() {
        mockChildrenSmartSelector = mock(ChildrenSmartSelector.class);
        mockLocalAuthorityNameLookupConfiguration = mock(LocalAuthorityNameLookupConfiguration.class);
        when(mockLocalAuthorityNameLookupConfiguration.getLocalAuthorityName("ABC")).thenReturn("Test County");
        ManageOrderDocumentService manageOrderDocumentService =
            new ManageOrderDocumentService(mockChildrenSmartSelector, mockLocalAuthorityNameLookupConfiguration);

        orderDetailsWithEndTypeGenerator = new OrderDetailsWithEndTypeGenerator(manageOrderDocumentService);

        underTest = new C26SecureAccommodationOrderDocumentParameterGenerator(
            orderDetailsWithEndTypeGenerator, mockChildrenSmartSelector);
    }

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(C26_SECURE_ACCOMMODATION_ORDER);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(ORDER);
    }

    @Test
    void generateWithWelshActNameAndFullerTextExample() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(LocalDateTime.of(2021, 1, 14, 13, 42))
            .manageOrdersIsByConsent("Yes")
            .manageOrdersOrderJurisdiction(WALES)
            .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY)
            .manageOrdersSetDateEndDate(LocalDate.of(2022, 6, 12))
            .manageOrdersReasonForSecureAccommodation(ABSCOND)
            .manageOrdersIsChildRepresented("No")
            .manageOrdersFurtherDirections("Further directions here")
            .build();
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("ABC")
            .manageOrdersEventData(manageOrdersEventData)
            .build();
        when(mockChildrenSmartSelector.getSelectedChildren(caseData)).thenReturn(singletonList(testChild(BOY)));

        C26SecureAccommodationOrderDocmosisParameters result =
            (C26SecureAccommodationOrderDocmosisParameters) underTest.generate(caseData);

        assertThat(result.getChildrenAct()).isEqualTo(WELSH_ACT_NAME);
        assertThat(result.getOrderTitle()).isEqualTo(C26_SECURE_ACCOMMODATION_ORDER.getTitle());
        assertThat(result.getFurtherDirections()).isEqualTo("Further directions here");
        assertThat(result.getOrderDetails()).isEqualTo("By consent, "
            + COURT_AUTHORISATION_PREFIX
            + "until 12th June 2022."
            + NEW_PARAGRAPH
            + ABSCONDENCE_ADVISORY_TEXT
            + NEW_PARAGRAPH
            + CHILD_NOT_REPRESENTED_ADVISORY_TEXT);
    }

    @Test
    void generateWithEnglishActNameAndEmptierTextExample() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(LocalDateTime.of(2021, 1, 14, 13, 42))
            .manageOrdersIsByConsent("No")
            .manageOrdersOrderJurisdiction(ENGLAND)
            .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
            .manageOrdersSetDateAndTimeEndDate(LocalDateTime.of(2023, AUGUST, 13, 18, 05))
            .manageOrdersReasonForSecureAccommodation(INJURY)
            .manageOrdersIsChildRepresented("Yes")
            .build();
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority("ABC")
            .manageOrdersEventData(manageOrdersEventData)
            .build();
        when(mockChildrenSmartSelector.getSelectedChildren(caseData)).thenReturn(singletonList(testChild(GIRL)));

        C26SecureAccommodationOrderDocmosisParameters result =
            (C26SecureAccommodationOrderDocmosisParameters) underTest.generate(caseData);

        assertThat(result.getChildrenAct()).isEqualTo(ENGLISH_ACT_NAME);
        assertThat(result.getOrderDetails()).isEqualTo(COURT_AUTHORISATION_PREFIX
            + "until 6:05pm on the 13th August 2023."
            + NEW_PARAGRAPH
            + INJURY_ADVISORY_TEXT
            + NEW_PARAGRAPH);
    }

    @Test
    void shouldProduceAdequateOrderDetailsForEndDateInANumberOfMonths() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(LocalDateTime.of(2021, 1, 14, 13, 42))
            .manageOrdersIsByConsent("No")
            .manageOrdersOrderJurisdiction(ENGLAND)
            .manageOrdersReasonForSecureAccommodation(INJURY)
            .manageOrdersIsChildRepresented("Yes")
            .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
            .manageOrdersSetMonthsEndDate(6)
            .build();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(manageOrdersEventData)
            .build();
        when(mockChildrenSmartSelector.getSelectedChildren(caseData)).thenReturn(singletonList(testChild(BOY)));

        C26SecureAccommodationOrderDocmosisParameters result =
            (C26SecureAccommodationOrderDocmosisParameters) underTest.generate(caseData);

        assertThat(result.getOrderDetails()).contains("secure accommodation for 6 months from the date of this order.");
    }

    @Test
    void shouldProduceAdequateOrderDetailsForEndDateInOneMonth() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(LocalDateTime.of(2021, 1, 14, 13, 42))
            .manageOrdersIsByConsent("No")
            .manageOrdersOrderJurisdiction(ENGLAND)
            .manageOrdersReasonForSecureAccommodation(INJURY)
            .manageOrdersIsChildRepresented("Yes")
            .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
            .manageOrdersSetMonthsEndDate(1)
            .build();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(manageOrdersEventData)
            .build();
        when(mockChildrenSmartSelector.getSelectedChildren(caseData)).thenReturn(singletonList(testChild(BOY)));

        C26SecureAccommodationOrderDocmosisParameters result =
            (C26SecureAccommodationOrderDocmosisParameters) underTest.generate(caseData);

        assertThat(result.getOrderDetails()).contains("secure accommodation for 1 month from the date of this order.");
    }

}
