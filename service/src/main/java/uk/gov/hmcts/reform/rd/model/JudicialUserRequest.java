package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@Jacksonized
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudicialUserRequest {

    @JsonProperty("personal_code")
    private List<String> personalCode;

    @JsonProperty("ccdServiceName")
    private String ccdServiceName;

    public static JudicialUserRequest fromPersonalCode(String personalCode) {
        return JudicialUserRequest.builder()
            .personalCode(List.of(personalCode))
            .build();
    }

}
