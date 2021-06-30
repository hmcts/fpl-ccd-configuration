package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;

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
        final Others expectedOthers = Others.builder()
            .firstOther(null)
            .additionalOthers(emptyList())
            .build();

        assertThat(actualOthers).isEqualTo(expectedOthers);
    }

    @Test
    void shouldReturnDefaultOthersWhenListOfOthersIsNull() {
        final List<Element<Other>> othersList = null;
        final Others actualOthers = Others.from(othersList);
        final Others expectedOthers = Others.builder()
            .firstOther(null)
            .additionalOthers(emptyList())
            .build();

        assertThat(actualOthers).isEqualTo(expectedOthers);
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
}
