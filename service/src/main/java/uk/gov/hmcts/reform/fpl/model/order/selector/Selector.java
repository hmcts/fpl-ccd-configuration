package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder
public class Selector {
    @Builder.Default
    private String count = "";

    @Builder.Default
    protected List<Integer> selected = new ArrayList<>();

    @Builder.Default
    protected List<Integer> hidden = new ArrayList<>();

    public Selector setNumberOfOptions(Integer max) {
        setCount(IntStream.rangeClosed(1, defaultIfNull(max, 0))
            .mapToObj(Integer::toString)
            .collect(joining()));
        return this;
    }

    public Selector setNumberOfOptions(Integer min, Integer max) {
        setCount(IntStream.rangeClosed(min, defaultIfNull(max, 0))
            .mapToObj(Integer::toString)
            .collect(joining()));
        return this;
    }

    public static Selector newSelector(Integer size) {
        return Selector.builder().build().setNumberOfOptions(size);
    }
}
