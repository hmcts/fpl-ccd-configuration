package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class ElementUtils {

    private ElementUtils() {
    }

    @SafeVarargs
    public static <T> List<Element<T>> wrapElements(T... elements) {
        return Stream.of(elements)
            .filter(Objects::nonNull)
            .map(element -> Element.<T>builder().value(element).build())
            .collect(toList());
    }

    public static <T> List<Element<T>> wrapElements(List<T> elements) {
        return nullSafeCollection(elements).stream()
            .filter(Objects::nonNull)
            .map(element -> Element.<T>builder().value(element).build())
            .collect(toList());
    }

    public static <T> List<T> unwrapElements(List<Element<T>> elements) {
        return nullSafeCollection(elements)
            .stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(toUnmodifiableList());
    }

    public static <T> Element<T> element(T element) {
        return Element.<T>builder()
            .id(UUID.randomUUID())
            .value(element)
            .build();
    }

    public static <T> Element<T> element(UUID id, T element) {
        return Element.<T>builder()
            .id(id)
            .value(element)
            .build();
    }

    public static <T> Optional<Element<T>> findElement(UUID id, List<Element<T>> elements) {
        return nullSafeCollection(elements).stream()
            .filter(element -> Objects.equals(element.getId(), id))
            .findFirst();
    }

    public static <T> List<Element<T>> findElements(T elementToFind, List<Element<T>> elements) {
        return nullSafeCollection(elements).stream()
            .filter(element -> Objects.equals(element.getValue(), elementToFind))
            .collect(Collectors.toList());
    }

    public static <T> List<UUID> findElementsId(T elementToFind, List<Element<T>> elements) {
        return findElements(elementToFind, elements).stream()
            .map(Element::getId)
            .collect(toList());
    }

    public static <T> DynamicList asDynamicList(List<Element<T>> elements,
                                                UUID selectedId,
                                                Function<T, String> labelProducer) {
        return asDynamicList(emptyList(), elements, selectedId, labelProducer);
    }

    public static <T> DynamicList asDynamicList(List<DynamicListElement> additionalItems, List<Element<T>> elements,
                                                UUID selectedId,
                                                Function<T, String> labelProducer) {
        Objects.requireNonNull(labelProducer, "Label producer is required to convert elements to dynamic lists");

        List<DynamicListElement> items = nullSafeCollection(elements).stream()
            .filter(Objects::nonNull)
            .filter(element -> Objects.nonNull(element.getId()))
            .map(element -> DynamicListElement.builder()
                .code(element.getId())
                .label(labelProducer.apply(element.getValue()))
                .build())
            .collect(toList());

        items.addAll(0, additionalItems);

        DynamicListElement selectedItem = items.stream()
            .filter(element -> element.hasCode(selectedId))
            .findFirst()
            .orElse(DynamicListElement.EMPTY);

        return DynamicList.builder().listItems(items).value(selectedItem).build();
    }

    public static <T> DynamicList asDynamicList(List<Element<T>> elements, Function<T, String> labelProducer) {
        return asDynamicList(elements, null, labelProducer);
    }

    private static <T> Collection<T> nullSafeCollection(Collection<T> collection) {
        return defaultIfNull(collection, emptyList());
    }

    public static <T> List<T> nullSafeList(List<T> collection) {
        return defaultIfNull(collection, emptyList());
    }

    public static UUID getDynamicListSelectedValue(Object dynamicList, ObjectMapper mapper) {
        if (dynamicList instanceof String) {
            return UUID.fromString((String) dynamicList);
        }

        return ofNullable(mapper.convertValue(dynamicList, DynamicList.class))
            .map(DynamicList::getValueCodeAsUUID)
            .orElse(null);
    }

    public static String getDynamicListSelectedElementValue(Object dynamicList, ObjectMapper mapper) {
        if (dynamicList instanceof String) {
            return ((String) dynamicList);
        }

        return ofNullable(mapper.convertValue(dynamicList, DynamicList.class))
            .map(DynamicList::getValueCode)
            .orElse(null);
    }

}
