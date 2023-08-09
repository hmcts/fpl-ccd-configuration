package uk.gov.hmcts.reform.fpl.service.representative.diff;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Child;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class ChildRepresentativeDiffCalculatorTest {

    private final Child child1 = mock(Child.class);
    private final Child child2 = mock(Child.class);

    private final ChildRepresentativeDiffCalculator underTest = new ChildRepresentativeDiffCalculator();

    @Test
    void getRegisteredDiffNewElement() {
        when(child1.hasRegisteredOrganisation()).thenReturn(true);
        when(child2.hasRegisteredOrganisation()).thenReturn(true);

        List<Child> diff = underTest.getRegisteredDiff(wrapElements(child1, child2), wrapElements(child2));

        assertThat(diff).isEqualTo(List.of(child1));
    }

    @Test
    void getRegisteredDiffNowRegistered() {
        when(child1.hasRegisteredOrganisation()).thenReturn(true, false);

        List<Child> diff = underTest.getRegisteredDiff(wrapElements(child1), wrapElements(child1));

        assertThat(diff).isEqualTo(List.of(child1));
    }

    @Test
    void getRegisteredDiffNoChange() {
        when(child1.hasRegisteredOrganisation()).thenReturn(true, true);

        List<Child> diff = underTest.getRegisteredDiff(wrapElements(child1), wrapElements(child1));

        assertThat(diff).isEmpty();
    }

    @Test
    void getUnregisteredDiffNewElement() {
        when(child1.hasUnregisteredOrganisation()).thenReturn(true);
        when(child2.hasUnregisteredOrganisation()).thenReturn(true);

        List<Child> diff = underTest.getUnregisteredDiff(wrapElements(child1, child2), wrapElements(child2));

        assertThat(diff).isEqualTo(List.of(child1));
    }

    @Test
    void getUnregisteredDiffNowRegistered() {
        when(child1.hasUnregisteredOrganisation()).thenReturn(true, false);

        List<Child> diff = underTest.getUnregisteredDiff(wrapElements(child1), wrapElements(child1));

        assertThat(diff).isEqualTo(List.of(child1));
    }

    @Test
    void getUnregisteredDiffNoChange() {
        when(child1.hasUnregisteredOrganisation()).thenReturn(true, true);

        List<Child> diff = underTest.getUnregisteredDiff(wrapElements(child1), wrapElements(child1));

        assertThat(diff).isEmpty();
    }
}
