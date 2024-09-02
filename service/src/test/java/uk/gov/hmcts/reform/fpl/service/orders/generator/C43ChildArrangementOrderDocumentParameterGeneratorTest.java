package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.ChildArrangementsOrderType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43ChildArrangementOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.SPECIFIC_ISSUE_ORDER;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderDocumentParameterGenerator.CONDITIONS_MESSAGE;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderDocumentParameterGenerator.NOTICE_MESSAGE;
import static uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderDocumentParameterGenerator.WARNING_MESSAGE;

@ExtendWith({MockitoExtension.class})
public class C43ChildArrangementOrderDocumentParameterGeneratorTest {
    private static final String PASSPORT_OFFICE_EMAIL = "passport-office@example.com";
    private static final String PASSPORT_OFFICE_ADDRESS = "Passport Office, some address, somewhere";
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final String CONSENT = "By consent";
    private static final String ORDER_HEADER = "Warning \n";
    private static final String PROHIBITED_STEPS_DETAIL = "Prohibited steps order detail";
    private static final String SPECIFIC_ISSUE_DETAIL = "Specific issue order detail";
    private static final String CONTACT_WITH_DETAIL = "Contact with order detail";
    private static final String LIVE_WITH_DETAIL = "Live with order detail";
    private static final String RECITALS_AND_PREAMBLES = "Recitals and Preambles";
    private static final String DIRECTIONS = "Directions";
    private static final String FURTHER_DIRECTIONS = "Further directions";
    private static final String ORDER_TITLE = "Title";
    private static final String CHILD_LIVE_TEXT = "Child to live with";
    private static final String CHILD_CONTACT_TEXT = "Child to have contact with";
    private static final ChildArrangementsOrderType CHILD_LIVE = ChildArrangementsOrderType.CHILD_LIVE;
    private static final ChildArrangementsOrderType CHILD_CONTACT = ChildArrangementsOrderType.CHILD_CONTACT;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @Mock
    private OrderMessageGenerator orderMessageGenerator;

    @Mock
    private C43ChildArrangementOrderTitleGenerator c43ChildArrangementOrderTitleGenerator;

    @InjectMocks
    private C43ChildArrangementOrderDocumentParameterGenerator underTest;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(underTest, "passportOfficeEmail", PASSPORT_OFFICE_EMAIL);
        ReflectionTestUtils.setField(underTest, "passportOfficeAddress", PASSPORT_OFFICE_ADDRESS);
    }

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER);
    }

    @Test
    void shouldShowWarningWhenChildArrangementOrder() {
        List<C43OrderType> c43OrderTypes = List.of(C43OrderType.CHILD_ARRANGEMENT_ORDER,
            SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER);

        CaseData caseData = buildCaseData(c43OrderTypes, List.of(CHILD_LIVE));

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(any())).thenReturn(ORDER_TITLE);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedCommonParametersChildArrangementOrder()
            .orderHeader(ORDER_HEADER)
            .orderMessage(WARNING_MESSAGE)
            .build());
    }

    @Test
    void shouldNotShowWarningWhenNoChildArrangementOrder() {
        List<C43OrderType> c43OrderTypes = List.of(
            SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER);

        CaseData caseData = buildCaseData(c43OrderTypes, List.of());

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(any())).thenReturn(ORDER_TITLE);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedCommonParameters().build());
    }

    @Test
    void shouldContainCorrectTextWhenChildArrangementOrderTypeIsLiveWith() {
        List<C43OrderType> c43OrderTypes = List.of(C43OrderType.CHILD_ARRANGEMENT_ORDER,
            SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER);

        CaseData caseData = buildCaseData(c43OrderTypes, List.of(CHILD_LIVE));

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(any())).thenReturn(ORDER_TITLE);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters.toString()).contains(CHILD_LIVE_TEXT);
    }

    @Test
    void shouldContainCorrectTextWhenChildArrangementOrderTypeIsContactWith() {
        List<C43OrderType> c43OrderTypes = List.of(C43OrderType.CHILD_ARRANGEMENT_ORDER,
            SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER);

        CaseData caseData = buildCaseData(c43OrderTypes, List.of(CHILD_CONTACT));

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(any())).thenReturn(ORDER_TITLE);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters.toString()).contains(CHILD_CONTACT_TEXT);
    }

    @Test
    void shouldContainCorrectTextWhenBothArrangementOrderTypeSelected() {
        List<C43OrderType> c43OrderTypes = List.of(C43OrderType.CHILD_ARRANGEMENT_ORDER,
            SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER);

        CaseData caseData = buildCaseData(c43OrderTypes, List.of(CHILD_LIVE, CHILD_CONTACT));

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(any())).thenReturn(ORDER_TITLE);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters.toString()).contains(CHILD_LIVE_TEXT);
        assertThat(generatedParameters.toString()).contains(CHILD_CONTACT_TEXT);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    private CaseData buildCaseData(List<C43OrderType> c43OrderTypes,
                                   List<ChildArrangementsOrderType> childArrOrderType) {
        return CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER)
                .manageOrdersIsByConsent("No")
                .manageOrdersMultiSelectListForC43(c43OrderTypes)
                .manageOrdersProhibitedStepsOrderDetails(
                    c43OrderTypes.contains(PROHIBITED_STEPS_ORDER) ? PROHIBITED_STEPS_DETAIL : null)
                .manageOrdersSpecificIssueOrderDetails(
                    c43OrderTypes.contains(SPECIFIC_ISSUE_ORDER) ? SPECIFIC_ISSUE_DETAIL : null)
                .manageOrdersChildArrangementsLiveWithDetails(
                    childArrOrderType.contains(CHILD_LIVE) ? LIVE_WITH_DETAIL : null)
                .manageOrdersChildArrangementsContactWithDetails(
                    childArrOrderType.contains(CHILD_CONTACT) ? CONTACT_WITH_DETAIL : null)
                .manageOrdersRecitalsAndPreambles(RECITALS_AND_PREAMBLES)
                .manageOrdersDirections(DIRECTIONS)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersChildArrangementsOrderTypes(childArrOrderType)
                .build())
            .build();
    }

    private C43ChildArrangementOrderDocmosisParameters.C43ChildArrangementOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParameters() {
        String orderDetails = String.format("The Court orders,\n\nSpecific Issue\n\n%s\n\nProhibited Steps\n\n%s",
            SPECIFIC_ISSUE_DETAIL, PROHIBITED_STEPS_DETAIL);
        String directions = String.format("%s\n\n%s\n\n%s", DIRECTIONS, FURTHER_DIRECTIONS,
            CONDITIONS_MESSAGE);
        String noticeMessage = String.format(NOTICE_MESSAGE, PASSPORT_OFFICE_ADDRESS, PASSPORT_OFFICE_EMAIL);

        return C43ChildArrangementOrderDocmosisParameters.builder()
            .orderTitle(ORDER_TITLE)
            .recitalsOrPreamble(RECITALS_AND_PREAMBLES)
            .orderByConsent(CONSENT)
            .prohibitedStepsOrderDetails(PROHIBITED_STEPS_DETAIL)
            .specificIssueOrderDetails(SPECIFIC_ISSUE_DETAIL)
            .orderDetails(orderDetails)
            .furtherDirections(directions)
            .localAuthorityName(LA_NAME)
            .noticeHeader("Notice")
            .noticeMessage(noticeMessage);
    }

    private C43ChildArrangementOrderDocmosisParameters.C43ChildArrangementOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParametersChildArrangementOrder() {
        String orderDetails = String.format("The Court orders,\n\nThe Child Arrangement Order is for the:"
            + "\n\nChild to live with\n\n%s\n\nSpecific Issue\n\n%s\n\nProhibited Steps\n\n%s",
            LIVE_WITH_DETAIL, SPECIFIC_ISSUE_DETAIL, PROHIBITED_STEPS_DETAIL);
        String directions = String.format("%s\n\n%s\n\n%s", DIRECTIONS, FURTHER_DIRECTIONS,
            CONDITIONS_MESSAGE);
        String noticeMessage = String.format(NOTICE_MESSAGE, PASSPORT_OFFICE_ADDRESS, PASSPORT_OFFICE_EMAIL);

        return C43ChildArrangementOrderDocmosisParameters.builder()
            .orderTitle(ORDER_TITLE)
            .recitalsOrPreamble(RECITALS_AND_PREAMBLES)
            .childArrangementsLiveWithDetails(LIVE_WITH_DETAIL)
            .specificIssueOrderDetails(SPECIFIC_ISSUE_DETAIL)
            .prohibitedStepsOrderDetails(PROHIBITED_STEPS_DETAIL)
            .orderByConsent(CONSENT)
            .orderDetails(orderDetails)
            .furtherDirections(directions)
            .localAuthorityName(LA_NAME)
            .noticeHeader("Notice")
            .noticeMessage(noticeMessage);
    }
}
