package uk.gov.hmcts.reform.fpl.service.orders.prepopulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.AmendOrderToDownloadPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.AppointedGuardianBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ApprovalDateBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ApprovalDateTimeBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ApproverBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.CloseCaseBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.EPOTypeAndPreventRemovalBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.LinkApplicationBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.LinkedToHearingBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.QuestionBlockOrderPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.SingleChildSelectionBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.WhichChildrenBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.WhichOthersBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.ChildrenDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.DraftOrderPreviewSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.HearingDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.IssuingDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.OrderDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.OrderSectionPrePopulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPOINTED_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CLOSE_CASE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_TYPE_AND_PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.LINKED_TO_HEARING;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.LINK_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ORDER_TO_AMEND;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SELECT_SINGLE_CHILD;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_OTHERS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.CHILDREN_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ISSUING_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ORDER_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.REVIEW;

@ExtendWith(MockitoExtension.class)
class OrderSectionAndQuestionsPrePopulatorHolderTest {

    @Mock
    private WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator;
    @Mock
    private SingleChildSelectionBlockPrePopulator singleChildSelectionBlockPrePopulator;
    @Mock
    private ApproverBlockPrePopulator approverBlockPrePopulator;
    @Mock
    private EPOTypeAndPreventRemovalBlockPrePopulator epoTypeAndPreventRemovalBlockPrePopulator;
    @Mock
    private LinkedToHearingBlockPrePopulator linkedToHearingBlockPrePopulator;
    @Mock
    private LinkApplicationBlockPrePopulator linkApplicationBlockPrePopulator;
    @Mock
    private ApprovalDateBlockPrePopulator approvalDateBlockPrePopulator;
    @Mock
    private ApprovalDateTimeBlockPrePopulator approvalDateTimeBlockPrePopulator;
    @Mock
    private AppointedGuardianBlockPrePopulator appointedGuardianBlockPrePopulator;
    @Mock
    private WhichOthersBlockPrePopulator whichOthersBlockPrePopulator;
    @Mock
    private AmendOrderToDownloadPrePopulator amendOrderToDownloadPrePopulator;

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
            linkApplicationBlockPrePopulator,
            approvalDateBlockPrePopulator,
            approvalDateTimeBlockPrePopulator,
            whichChildrenBlockPrePopulator,
            singleChildSelectionBlockPrePopulator,
            approverBlockPrePopulator,
            epoTypeAndPreventRemovalBlockPrePopulator,
            closeCaseBlockPrePopulator,
            appointedGuardianBlockPrePopulator,
            whichOthersBlockPrePopulator,
            amendOrderToDownloadPrePopulator
        );
        questionBlockPrepopulatorMapping = Map.ofEntries(
            Map.entry(LINKED_TO_HEARING, linkedToHearingBlockPrePopulator),
            Map.entry(LINK_APPLICATION, linkApplicationBlockPrePopulator),
            Map.entry(APPROVAL_DATE, approvalDateBlockPrePopulator),
            Map.entry(APPROVAL_DATE_TIME, approvalDateTimeBlockPrePopulator),
            Map.entry(APPROVER, approverBlockPrePopulator),
            Map.entry(WHICH_CHILDREN, whichChildrenBlockPrePopulator),
            Map.entry(APPOINTED_GUARDIAN, appointedGuardianBlockPrePopulator),
            Map.entry(SELECT_SINGLE_CHILD, singleChildSelectionBlockPrePopulator),
            Map.entry(EPO_TYPE_AND_PREVENT_REMOVAL, epoTypeAndPreventRemovalBlockPrePopulator),
            Map.entry(CLOSE_CASE, closeCaseBlockPrePopulator),
            Map.entry(WHICH_OTHERS, whichOthersBlockPrePopulator),
            Map.entry(ORDER_TO_AMEND, amendOrderToDownloadPrePopulator)
        );

        sectionPrepopulatorMapping = Map.of(
            HEARING_DETAILS, hearingDetailsSectionPrePopulator,
            ISSUING_DETAILS, issuingDetailsSectionPrePopulator,
            CHILDREN_DETAILS, childrenDetailsSectionPrePopulator,
            ORDER_DETAILS, orderDetailsSectionPrePopulator,
            REVIEW, draftOrderPreviewSectionPrePopulator
        );
        sectionPrePopulators = List.of(
            hearingDetailsSectionPrePopulator,
            issuingDetailsSectionPrePopulator, childrenDetailsSectionPrePopulator, orderDetailsSectionPrePopulator,
            draftOrderPreviewSectionPrePopulator
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
