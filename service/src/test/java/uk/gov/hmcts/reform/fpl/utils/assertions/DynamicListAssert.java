package uk.gov.hmcts.reform.fpl.utils.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstanceOfAssertFactory;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class DynamicListAssert extends AbstractAssert<DynamicListAssert, DynamicList> {

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
        int actualSize = actual.getListItems().size();
        if (actualSize != expectedSize) {
            failWithMessage("Expected dynamic list to have %s elements, but it only had %s", expectedSize, actualSize);
        }

        return this;
    }

    public DynamicListAssert hasElement(UUID expectedCode, String expectedLabel) {
        isNotNull();

        DynamicListElement expectedElement = DynamicListElement.builder()
            .code(expectedCode)
            .label(expectedLabel)
            .build();

        List<DynamicListElement> dynamicListElementList = actual.getListItems();
        Stream<DynamicListElement> stream = dynamicListElementList.stream();
        if (stream.noneMatch(e -> e.equals(expectedElement))) {
            failWithMessage("Expected to find element %s in list, but couldn't. List is %s",
                expectedElement,
                dynamicListElementList.toString());
        }

        return this;
    }

}
