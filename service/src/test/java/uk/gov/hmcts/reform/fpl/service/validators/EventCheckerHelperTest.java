package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.allNonEmpty;

class EventCheckerHelperTest {

    @Nested
    class AllNonEmpty {

        @Test
        void testEmptyArguments() {
            assertThat(allNonEmpty()).isTrue();
        }

        @Test
        void testOneEmpty() {
            assertThat(allNonEmpty("")).isFalse();
        }

        @Test
        void testOneNull() {
            assertThat(allNonEmpty(new Object[] {null})).isFalse();
        }

        @Test
        void testMultipleAndOneEmpty() {
            assertThat(allNonEmpty("", "x")).isFalse();
        }

        @Test
        void testNonEmpty() {
            assertThat(allNonEmpty("x")).isTrue();
        }

    }

}
