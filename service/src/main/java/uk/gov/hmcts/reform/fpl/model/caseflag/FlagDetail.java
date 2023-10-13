package uk.gov.hmcts.reform.fpl.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FlagDetail {
    @JsonProperty("name")
    private String name;
    @JsonProperty("subTypeValue")
    private String subTypeValue;
    @JsonProperty("subTypeKey")
    private String subTypeKey;
    @JsonProperty("otherDescription")
    private String otherDescription;
    @JsonProperty("flagComment")
    private String flagComment;
    @JsonProperty("dateTimeModified")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateTimeModified;
    @JsonProperty("dateTimeCreated")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateTimeCreated;
    @JsonProperty("path")
    private List<PathValue> path;
    @JsonProperty("hearingRelevant")
    private String hearingRelevant;
    @JsonProperty("flagCode")
    private String flagCode;
    @JsonProperty("status")
    private String status;
}
