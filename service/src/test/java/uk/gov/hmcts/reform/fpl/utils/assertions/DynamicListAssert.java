package uk.gov.hmcts.reform.fpl.utils.assertions;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.WritableAssertionInfo;
import org.assertj.core.internal.Iterables;
import org.assertj.core.internal.StandardComparisonStrategy;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.Arrays;
import java.util.UUID;

public class DynamicListAssert extends AbstractAssert<DynamicListAssert, DynamicList> {

    private final WritableAssertionInfo info = new WritableAssertionInfo(null);
    private final Iterables iterables = new Iterables(StandardComparisonStrategy.instance());

    public DynamicListAssert(DynamicList actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public static DynamicListAssert assertThat(DynamicList actual) {
        return new DynamicListAssert(actual, DynamicListAssert.class);
    }

    public static InstanceOfAssertFactory<DynamicList, DynamicListAssert> getInstanceOfAssertFactory() {
        return new InstanceOfAssertFactory<>(DynamicList.class, DynamicListAssert::assertThat);
    }

    public DynamicListAssert hasSize(int expectedSize) {
        isNotNull();
        iterables.assertHasSize(info, actual.getListItems(), expectedSize);
        return this;
    }

    public DynamicListAssert hasElement(UUID expectedCode, String expectedLabel) {
        return hasElements(Pair.of(expectedCode, expectedLabel));
    }

    public DynamicListAssert hasNoSelectedValue() {
        return hasSelectedValue(null, null);
    }

    public DynamicListAssert hasSelectedValue(UUID expectedCode, String expectedLabel) {
        isNotNull();

        DynamicListElement expected = DynamicListElement.builder()
            .code(expectedCode)
            .label(expectedLabel)
            .build();

        DynamicListElement actualValue = actual.getValue();

        objects.assertEqual(info, actualValue, expected);

        return this;
    }

    @SafeVarargs
    public final DynamicListAssert hasElements(Pair<UUID, String>... elements) {
        isNotNull();

        DynamicListElement[] dynamicListElements = processPairs(elements);

        iterables.assertContains(info, actual.getListItems(), dynamicListElements);

        return this;
    }

    @SafeVarargs
    public final DynamicListAssert hasElementsInOrder(Pair<UUID, String>... elements) {
        isNotNull();

        DynamicListElement[] dynamicListElements = processPairs(elements);

        iterables.assertContainsExactly(info, actual.getListItems(), dynamicListElements);

        return this;
    }

    private DynamicListElement[] processPairs(Pair<UUID, String>[] elements) {
        return Arrays.stream(elements)
            .map(e -> DynamicListElement.builder().code(e.getKey()).label(e.getValue()).build())
            .toArray(DynamicListElement[]::new);
    }
}
