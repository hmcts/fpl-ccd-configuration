package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
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
}
