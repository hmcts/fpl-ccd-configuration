package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C63aDeclarationOfParentageDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C63A_DECLARATION_OF_PARENTAGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C63aDeclarationOfParentageParameterGeneratorTest {

    public static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2021, 4, 20, 10, 0, 0);
    private static final String LOCAL_AUTHORITY_NAME = "Swansea Local Authority";
    private static Child CHILD = Child.builder().party(
        ChildParty.builder().firstName("Harley").lastName("Bloggs").build())
        .build();
    @Mock
    private ChildrenSmartSelector childrenSmartSelector;
    @InjectMocks
    private C63aDeclarationOfParentageDocumentParameterGenerator underTest;

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(C63A_DECLARATION_OF_PARENTAGE);
    }

    @Test
    void shouldReturnCorrectTemplate() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void generateDocumentWithoutHearingParty() {
        CaseData caseData = getCaseData("Peter Smith", "is",
            LOCAL_AUTHORITY_NAME, null, null);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails("It is declared that Peter Smith is the parent of Harley Bloggs.")
            .orderMessage("Upon the application of " + LOCAL_AUTHORITY_NAME + ".")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentWithTwoHearingParties() {
        CaseData caseData = getCaseData("Peter Smith", "is",
            LOCAL_AUTHORITY_NAME, "Mary", "Joseph");

        List<Element<Child>> selectedChildren = wrapElements(CHILD);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails("It is declared that Peter Smith is the parent of Harley Bloggs.")
            .orderMessage("Upon the application of " + LOCAL_AUTHORITY_NAME + "\n"
                + "and upon hearing Mary\n"
                + "and upon hearing Joseph.")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentWithOneHearingParty() {
        CaseData caseData = getCaseData("Peter Smith", "is",
            LOCAL_AUTHORITY_NAME, "Mary", null);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails("It is declared that Peter Smith is the parent of Harley Bloggs.")
            .orderMessage("Upon the application of " + LOCAL_AUTHORITY_NAME + "\n"
                + "and upon hearing Mary.")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private C63aDeclarationOfParentageDocmosisParameters
        .C63aDeclarationOfParentageDocmosisParametersBuilder<?, ?>
        expectedCommonParameters() {

        return C63aDeclarationOfParentageDocmosisParameters.builder()
            .orderTitle(C63A_DECLARATION_OF_PARENTAGE.getTitle())
            .childrenAct(C63A_DECLARATION_OF_PARENTAGE.getChildrenAct());
    }

    private CaseData getCaseData(String personWhoseParenthoodIs, String action, String applicant, String hearingParty1,
                                 String hearingParty2) {
        return CaseData.builder().manageOrdersEventData(
            ManageOrdersEventData.builder().manageOrdersApprovalDateTime(APPROVAL_DATE_TIME)
                .manageOrdersPersonWhoseParenthoodIs(
                    DynamicList.builder().value(DynamicListElement.builder().code(personWhoseParenthoodIs).build())
                        .build()).manageOrdersParentageAction(
                    DynamicList.builder().value(DynamicListElement.builder().label(action).build()).build())
                .manageOrdersParentageApplicant(
                    DynamicList.builder().value(DynamicListElement.builder().label(applicant).build()).build())
                .manageOrdersHearingParty1(hearingParty1 == null ? null :
                    DynamicList.builder().value(DynamicListElement.builder().code(hearingParty1).build()).build())
                .manageOrdersHearingParty2(hearingParty2 == null ? null :
                    DynamicList.builder().value(DynamicListElement.builder().code(hearingParty2).build()).build())
                .build()).build();
    }
}
