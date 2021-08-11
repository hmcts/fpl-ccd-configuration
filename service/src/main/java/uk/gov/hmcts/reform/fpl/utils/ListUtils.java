package uk.gov.hmcts.reform.fpl.utils;

import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class ListUtils {//TODO - potentially undo this change

    private ListUtils() {
    }

    public static <T> Stream<T> getAddedItems(List<T> currentItems, List<T> previousItems) {
        return currentItems.stream().filter(not(previousItems::contains));
    }

    public static <T> Stream<T> getRemovedItems(List<T> currentItems, List<T> previousItems) {
        return previousItems.stream().filter(not(currentItems::contains));
    }

}
