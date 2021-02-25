package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DynamicListService {

    private final ObjectMapper mapper;

    public Optional<String> getSelectedValue(Object dynamicList) {
        if (dynamicList == null) {
            return Optional.empty();
        }

        if (dynamicList instanceof String) {
            return Optional.of((String) dynamicList);
        }

        return Optional.ofNullable(mapper.convertValue(dynamicList, DynamicList.class))
            .map(DynamicList::getValueCode);
    }

    public <T> DynamicList asDynamicList(List<T> elements,
                                         Function<T, String> codeProducer,
                                         Function<T, String> valueProducer) {
        return asDynamicList(elements, null, codeProducer, valueProducer);
    }

    public <T> DynamicList asDynamicList(List<T> elements,
                                         String selectedCode,
                                         Function<T, String> codeProducer,
                                         Function<T, String> valueProducer) {

        List<DynamicListElement> items = elements.stream()
            .filter(Objects::nonNull)
            .map(element -> DynamicListElement.builder()
                .code(codeProducer.apply(element))
                .label(valueProducer.apply(element))
                .build())
            .collect(toList());

        DynamicListElement selectedItem = items.stream()
            .filter(item -> item.hasCode(selectedCode))
            .findFirst()
            .orElse(DynamicListElement.builder().build());

        return DynamicList.builder()
            .listItems(items)
            .value(selectedItem)
            .build();
    }

    public <T> DynamicList asDynamicList(List<T> elements,
                                         int selected,
                                         Function<T, String> codeProducer,
                                         Function<T, String> valueProducer) {

        return asDynamicList(elements, codeProducer.apply(elements.get(selected)), codeProducer, valueProducer);
    }

}
