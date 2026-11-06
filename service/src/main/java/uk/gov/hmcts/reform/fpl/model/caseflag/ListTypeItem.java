package uk.gov.hmcts.reform.fpl.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ListTypeItem<T> extends ArrayList<GenericTypeItem<T>> {
    public static <T> ListTypeItem<T> from(GenericTypeItem<T> value) {
        ListTypeItem<T> typeItem = new ListTypeItem<>();
        typeItem.add(value);
        return typeItem;
    }

    public static <T> ListTypeItem<T> from(T value) {
        ListTypeItem<T> typeItem = new ListTypeItem<>();
        typeItem.add(GenericTypeItem.from(value));
        return typeItem;
    }

    public static <T> ListTypeItem<T> from(T value, String id) {
        ListTypeItem<T> typeItem = new ListTypeItem<>();
        typeItem.add(GenericTypeItem.from(id, value));
        return typeItem;
    }

    public static <T> ListTypeItem<T> from(Stream<GenericTypeItem<T>> stream) {
        return stream.collect(Collectors.toCollection(ListTypeItem::new));
    }

    @SafeVarargs
    public static <T> ListTypeItem<T> from(T...values) {
        return Arrays.stream(values)
            .map(GenericTypeItem::from)
            .collect(Collectors.toCollection(ListTypeItem::new));
    }

    @SafeVarargs
    public static <T> ListTypeItem<T> from(GenericTypeItem<T>...values) {
        return Arrays.stream(values).collect(Collectors.toCollection(ListTypeItem::new));
    }

    @SafeVarargs
    public static <T> ListTypeItem<T> concat(ListTypeItem<T>...values) {
        return Stream.of(values)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(ListTypeItem::new));
    }

    /** Convenience method for contains() without the complexity of TypeItem wrapping. */
    public boolean includes(Object item) {
        return super.stream().anyMatch(o -> item.equals(o.getValue()));
    }

    /** Convenience method for converting the object to a GenericTypeItem and adding it. */
    public void addAsItem(T item) {
        super.add(GenericTypeItem.from(item));
    }

    /** Add if the item doesn't already exist in the list. */
    public void addDistinct(T item) {
        if (!includes(item)) {
            addAsItem(item);
        }
    }

    /** Convenience method for streaming findFirst without the complexity of TypeItem wrapping. */
    public Optional<T> findFirst(Predicate<? super T> predicate) {
        var item = this.stream().filter(o -> predicate.test(o.getValue())).findFirst();
        return item.isPresent() ? Optional.of(item.get().getValue()) : Optional.empty();
    }

    /** Convenience method for streaming filter without the complexity of TypeItem wrapping. */
    public Stream<T> filter(Predicate<? super T> predicate) {
        var item = this.stream().filter(o -> predicate.test(o.getValue()));
        return item.map(GenericTypeItem::getValue);
    }

    /** Convenience method for streaming map without the complexity of TypeItem wrapping. */
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return this.stream().map(o -> mapper.apply(o.getValue()));
    }
}
