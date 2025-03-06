package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AllocationProposalCheckerIsStartedTest {

    @InjectMocks
    private AllocationProposalChecker allocationProposalChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyAllocationProposal")
    void shouldReturnFalseWhenAllocationProposalNotProvided(final Allocation allocation) {
        final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

        assertThat(allocationProposalChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyAllocationProposal")
    void shouldReturnTrueWhenAllocationProposalProvided(final Allocation allocation) {
        final CaseData caseData = CaseData.builder()
                .allocationProposal(allocation)
                .build();

        assertThat(allocationProposalChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> emptyAllocationProposal() {
        return Stream.of(
                Allocation.builder()
                        .build(),
                Allocation.builder()
                        .proposalV2(null)
                        .proposalReason(null)
                        .build(),
                Allocation.builder()
                        .proposalV2("")
                        .proposalReason("")
                        .build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> nonEmptyAllocationProposal() {
        return Stream.of(
                Allocation.builder()
                        .proposalV2("Circuit Judge")
                        .build(),
                Allocation.builder()
                        .proposalReason("Test reason")
                        .build(),
                Allocation.builder()
                        .proposalV2("Circuit Judge")
                        .proposalReason("Test reason")
                        .build())
                .map(Arguments::of);
    }
}
