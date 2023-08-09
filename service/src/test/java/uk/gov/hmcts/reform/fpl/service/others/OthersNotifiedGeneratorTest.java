package uk.gov.hmcts.reform.fpl.service.others;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.service.others.OthersNotifiedGenerator;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

class OthersNotifiedGeneratorTest {

    private static final String OTHER_NAME_1 = "Other Name1";
    private static final String OTHER_NAME_2 = "Other Name2";

    private final OthersNotifiedGenerator underTest = new OthersNotifiedGenerator();

    @Test
    void testIfNull() {
        String actual = underTest.getOthersNotified(null);

        assertThat(actual).isNull();
    }

    @Test
    void testIfEmpty() {
        String actual = underTest.getOthersNotified(Collections.emptyList());

        assertThat(actual).isEmpty();
    }

    @Test
    void testIfRepresented() {
        Other other = Other.builder()
            .name(OTHER_NAME_1).build();
        other.addRepresentative(UUID.randomUUID());

        String actual = underTest.getOthersNotified(wrapElements(other));
        assertThat(actual).isEqualTo(OTHER_NAME_1);
    }

    @Test
    void testIfHasAddressAdded() {
        Other other = Other.builder().name(OTHER_NAME_1)
            .address(testAddress())
            .build();
        String actual = underTest.getOthersNotified(wrapElements(other));

        assertThat(actual).isEqualTo(OTHER_NAME_1);
    }

    @Test
    void testIfMultipleHasAddressAdded() {
        Other other = Other.builder()
            .name(OTHER_NAME_1)
            .address(testAddress())
            .build();

        Other anotherOther = Other.builder()
            .name(OTHER_NAME_2)
            .address(testAddress())
            .build();

        String actual = underTest.getOthersNotified(wrapElements(other, anotherOther));
        assertThat(actual).isEqualTo(OTHER_NAME_1 + ", " + OTHER_NAME_2);
    }
}
