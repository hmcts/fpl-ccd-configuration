package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ZERO;

@ExtendWith(MockitoExtension.class)
class CardinalityTest {

    @Test
    void shouldReturnCardinalityZero() {

        assertThat(Cardinality.from(0)).isEqualTo(ZERO);
    }

    @Test
    void shouldReturnCardinalityOne() {

        assertThat(Cardinality.from(1)).isEqualTo(ONE);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 10})
    void shouldReturnCardinalityMany(int amount) {

        assertThat(Cardinality.from(amount)).isEqualTo(MANY);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10})
    void shouldThrowsExceptionWhenGivenNumberIsNegative(int amount) {

        assertThatThrownBy(() -> Cardinality.from(amount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cardinality can be represented by non negative numbers only");
    }

}
