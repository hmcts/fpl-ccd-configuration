package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class OrderQuestionBlockTest {

    @Test
    void shouldSerialiseAllRegisteredShowHideFields() {
        Set<String> serialisedAttributes = new ObjectMapper()
            .convertValue(OrderTempQuestions.builder().build(), new TypeReference<Map<String, Object>>() {
            })
            .keySet();

        Set<String> expectedFields = Arrays.stream(OrderQuestionBlock.values())
            .map(OrderQuestionBlock::getShowHideField)
            .collect(Collectors.toSet());

        assertThat(serialisedAttributes).containsAll(expectedFields);
    }

}
