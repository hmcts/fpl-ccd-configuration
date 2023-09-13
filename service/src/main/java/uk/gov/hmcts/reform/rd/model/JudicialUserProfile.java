package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@Jacksonized
@AllArgsConstructor
public class JudicialUserProfile {

    @JsonProperty("sidam_id")
    private String sidamId;

    @JsonProperty("known_as")
    private String knownAs;

    private String surname;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("post_nominals")
    private String postNominals;

    @JsonProperty("email_id")
    private String emailId;

    private String personalCode;

}
