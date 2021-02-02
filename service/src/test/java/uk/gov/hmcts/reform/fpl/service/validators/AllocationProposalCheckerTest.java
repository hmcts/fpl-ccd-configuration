package uk.gov.hmcts.reform.fpl.service.validators;

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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AllocationProposalChecker.class, LocalValidatorFactoryBean.class})
class AllocationProposalCheckerTest {

    @Autowired
    private AllocationProposalChecker allocationProposalChecker;

    @Test
    void shouldReturnErrorWhenNoAllocationProposalHasBeenAdded() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = allocationProposalChecker.validate(caseData);
        final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

        assertThat(errors).contains("Add the allocation proposal");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenNoAllocationProposalDetails() {
        final Allocation allocation = Allocation.builder().build();
        final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

        final List<String> errors = allocationProposalChecker.validate(caseData);
        final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

        assertThat(errors).contains("Enter an allocation proposal");
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorWhenAllocationProposalIsBlank() {
        final Allocation allocation = Allocation.builder()
                .proposal("")
                .build();
        final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

        final List<String> errors = allocationProposalChecker.validate(caseData);
        final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

        assertThat(errors).contains("Enter an allocation proposal");
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnEmptyErrorsWhenAllocationProposalIsPresent() {
        final Allocation allocation = Allocation.builder()
                .proposal("Circuit Judge")
                .build();
        final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

        final List<String> errors = allocationProposalChecker.validate(caseData);
        final boolean isCompleted = allocationProposalChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }
}
