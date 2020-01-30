package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ElementUtils {

    private ElementUtils() {
    }

    @SafeVarargs
    public static <T> List<Element<T>> wrapElements(T... elements) {
        return Stream.of(elements)
            .map(element -> Element.<T>builder().value(element).build())
            .collect(toList());
    }

    public static <T> List<T> unwrapElements(List<Element<T>> elements) {
        return Optional.ofNullable(elements)
            .orElse(emptyList())
            .stream()
            .map(Element::getValue)
            .collect(toList());
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
}
