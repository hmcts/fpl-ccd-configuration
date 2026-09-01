package uk.gov.hmcts.reform.fpl.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class GenericTypeItem<T> {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private T value;

    public static <T> GenericTypeItem<T> from(T value) {
        GenericTypeItem<T> typeItem = new GenericTypeItem<>();
        typeItem.id = UUID.randomUUID().toString();
        typeItem.value = value;
        return typeItem;
    }

    public static <T> GenericTypeItem<T> from(String id, T value) {
        GenericTypeItem<T> typeItem = new GenericTypeItem<>();
        typeItem.id = id;
        typeItem.value = value;
        return typeItem;
    }

    public boolean itemEquals(T value) {
        return value.equals(this.value);
    }
}
