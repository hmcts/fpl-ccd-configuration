package uk.gov.hmcts.reform.fpl.service.orders.prepopulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.*;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.*;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.*;

@ExtendWith(MockitoExtension.class)
class OrderSectionAndQuestionsPrePopulatorHolderTest {

    // Section blocks
    @Mock
    private WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator;
    @Mock
    private ApproverBlockPrePopulator approverBlockPrePopulator;
    @Mock
    private EPOTypeAndPreventRemovalBlockPrePopulator epoTypeAndPreventRemovalBlockPrePopulator;
    @Mock
    private LinkedToHearingBlockPrePopulator linkedToHearingBlockPrePopulator;
    @Mock
    private ApprovalDateBlockPrePopulator approvalDateBlockPrePopulator;
    @Mock
    private ApprovalDateTimeBlockPrePopulator approvalDateTimeBlockPrePopulator;
    @Mock
    private WhichOthersBlockPrePopulator whichOthersBlockPrePopulator;

    // Section blocks
    @Mock
    private HearingDetailsSectionPrePopulator hearingDetailsSectionPrePopulator;
    @Mock
    private IssuingDetailsSectionPrePopulator issuingDetailsSectionPrePopulator;
    @Mock
    private ChildrenDetailsSectionPrePopulator childrenDetailsSectionPrePopulator;
    @Mock
    private OrderDetailsSectionPrePopulator orderDetailsSectionPrePopulator;
    @Mock
    private DraftOrderPreviewSectionPrePopulator draftOrderPreviewSectionPrePopulator;
    @Mock
    private CloseCaseBlockPrePopulator closeCaseBlockPrePopulator;
    @Mock
    private OtherDetailsSectionPrePopulator otherDetailsSectionPrePopulator;

    @InjectMocks
    private OrderSectionAndQuestionsPrePopulatorHolder underTest;

    private List<QuestionBlockOrderPrePopulator> questionPrepopulators;
    private Map<OrderQuestionBlock, QuestionBlockOrderPrePopulator> questionBlockPrepopulatorMapping;

    private List<OrderSectionPrePopulator> sectionPrePopulators;
    private Map<OrderSection, OrderSectionPrePopulator> sectionPrepopulatorMapping;

    @BeforeEach
    void setUp() {
        questionPrepopulators = List.of(
            linkedToHearingBlockPrePopulator,
            approvalDateBlockPrePopulator,
            approvalDateTimeBlockPrePopulator,
            whichChildrenBlockPrePopulator,
            approverBlockPrePopulator,
            epoTypeAndPreventRemovalBlockPrePopulator,
            closeCaseBlockPrePopulator,
            whichOthersBlockPrePopulator

        );
        questionBlockPrepopulatorMapping = Map.of(
            LINKED_TO_HEARING, linkedToHearingBlockPrePopulator,
            APPROVAL_DATE, approvalDateBlockPrePopulator,
            APPROVAL_DATE_TIME, approvalDateTimeBlockPrePopulator,
            APPROVER, approverBlockPrePopulator,
            WHICH_CHILDREN, whichChildrenBlockPrePopulator,
            EPO_TYPE_AND_PREVENT_REMOVAL, epoTypeAndPreventRemovalBlockPrePopulator,
            CLOSE_CASE, closeCaseBlockPrePopulator,
            WHICH_OTHERS, whichOthersBlockPrePopulator
        );

        sectionPrepopulatorMapping = Map.of(
            HEARING_DETAILS, hearingDetailsSectionPrePopulator,
            ISSUING_DETAILS, issuingDetailsSectionPrePopulator,
            CHILDREN_DETAILS, childrenDetailsSectionPrePopulator,
            ORDER_DETAILS, orderDetailsSectionPrePopulator,
            OTHER_DETAILS, otherDetailsSectionPrePopulator,
            REVIEW, draftOrderPreviewSectionPrePopulator
        );
        sectionPrePopulators = List.of(
            hearingDetailsSectionPrePopulator,
            issuingDetailsSectionPrePopulator, childrenDetailsSectionPrePopulator, orderDetailsSectionPrePopulator,
            draftOrderPreviewSectionPrePopulator, otherDetailsSectionPrePopulator
        );
    }

    @Test
    void questionBlockToPopulator() {
        questionPrepopulators.forEach(populator -> when(populator.accept()).thenCallRealMethod());

        assertThat(underTest.questionBlockToPopulator()).isEqualTo(questionBlockPrepopulatorMapping);
    }

    @Test
    void questionBlockToPopulatorCached() {
        questionPrepopulators.forEach(populator -> when(populator.accept()).thenCallRealMethod());

        underTest.questionBlockToPopulator();

        assertThat(underTest.questionBlockToPopulator()).isEqualTo(questionBlockPrepopulatorMapping);

        questionPrepopulators.forEach(populator -> verify(populator).accept());
    }

    @Test
    void sectionBlockToPopulator() {
        sectionPrePopulators.forEach(populator -> when(populator.accept()).thenCallRealMethod());

        assertThat(underTest.sectionBlockToPopulator()).isEqualTo(sectionPrepopulatorMapping);
    }

    @Test
    void sectionBlockToPopulatorCached() {
        sectionPrePopulators.forEach(populator -> when(populator.accept()).thenCallRealMethod());

        underTest.sectionBlockToPopulator();

        assertThat(underTest.sectionBlockToPopulator()).isEqualTo(sectionPrepopulatorMapping);

        sectionPrePopulators.forEach(populator -> verify(populator).accept());
    }
}
