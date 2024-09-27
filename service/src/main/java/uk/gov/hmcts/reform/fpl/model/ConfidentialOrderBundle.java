package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public interface ConfidentialOrderBundle<T> {
    String SUFFIX_CTSC = "CTSC";
    String SUFFIX_LA = "LA";
    String SUFFIX_RESPONDENT = "Resp";
    String SUFFIX_CHILD = "Child";


    @JsonIgnore
    String getFieldBaseName();

    @JsonIgnore
    default String getGetterBaseName() {
        return "get" + StringUtils.capitalize(getFieldBaseName());
    }

    @JsonIgnore
    default String getSetterBaseName() {
        return "set" + StringUtils.capitalize(getFieldBaseName());
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    default List<Element<T>> getConfidentialOrdersBySuffix(String suffix) {
        final String fieldName = getGetterBaseName() + suffix;
        return Arrays.stream(this.getClass().getMethods())
            .filter(method -> method.getName().equals(fieldName))
            .map(method -> {
                try {
                    return method.invoke(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .filter(Objects::nonNull)
            .map(field -> (List<Element<T>>) field)
            .findFirst().orElse(null);
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    default void setConfidentialOrdersBySuffix(String suffix, List<Element<T>> orderCollection) {
        final String setterName = getSetterBaseName() + suffix;
        try {
            this.getClass().getMethod(setterName, List.class).invoke(this, orderCollection);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    default void processAllConfidentialOrders(BiConsumer<String, List<Element<T>>> consumer) {
        final String getterBaseName = getGetterBaseName();
        Arrays.stream(this.getClass().getMethods())
            .filter(method -> method.getName().contains(getterBaseName) && !method.getName().equals(getterBaseName))
            .forEach(method -> {
                try {
                    List<Element<T>> bundles = (List<Element<T>>) method.invoke(this);
                    String suffix = method.getName().replace(getterBaseName, "");
                    consumer.accept(suffix, bundles);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    default List<List<Element<T>>> getListOfOrders() {
        final String getterBaseName = getGetterBaseName();
        List<List<Element<T>>> ret = new ArrayList<>();
        Arrays.stream(this.getClass().getMethods())
            .filter(method -> method.getName().contains(getterBaseName) && method.getParameters().length == 0)
            .map(method -> {
                try {
                    return method.invoke(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .filter(Objects::nonNull)
            .map(field -> (List<Element<T>>) field)
            .forEach(r -> ret.add(r));

        return ret;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    default List<Element<T>> getAllConfidentialOrders() {
        final String getterBaseName = getGetterBaseName();
        Set<UUID> orderIdAdded = new HashSet<>();
        List<Element<T>> confidentialOrders = new ArrayList<>();
        Arrays.stream(this.getClass().getMethods())
            .filter(method -> method.getName().contains(getterBaseName) && !method.getName().equals(getterBaseName)
                && method.getParameters().length == 0)
            .map(method -> {
                try {
                    return method.invoke(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .filter(Objects::nonNull)
            .map(field -> (List<Element<T>>) field)
            .flatMap(List::stream)
            .forEach(confidentialOrder -> {
                // filter out the duplicated orders uploaded by user with multiple case roles
                if (!orderIdAdded.contains(confidentialOrder.getId())) {
                    confidentialOrders.add(confidentialOrder);
                    orderIdAdded.add(confidentialOrder.getId());
                }
            });

        return confidentialOrders;
    }

    @JsonIgnore
    default List<Element<T>> getAllChildConfidentialOrders() {
        final List<Element<T>> childConfidentialOrders = new ArrayList<>();
        processAllConfidentialOrders((suffix, orders) -> {
            if (orders != null && suffix.contains(SUFFIX_CHILD)) {
                childConfidentialOrders.addAll(orders);
            }
        });

        return childConfidentialOrders;
    }
}
