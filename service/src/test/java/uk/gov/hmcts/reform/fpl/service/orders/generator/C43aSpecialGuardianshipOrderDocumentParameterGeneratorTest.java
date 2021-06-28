package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43aSpecialGuardianshipOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C43aSpecialGuardianshipOrderDocumentParameterGeneratorTest {

    private static final Child CHILD = mock(Child.class);
    public static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2021, 4, 20, 10, 0, 0);
    public static final String EXPECTED_APPROVAL_DATE_TIME = "20 April 2021, 10:00am";
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final String ORDER_HEADER = "Warning \n";
    private static final String ORDER_MESSAGE = "Where a Special Guardianship Order is in force no person may "
        + "cause the child to be known by a new surname or remove the "
        + "child from the United Kingdom without either the written consent"
        + " of every person who has parental responsibility for the child or "
        + "the leave of the court. "
        + "However, this does not prevent the removal "
        + "of a child for a period of less than 3 months, "
        + "by its special guardian(s) (Section 14C (3) and (4) Children Act 1989)."
        + "\n \n"
        + "It may be a criminal offence under the Child Abduction Act 1984 "
        + "to remove the child from the United Kingdom without leave of the court.\n"
        + "";
    private static final String NOTICE_HEADER = "Notice \n";
    private static final String NOTICE_MESSAGE = "Any person with parental responsibility for a child may "
        + "obtain advice on what can be done to prevent the issue of a passport to the child. They should write "
        + "to The United Kingdom Passport Agency, Globe House, 89 Eccleston Square, LONDON, SW1V 1PN.";

    @Mock
    private ChildrenService childrenService;

    @Mock
    private AppointedGuardianFormatter appointedGuardianFormatter;

    @InjectMocks
    private C43aSpecialGuardianshipOrderDocumentParameterGenerator underTest;

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(C43A_SPECIAL_GUARDIANSHIP_ORDER);
    }

    @Test
    void shouldReturnCorrectTemplate() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void generateDocumentForSingleChildWithOrderByConsent() {
        CaseData caseData = getCaseData(true, 1);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData)).thenReturn("Remmy Respondent is");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildWithSinglePersonResponsible())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildWithOrderByConsentAndSingleSpecialGuardianAppointee() {
        CaseData caseData = getCaseData(true, 1);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData)).thenReturn("Remmy Respondent is");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildWithSinglePersonResponsible())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildWithoutOrderByConsentAndSingleSpecialGuardianAppointee() {
        CaseData caseData = getCaseData(false, 1);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData)).thenReturn("Remmy Respondent is");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(getOrderAppointmentMessageForChildWithSinglePersonResponsible())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildrenWithOrderByConsentAndSingleSpecialGuardianAppointee() {
        CaseData caseData = getCaseData(true, 1);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD, CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData)).thenReturn("Remmy Respondent is");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildrenWithSinglePersonResponsible())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildrenWithoutOrderByConsentAndSingleSpecialGuardianAppointee() {
        CaseData caseData = getCaseData(false, 1);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD, CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData)).thenReturn("Remmy Respondent is");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(getOrderAppointmentMessageForChildrenWithSinglePersonResponsible())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildWithOrderByConsentAndMultipleSpecialGuardianAppointee() {
        CaseData caseData = getCaseData(true, 2);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData))
            .thenReturn("Remmy Respondent, Randle Responde are");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildWithMultiplePeopleResponsible())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildrenWithOrderByConsentAndMultipleSpecialGuardianAppointee() {
        CaseData caseData = getCaseData(true, 2);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData))
            .thenReturn("Remmy Respondent, Randle Responde are");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildrenWithMultiplePeopleResponsible())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildrenWithOrderByConsentAndLargeNumberOfSpecialGuardianAppointee() {
        CaseData caseData = getCaseData(true, 15);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(appointedGuardianFormatter.getGuardiansNamesForDocument(caseData))
            .thenReturn("P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17 are");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getMaxSpecialGuardiansAllowed())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private String getOrderAppointmentMessageForChildWithSinglePersonResponsible() {
        return "The Court orders that Remmy Respondent is appointed as special guardian for the child.";
    }

    private String getOrderAppointmentMessageForChildrenWithSinglePersonResponsible() {
        return "The Court orders that Remmy Respondent is appointed as special guardian for the children.";
    }

    private String getOrderAppointmentMessageForChildWithMultiplePeopleResponsible() {
        return "The Court orders that Remmy Respondent, "
            + "Randle Responde are appointed as special guardian for the child.";
    }

    private String getOrderAppointmentMessageForChildrenWithMultiplePeopleResponsible() {
        return "The Court orders that Remmy Respondent, "
            + "Randle Responde are appointed as special guardian for the children.";
    }

    private String getMaxSpecialGuardiansAllowed() {
        return "The Court orders that P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17"
            + " are appointed as special guardian for the children.";
    }

    private C43aSpecialGuardianshipOrderDocmosisParameters.C43aSpecialGuardianshipOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParameters(Boolean isOrderByConsent) {
        String orderByConsentContent = getOrderByConsentContent(isOrderByConsent);

        return C43aSpecialGuardianshipOrderDocmosisParameters.builder()
            .orderTitle(Order.C43A_SPECIAL_GUARDIANSHIP_ORDER.getTitle())
            .dateOfIssue(EXPECTED_APPROVAL_DATE_TIME)
            .furtherDirections(FURTHER_DIRECTIONS)
            .orderByConsent(orderByConsentContent)
            .orderHeader(ORDER_HEADER)
            .orderMessage(ORDER_MESSAGE)
            .noticeHeader(NOTICE_HEADER)
            .noticeMessage(NOTICE_MESSAGE);
    }

    private String getOrderByConsentContent(Boolean isOrderByConsent) {
        String orderByConsentContent = "By consent";
        if (!isOrderByConsent) {
            orderByConsentContent = null;
        }
        return orderByConsentContent;
    }

    private CaseData getCaseData(boolean isOrderByConsent, int numOfGuardians) {

        return CaseData.builder()
            .appointedGuardianSelector(Selector.builder().selected(List.of(numOfGuardians)).build())
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(APPROVAL_DATE_TIME)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersIsByConsent(isOrderByConsent ? "Yes" : "No")
                .build())
            .build();
    }
}
