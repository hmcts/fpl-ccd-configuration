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
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C45aParentalResponsibilityOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild.FATHER;
import static uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild.SECOND_FEMALE_PARENT;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C45A_PARENTAL_RESPONSIBILITY_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C45aParentalResponsibilityOrderDocumentParameterGeneratorTest {
    private static final Child CHILD = mock(Child.class);
    public static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2021, 4, 20, 10, 0, 0);
    public static final String EXPECTED_APPROVAL_DATE_TIME = "20 April 2021, 10:00am";
    public static final String CONSENT = "By consent";
    private static final String FURTHER_DIRECTIONS = "further directions";

    private static final String NOTICE_HEADER = "Notice \n";
    private static final String NOTICE_MESSAGE = "A parental responsibility order can only end\n \n"
        + "a) When the child reaches 18 years\n"
        + "b) By order of the court made\n"
        + "      * on the application of any person who has parental responsibility\n"
        + "      * with leave of the court on the application of the child\n";

    private static final String CHILDREN_ACT_FATHER = "Section 4(1) Children Act 1989";
    private static final String CHILDREN_ACT_SECOND_FEMALE_PARENT = "Section 4ZA Children Act 1989";

    @Mock
    private ChildrenService childrenService;

    @Mock
    private OrderMessageGenerator orderMessageGenerator;

    @InjectMocks
    private C45aParentalResponsibilityOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(C45A_PARENTAL_RESPONSIBILITY_ORDER);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void shouldReturnExpectedParametersFor_NamedPartyByConsentForChild() {
        CaseData caseData = getCaseData();

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn("Yes");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getOrderDetailsForChild())
            .orderByConsent("Yes")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedParametersFor_NamedPartyByConsentForChildren() {
        CaseData caseData = getCaseData();

        List<Element<Child>> selectedChildren = wrapElements(CHILD,CHILD,CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn("Yes");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getOrderDetailsForChildren())
            .orderByConsent("Yes")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedParametersFor_NamedPartyWithoutConsentForChild() {
        CaseData caseData = getCaseData();

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(null);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getOrderDetailsForChild())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedParametersFor_NamedPartyWithoutConsentForChildren() {
        CaseData caseData = getCaseData();

        List<Element<Child>> selectedChildren = wrapElements(CHILD,CHILD,CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(null);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(getOrderDetailsForChildren())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnChildActFor_SecondFemaleParent() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(APPROVAL_DATE_TIME)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersRelationshipWithChild(SECOND_FEMALE_PARENT)
                .manageOrdersParentResponsible("Remmie Responsible")
                .build())
            .build();

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn("Yes");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .childrenAct(CHILDREN_ACT_SECOND_FEMALE_PARENT)
            .orderDetails(getOrderDetailsForChild())
            .orderByConsent("Yes")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnChildActFor_Father() {
        CaseData caseData = getCaseData();

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn("Yes");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .childrenAct(CHILDREN_ACT_FATHER)
            .orderDetails(getOrderDetailsForChild())
            .orderByConsent("Yes")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }


    private String getOrderDetailsForChild() {
        return "The Court orders that Remmie Responsible shall have parental responsibility for the child.";
    }

    private String getOrderDetailsForChildren() {
        return "The Court orders that Remmie Responsible shall have parental responsibility for the children.";
    }

    private C45aParentalResponsibilityOrderDocmosisParameters.C45aParentalResponsibilityOrderDocmosisParametersBuilder
        <?,?> expectedCommonParameters() {
        return C45aParentalResponsibilityOrderDocmosisParameters.builder()
            .childrenAct(CHILDREN_ACT_FATHER)
            .furtherDirections(FURTHER_DIRECTIONS)
            .dateOfIssue(EXPECTED_APPROVAL_DATE_TIME)
            .noticeHeader(NOTICE_HEADER)
            .noticeMessage(NOTICE_MESSAGE)
            .orderTitle(C45A_PARENTAL_RESPONSIBILITY_ORDER.getTitle());
    }

    private CaseData getCaseData() {
        return CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(APPROVAL_DATE_TIME)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersRelationshipWithChild(FATHER)
                .manageOrdersParentResponsible("Remmie Responsible")
                .build())
            .build();
    }


}
