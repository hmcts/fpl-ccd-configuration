package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class ElementUtils {

    private ElementUtils() {
    }

    @SafeVarargs
    public static <T extends Object> List<Element<T>> wrap(T... elements) {
        return Stream.of(elements)
            .map(element -> Element.<T>builder().value(element).build())
            .collect(Collectors.toUnmodifiableList());
    }

    public static <T extends Object> List<T> unwrap(List<Element<T>> elements) {
        return Optional.ofNullable(elements)
            .orElse(emptyList())
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toUnmodifiableList());
    }

    public static <T> Element<T> element(T element) {
        return Element.<T>builder().id(UUID.randomUUID()).value(element).build();
    }


}
