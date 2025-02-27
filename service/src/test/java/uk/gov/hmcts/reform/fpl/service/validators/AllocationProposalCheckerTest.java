package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AllocationProposalChecker.class, LocalValidatorFactoryBean.class})
class AllocationProposalCheckerTest {

    @Autowired
    private AllocationProposalChecker allocationProposalChecker;

    @Nested
    class Validate {

        @Test
        void shouldReturnErrorWhenNoAllocationProposalHasBeenAdded() {
            final CaseData caseData = CaseData.builder().build();

            final List<String> errors = allocationProposalChecker.validate(caseData);

            assertThat(errors).contains("Add the allocation proposal");
        }

        @Test
        void shouldReturnErrorWhenNoAllocationProposalDetails() {
            final Allocation allocation = Allocation.builder().build();
            final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

            final List<String> errors = allocationProposalChecker.validate(caseData);

            assertThat(errors).contains("Enter an allocation proposal reason");
        }

        @Test
        void shouldReturnErrorWhenAllocationProposalIsBlank() {
            final Allocation allocation = Allocation.builder()
                .proposalV2("")
                .build();
            final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

            final List<String> errors = allocationProposalChecker.validate(caseData);

            assertThat(errors).contains("Enter an allocation proposal");
        }

        @Test
        void shouldReturnEmptyErrorsWhenAllocationProposalandProposalDetailsIsPresent() {
            final Allocation allocation = Allocation.builder()
                .proposalV2("Circuit Judge")
                .proposalReason("test reason")
                .build();
            final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

            final List<String> errors = allocationProposalChecker.validate(caseData);

            assertThat(errors).isEmpty();
        }
    }

    @Nested
    class IsCompleted {

        @Test
        void shouldNotBeCompletedWhenNoAllocationProposalHasBeenAdded() {
            final CaseData caseData = CaseData.builder().build();

            final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @Test
        void shouldNotBeCompletedWhenNoAllocationProposalDetails() {
            final Allocation allocation = Allocation.builder().build();
            final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

            final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @Test
        void shouldNotBeCompletedWhenAllocationProposalIsBlank() {
            final Allocation allocation = Allocation.builder()
                .proposalV2("")
                .proposalReason("Proposal Reason")
                .build();
            final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

            final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @Test
        void shouldNotBeCompletedWhenAllocationProposalIsPresent() {
            final Allocation allocation = Allocation.builder()
                .proposalV2("Circuit Judge")
                .build();
            final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

            final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @Test
        void shouldBeCompletedWhenAllocationProposalAndReasonIsPresent() {
            final Allocation allocation = Allocation.builder()
                .proposalV2("Circuit Judge")
                .proposalReason("Proposal Reason")
                .build();
            final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

            final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    @Test
    void testCompletedState() {
        assertThat(allocationProposalChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

}
