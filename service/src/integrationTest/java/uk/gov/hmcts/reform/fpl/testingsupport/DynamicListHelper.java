package uk.gov.hmcts.reform.fpl.testingsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;

@Service
public class DynamicListHelper {

    private final ObjectMapper mapper;

    public DynamicListHelper(@Autowired ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @SafeVarargs
    public final Map<String, Object> asMap(Pair<String, ?>... options) {
        return mapper.convertValue(from(options), MAP_TYPE);
    }

    @SafeVarargs
    public final Map<String, Object> asMap(int selected, Pair<String, ?>... options) {
        return mapper.convertValue(from(selected, options), MAP_TYPE);
    }

    @SafeVarargs
    public final DynamicList from(Pair<String, ?>... options) {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(listItems(Arrays.asList(options)))
            .build();
    }

    @SafeVarargs
    public final DynamicList from(int selected, Pair<String, ?>... options) {
        return from(options[selected].getLeft(), options[selected].getRight().toString(), options);
    }

    @SafeVarargs
    private DynamicList from(String label, String code, Pair<String, ?>... options) {
        return DynamicList.builder()
            .value(DynamicListElement.builder().label(label).code(code).build())
            .listItems(listItems(Arrays.asList(options)))
            .build();
    }

    public DynamicList convert(Object object) {
        return mapper.convertValue(object, DynamicList.class);
    }

    private List<DynamicListElement> listItems(List<Pair<String, ?>> options) {
        return options.stream()
            .map(option -> DynamicListElement.builder()
                .label(option.getLeft())
                .code(option.getRight().toString())
                .build())
            .collect(Collectors.toList());
    }
}
