package uk.gov.hmcts.reform.fpl.config.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.Map;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedElementValue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class ElementUtilsTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {JacksonAutoConfiguration.class})
    class DynamicListValueCode {
        // The default construction of the object mapper cannot convert dynamic lists due to not being able to find a
        // creator
        //    Cannot construct instance of `uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList` (no Creators,
        //    like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based
        //    Creator)
        // Using the mapper provided by the JacksonAutoConfiguration.class can though
        @Autowired
        private ObjectMapper mapper;

        @Test
        void shouldReturnUuidWhenStringIsPassed() {
            UUID testId = UUID.randomUUID();
            UUID valueCode = getDynamicListSelectedValue(testId.toString(), mapper);

            assertThat(valueCode).isEqualTo(testId);
        }

        @Test
        void shouldReturnUuidWhenMapRepresentationOfDynamicListPassed() {
            UUID selectedID = UUID.randomUUID();

            Map<String, Object> dynamicListAsMap = Map.of(
                "value", Map.of(
                    "code", selectedID.toString(),
                    "label", "selected label"
                ),
                "list_items", List.of(Map.of(
                    "code", selectedID.toString(),
                    "label", "selected label"
                    )
                )
            );

            UUID valueCode = getDynamicListSelectedValue(dynamicListAsMap, mapper);

            assertThat(valueCode).isEqualTo(selectedID);
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {JacksonAutoConfiguration.class})
    class DynamicListSelectedElementValue {
        private ObjectMapper mapper = new ObjectMapper();

        @Test
        void shouldReturnStringWhenUUIDIsPassed() {
            String selectedValue = "code1";
            String valueCode = getDynamicListSelectedElementValue(selectedValue, mapper);

            assertThat(valueCode).isEqualTo(selectedValue);
        }

        @Test
        void shouldReturnUuidWhenMapRepresentationOfDynamicListPassed() {
            String selectedID = "selected-code";

            Map<String, Object> dynamicListAsMap = Map.of(
                "value", Map.of(
                    "code", selectedID,
                    "label", "selected label"
                ),
                "list_items", List.of(Map.of(
                    "code", selectedID,
                    "label", "selected label"
                    )
                )
            );

            String valueCode = getDynamicListSelectedElementValue(dynamicListAsMap, mapper);
            assertThat(valueCode).isEqualTo(selectedID);
        }
    }

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
    class FindElements {

        @Test
        void shouldFindAllElements() {
            Element<String> element1 = Element.<String>builder().id(randomUUID()).value("A").build();
            Element<String> element2 = Element.<String>builder().id(randomUUID()).value("B").build();
            Element<String> element3 = Element.<String>builder().id(randomUUID()).value("B").build();
            Element<String> element4 = Element.<String>builder().id(randomUUID()).value("C").build();
            List<Element<String>> elements = List.of(element1, element2, element3, element4);

            assertThat(findElements("B", elements)).containsExactlyInAnyOrder(element2, element3);
        }

        @Test
        void shouldFindAllNullElements() {
            Element<String> element1 = Element.<String>builder().id(UUID.randomUUID()).build();
            Element<String> element2 = Element.<String>builder().id(UUID.randomUUID()).value("A").build();
            Element<String> element3 = Element.<String>builder().id(UUID.randomUUID()).build();
            List<Element<String>> elements = List.of(element1, element2, element3);

            assertThat(findElements(null, elements)).containsExactly(element1, element3);
        }

        @Test
        void shouldNotFindNonExistingElement() {
            Element<String> element1 = Element.<String>builder().id(randomUUID()).value("A").build();
            Element<String> element2 = Element.<String>builder().id(randomUUID()).value("B").build();
            List<Element<String>> elements = List.of(element1, element2);

            assertThat(findElements("C", elements)).isEmpty();
        }

        @Test
        void shouldNotFindElementInEmptyList() {
            assertThat(findElements("A", emptyList())).isEmpty();
        }

        @Test
        void shouldNotFindElementInNullList() {
            assertThat(findElements("A", null)).isEmpty();
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
    class WrapListOfElements {

        @Test
        void shouldWrapAllObjectsWithElement() {
            List<String> elements = List.of("First", "Second");
            assertThat(wrapElements(elements)).extracting(Element::getValue).isEqualTo(elements);
        }

        @Test
        void shouldReturnEmptyElementListIfNoObjectsToWrap() {
            assertThat(wrapElements(emptyList())).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenListOfElementsIsNull() {
            List<String> elements = null;
            assertThat(wrapElements(elements)).isEmpty();
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
