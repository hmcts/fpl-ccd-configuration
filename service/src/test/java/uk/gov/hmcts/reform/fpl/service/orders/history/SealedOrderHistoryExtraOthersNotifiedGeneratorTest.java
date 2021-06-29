package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class SealedOrderHistoryExtraOthersNotifiedGeneratorTest {

    private static final String OTHER_NAME_1 = "Other Name1";
    private static final String OTHER_NAME_2 = "Other Name2";
    private final OthersService othersService = mock(OthersService.class);

    private final SealedOrderHistoryExtraOthersNotifiedGenerator underTest =
        new SealedOrderHistoryExtraOthersNotifiedGenerator(
        othersService);

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
        Other other = Other.builder().name(OTHER_NAME_1).build();
        when(othersService.isRepresented(other)).thenReturn(true);
        String actual = underTest.getOthersNotified(wrapElements(other));

        assertThat(actual).isEqualTo(OTHER_NAME_1);
    }

    @Test
    void testIfHasAddressAdded() {
        Other other = Other.builder().name(OTHER_NAME_1).build();
        when(othersService.isRepresented(other)).thenReturn(false);
        when(othersService.hasAddressAdded(other)).thenReturn(true);
        String actual = underTest.getOthersNotified(wrapElements(other));

        assertThat(actual).isEqualTo(OTHER_NAME_1);
    }

    @Test
    void testIfMultipleHasAddressAdded() {
        Other other = Other.builder().name(OTHER_NAME_1).build();
        Other anotherOther = Other.builder().name(OTHER_NAME_2).build();
        when(othersService.isRepresented(other)).thenReturn(true);
        when(othersService.isRepresented(anotherOther)).thenReturn(true);
        String actual = underTest.getOthersNotified(wrapElements(other, anotherOther));

        assertThat(actual).isEqualTo(OTHER_NAME_1 + ", " + OTHER_NAME_2);
    }
}
