package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Data
@Builder
@NoArgsConstructor
@Jacksonized
@Slf4j
@AllArgsConstructor
public class JudicialUserProfile {

    /**
     * This is a set of manually identified titles of PUBLIC LAW judges from the JRD database -
     * until the elinks API is deployed we have no way of identifying their judge titles other than
     * pattern matching, unfortunately.
     */
    @JsonIgnore
    public static final List<String> TITLES = List.of("Baroness",
        "Deputy District Judge",
        "District Judge (MC)",
        "District Judge",
        "Employment Judge",
        "Her Honour Judge",
        "Her Honour",
        "His Honour Judge",
        "His Honour",
        "ICC Judge",
        "Immigration Judge",
        "Recorder",
        "The Hon Mr Justice",
        "The Hon Mrs Justice",
        "The Hon Ms Justice",
        "The Hon Miss Justice",
        "Upper Tribunal Judge",
        "Tribunal Judge",
        "Mrs",
        "Mr",
        "Ms",
        "Miss",
        "Dr",
        "Judge",
        "Dame",
        "Sir"
    );

    @JsonProperty("sidam_id")
    private String sidamId;

    @JsonProperty("known_as")
    private String knownAs;

    // Will be NULL until elinks is deployed
    private String title;

    private String surname;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("post_nominals")
    private String postNominals;

    @JsonProperty("email_id")
    private String emailId;

    private String personalCode;

    public String getTitle() {
        if (isEmpty(this.title) || this.title.equalsIgnoreCase("Unknown")) {
            // if no title from JRD - pattern match it from the known list of titles in JRD full name fields
            // todo - test removal once elinks is deployed in prod.
            for (String title : TITLES) {
                if (fullName.toLowerCase().contains(title.toLowerCase())) {
                    return title;
                }
            }
            log.info("Unknown title for judge, {}", getFullName());
            return "Judge";
        }
        return this.title;
    }

}
