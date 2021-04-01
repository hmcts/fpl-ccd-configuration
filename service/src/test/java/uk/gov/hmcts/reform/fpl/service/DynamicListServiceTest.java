package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DynamicListServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicListService dynamicListService = new DynamicListService(objectMapper);

    @Test
    void shouldReturnEmptyValueIfDynamicListIsNull() {
        assertThat(dynamicListService.getSelectedValue(null)).isEmpty();
    }

    @Test
    void shouldReturnValueIfDynamicListIsString() {
        assertThat(dynamicListService.getSelectedValue("test")).contains("test");
    }

    @Test
    void shouldGenerateDynamicList() {
        List<Pair<String, String>> pairs = List.of(Pair.of("1", "one"), Pair.of("2", "two"));

        DynamicList dynamicList = dynamicListService.asDynamicList(pairs, Pair::getKey, Pair::getValue);

        assertThat(dynamicList.getValue()).isEqualTo(DynamicListElement.EMPTY);
        assertThat(dynamicList.getListItems()).containsExactly(
            DynamicListElement.builder().label("one").code("1").build(),
            DynamicListElement.builder().label("two").code("2").build());
    }

    @Test
    void shouldGenerateDynamicListSkippingNullElements() {
        List<Pair<String, String>> pairs = newArrayList(Pair.of("1", "one"), null, Pair.of("2", "two"));

        DynamicList dynamicList = dynamicListService.asDynamicList(pairs, Pair::getKey, Pair::getValue);

        assertThat(dynamicList.getValue()).isEqualTo(DynamicListElement.EMPTY);
        assertThat(dynamicList.getListItems()).containsExactly(
            DynamicListElement.builder().label("one").code("1").build(),
            DynamicListElement.builder().label("two").code("2").build());
    }

    @Test
    void shouldGenerateEmptyDynamicList() {
        List<Pair<String, String>> pairs = List.of();

        DynamicList dynamicList = dynamicListService.asDynamicList(pairs, Pair::getKey, Pair::getValue);

        assertThat(dynamicList.getValue()).isEqualTo(DynamicListElement.EMPTY);
        assertThat(dynamicList.getListItems()).isEmpty();
    }
}
