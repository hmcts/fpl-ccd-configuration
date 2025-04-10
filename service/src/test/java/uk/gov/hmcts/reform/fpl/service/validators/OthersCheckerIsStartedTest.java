package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.AddressNotKnowReason;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class OthersCheckerIsStartedTest {

    @InjectMocks
    private OthersChecker othersChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyOthers")
    void shouldReturnFalseWhenEmptyOthers(List<Element<Other>> others) {
        final CaseData caseData = CaseData.builder()
                .othersV2(others)
                .build();

        final boolean isStarted = othersChecker.isStarted(caseData);

        assertThat(isStarted).isFalse();
    }

    @Test
    void shouldReturnTrueWhenMoreThanOneOthersProvided() {
        final List<Element<Other>> others = wrapElements(
            Other.builder().firstName("test").build(),
            Other.builder().build());
        final CaseData caseData = CaseData.builder()
                .othersV2(others)
                .build();

        final boolean isStarted = othersChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyOthers")
    void shouldReturnTrueWhenOthersDetailsProvided(Other other) {
        final CaseData caseData = CaseData.builder()
                .othersV2(wrapElements(other))
                .build();

        final boolean isStarted = othersChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    private static Stream<Arguments> nonEmptyOthers() {
        return Stream.of(
                Other.builder().firstName("Test").build(),
                Other.builder().dateOfBirth("01-01-2010").build(),
                Other.builder().childInformation("Test").build(),
                Other.builder().hideTelephone("No").build(),
                Other.builder().hideAddress("No").build(),
                Other.builder().litigationIssues("No").build(),
                Other.builder().telephone("777").build(),
                Other.builder().addressNotKnowReason(AddressNotKnowReason.DECEASED.getType()).build(),
                Other.builder().address(Address.builder().addressLine1("Test").build()).build(),
                Other.builder().address(Address.builder().addressLine2("Test").build()).build(),
                Other.builder().address(Address.builder().addressLine3("Test").build()).build(),
                Other.builder().address(Address.builder().postTown("Test").build()).build(),
                Other.builder().address(Address.builder().county("Test").build()).build(),
                Other.builder().address(Address.builder().country("Test").build()).build(),
                Other.builder().address(Address.builder().postcode("Test").build()).build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyOthers() {
        return Stream.of(
                List.of(),
                wrapElements(Other.builder().build()),
                wrapElements(Other.builder()
                    .name("")
                    .dateOfBirth("")
                    .gender("")
                    .birthPlace("")
                    .childInformation("")
                    .detailsHidden("")
                    .litigationIssues("")
                    .telephone("")
                    .address(Address.builder()
                        .addressLine1("")
                        .addressLine2("")
                        .addressLine3("")
                        .county("")
                        .country("")
                        .postTown("")
                        .postcode("")
                        .build())
                    .build()),
                wrapElements(Other.builder()
                    .firstName("")
                    .lastName("")
                    .dateOfBirth("")
                    .childInformation("")
                    .hideAddress("")
                    .hideTelephone("")
                    .litigationIssues("")
                    .telephone("")
                    .address(Address.builder()
                        .addressLine1("")
                        .addressLine2("")
                        .addressLine3("")
                        .county("")
                        .country("")
                        .postTown("")
                        .postcode("")
                        .build())
                    .build()))
                .map(Arguments::of);
    }
}
