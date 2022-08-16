package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class OthersTest {

    @Test
    void shouldReturnDefaultOthersWhenListOfOthersIsEmpty() {
        final List<Element<Other>> othersList = emptyList();
        final Others actualOthers = Others.from(othersList);
        assertThat(actualOthers).isNull();
    }

    @Test
    void shouldReturnDefaultOthersWhenListOfOthersIsNull() {
        assertThat(Others.from(null)).isNull();
    }

    @Test
    void shouldReturnOthersWithAdditionalOthersWhenFirstOtherIsEmpty() {
        Other firstOther = Other.builder().build();
        final List<Element<Other>> othersList = wrapElements(firstOther, testOther());
        final Others actualOthers = Others.from(othersList);
        final Others expectedOthers = Others.builder()
            .firstOther(othersList.get(1).getValue())
            .additionalOthers(List.of())
            .build();

        assertThat(actualOthers).isEqualTo(expectedOthers);
    }

    @Test
    void shouldIgnoreEmptyOthersWhenOtherDetailsAreNotPresent() {
        Other firstOther = testOther();
        final List<Element<Other>> othersList = wrapElements(firstOther, Other.builder().build());
        final Others actualOthers = Others.from(othersList);
        final Others expectedOthers = Others.builder()
            .firstOther(firstOther)
            .additionalOthers(List.of())
            .build();

        assertThat(actualOthers).isEqualTo(expectedOthers);
    }

    @Test
    void shouldReturnEmptyWhenFirstOtherAndAdditionalOtherAreEmpty() {
        final List<Element<Other>> othersList = wrapElements(null, Other.builder().build());
        final Others actualOthers = Others.from(othersList);

        assertThat(actualOthers).isNull();
    }

    @Test
    void shouldReturnOthersWhenSingleOtherPresent() {
        final Other other = testOther();
        final List<Element<Other>> othersList = wrapElements(other);
        final Others actualOthers = Others.from(othersList);
        final Others expectedOthers = Others.builder()
            .firstOther(other)
            .additionalOthers(emptyList())
            .build();

        assertThat(actualOthers).isEqualTo(expectedOthers);
    }

    @Test
    void shouldReturnOthersWhenMultipleOthersPresent() {
        final Other other1 = testOther();
        final Other other2 = testOther();

        final List<Element<Other>> othersList = wrapElements(other1, other2);
        final Others actualOthers = Others.from(othersList);
        final Others expectedOthers = Others.builder()
            .firstOther(other1)
            .additionalOthers(List.of(othersList.get(1)))
            .build();

        assertThat(actualOthers).isEqualTo(expectedOthers);
    }

    @Test
    void shouldReturnTrueWhenRepresented() {
        Other other = testOther("First other");
        other.addRepresentative(UUID.randomUUID());

        boolean isRepresented = other.isRepresented();

        assertTrue(isRepresented);
    }

    @Test
    void shouldReturnFalseWhenNotRepresented() {
        Other other = testOther("First other");

        boolean isRepresented = other.isRepresented();

        assertFalse(isRepresented);
    }

    @Test
    void shouldReturnTrueWhenHasAddressAdded() {
        Other other = testOther("First other");

        boolean hasAddressAdded = other.hasAddressAdded();

        assertTrue(hasAddressAdded);
    }

    @Test
    void shouldReturnFalseWhenAddressIsNotPresent() {
        Other other = Other.builder()
            .name("First other")
            .address(Address.builder().build())
            .build();

        boolean hasAddressAdded = other.hasAddressAdded();

        assertFalse(hasAddressAdded);
    }

    @Test
    void shouldReturnTrueWhenFirstOtherExists() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("First other").build())
            .build();

        assertTrue(others.hasOthers());
    }

    @Test
    void shouldReturnTrueWhenFirstOtherIsEmptyAndAdditionalOthersExist() {
        Others others = Others.builder().firstOther(Other.builder().build())
            .additionalOthers(wrapElements(Other.builder().name("some name").build())).build();

        assertTrue(others.hasOthers());
    }

    @Test
    void shouldReturnFalseWhenFirstOtherIsEmpty() {
        Others others = Others.builder().firstOther(Other.builder().build()).build();

        assertFalse(others.hasOthers());
    }

    @Test
    void shouldReturnTrueWhenAdditionalOtherExists() {
        Others others = Others.builder()
            .additionalOthers(wrapElements(Other.builder().name("Additional other").build()))
            .build();

        assertTrue(others.hasOthers());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseWhenFirstOtherAndAdditionalOthersDoNotExist(List<Element<Other>> additionalOthers) {
        Others others = Others.builder().additionalOthers(additionalOthers).build();
        assertFalse(others.hasOthers());
    }

    @Test
    void shouldReturnPartyWithDob() {
        Other testingOther = Other.builder().dateOfBirth("1989-06-04").build();
        assertThat(testingOther.toParty().getDateOfBirth()).isEqualTo(LocalDate.of(1989, Month.JUNE, 4));
    }
}
