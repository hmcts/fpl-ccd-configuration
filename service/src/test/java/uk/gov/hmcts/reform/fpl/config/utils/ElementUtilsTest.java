package uk.gov.hmcts.reform.fpl.config.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class ElementUtilsTest {

    @Nested
    class AsDynamicList {
        Element<String> element1 = Element.<String>builder().id(randomUUID()).value("First").build();
        Element<String> element2 = Element.<String>builder().id(randomUUID()).value("Second").build();

        Function<String, String> labelProducer = String::toUpperCase;

        DynamicListElement option1 = DynamicListElement.builder()
            .code(element1.getId())
            .label("FIRST")
            .build();

        DynamicListElement option2 = DynamicListElement.builder()
            .code(element2.getId())
            .label("SECOND")
            .build();

        @Test
        void shouldReturnDynamicListOfAllElements() {
            DynamicList elementsList = asDynamicList(List.of(element1, element2), null, labelProducer);

            DynamicList expectedElementsList = DynamicList.builder()
                .listItems(List.of(option1, option2))
                .value(DynamicListElement.EMPTY)
                .build();

            assertThat(elementsList).isEqualTo(expectedElementsList);
        }

        @Test
        void shouldReturnDynamicListExcludingNullElements() {
            DynamicList elementsList = asDynamicList(asList(element1, null), null, labelProducer);

            DynamicList expectedElementsList = DynamicList.builder()
                .listItems(List.of(option1))
                .value(DynamicListElement.EMPTY)
                .build();

            assertThat(elementsList).isEqualTo(expectedElementsList);
        }

        @Test
        void shouldReturnDynamicListExcludingElementsWithoutId() {
            Element<String> elementWithoutId = Element.<String>builder().value("Test").build();
            DynamicList elementsList = asDynamicList(asList(element1, elementWithoutId), null, labelProducer);

            DynamicList expectedElementsList = DynamicList.builder()
                .listItems(List.of(option1))
                .value(DynamicListElement.EMPTY)
                .build();

            assertThat(elementsList).isEqualTo(expectedElementsList);
        }

        @Test
        void shouldReturnPreselectedDynamicListOfAllElements() {
            UUID selectedElementId = element2.getId();

            DynamicList elementsList = asDynamicList(List.of(element1, element2), selectedElementId, 
                labelProducer);

            DynamicList expectedElementsList = DynamicList.builder()
                .listItems(List.of(option1, option2))
                .value(option2)
                .build();

            assertThat(elementsList).isEqualTo(expectedElementsList);
        }

        @Test
        void shouldReturnUnselectedListWhenSelectedElementNotFound() {
            UUID selectedElementId = randomUUID();

            DynamicList elementsList = asDynamicList(List.of(element1, element2), selectedElementId, labelProducer);

            DynamicList expectedElementsList = DynamicList.builder()
                .listItems(List.of(option1, option2))
                .value(DynamicListElement.EMPTY)
                .build();

            assertThat(elementsList).isEqualTo(expectedElementsList);
        }

        @Test
        void shouldReturnEmptyDynamicListWhenNoElements() {
            UUID selectedElementId = randomUUID();

            DynamicList elementsList = asDynamicList(emptyList(), selectedElementId, labelProducer);

            DynamicList expectedElementsList = DynamicList.builder()
                .listItems(emptyList())
                .value(DynamicListElement.EMPTY)
                .build();

            assertThat(elementsList).isEqualTo(expectedElementsList);
        }

        @Test
        void shouldReturnEmptyDynamicListWhenNullElements() {
            UUID selectedElementId = randomUUID();

            DynamicList elementsList = asDynamicList(null, selectedElementId, labelProducer);

            DynamicList expectedElementsList = DynamicList.builder()
                .listItems(emptyList())
                .value(DynamicListElement.EMPTY)
                .build();

            assertThat(elementsList).isEqualTo(expectedElementsList);
        }

        @Test
        void shouldThrowsExceptionWhenLabelProducerIsNull() {
            Exception exception = assertThrows(
                NullPointerException.class, () -> asDynamicList(null, null, null));

            assertThat(exception).hasMessage("Label producer is required to convert elements to dynamic lists");
        }
    }

    @Nested
    class FindElement {

        @Test
        void shouldFindElementById() {
            Element<String> element1 = Element.<String>builder().id(randomUUID()).value("First").build();
            Element<String> element2 = Element.<String>builder().id(randomUUID()).value("Second").build();
            List<Element<String>> elements = List.of(element1, element2);

            assertThat(findElement(element1.getId(), elements)).isEqualTo(Optional.of(element1));
            assertThat(findElement(element2.getId(), elements)).isEqualTo(Optional.of(element2));
        }

        @Test
        void shouldNotFindNonExistingElement() {
            Element<String> element1 = Element.<String>builder().id(randomUUID()).value("First").build();
            Element<String> element2 = Element.<String>builder().id(randomUUID()).value("Second").build();
            List<Element<String>> elements = List.of(element1, element2);

            assertThat(findElement(randomUUID(), elements)).isNotPresent();
        }

        @Test
        void shouldNotFindElementInEmptyList() {
            assertThat(findElement(randomUUID(), emptyList())).isNotPresent();
        }

        @Test
        void shouldNotFindElementInNullList() {
            assertThat(findElement(randomUUID(), null)).isNotPresent();
        }

        @Test
        void shouldNotFindNullElement() {
            assertThat(findElement(null, null)).isNotPresent();
        }
    }

    @Nested
    class WrapElements {

        Element<String> element1 = Element.<String>builder().value("First").build();
        Element<String> element2 = Element.<String>builder().value("Second").build();

        @Test
        void shouldWrapAllObjectsWithElement() {
            assertThat(wrapElements("First", "Second")).containsExactly(element1, element2);
        }

        @Test
        void shouldReturnEmptyElementListIfNoObjectsToWrap() {
            assertThat(wrapElements()).isEmpty();
        }

        @Test
        void shouldWrapNonNullObjectsWithElement() {
            assertThat(wrapElements("First", null)).containsExactly(element1);
        }
    }

    @Nested
    class UnwrapElements {

        Element<String> element1 = Element.<String>builder().id(randomUUID()).value("First").build();
        Element<String> element2 = Element.<String>builder().id(randomUUID()).value("Second").build();
        Element<String> elementWithoutValue = Element.<String>builder().id(randomUUID()).build();

        @Test
        void shouldUnwrapAllElements() {
            assertThat(unwrapElements(List.of(element1, element2))).containsExactly("First", "Second");
        }

        @Test
        void shouldExcludeElementsWithNullValues() {
            assertThat(unwrapElements(List.of(element1, elementWithoutValue))).containsExactly("First");
        }

        @Test
        void shouldReturnEmptyListIfListOfElementIsEmpty() {
            assertThat(unwrapElements(emptyList())).isEmpty();
        }

        @Test
        void shouldReturnEmptyListIfListOfElementIsNull() {
            assertThat(unwrapElements(null)).isEmpty();
        }
    }
}
