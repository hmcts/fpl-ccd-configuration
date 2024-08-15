package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Guardian {
    @Valid
    @NotEmpty(message = "guardianName should not be empty")
    private String guardianName;
    private String telephoneNumber;
    private String email;
    @Valid
    @NotEmpty(message = "children list should not be empty")
    @JsonProperty("children")
    private List<@NotEmpty(message = "child name should not be empty") String> childrenRepresenting;
}
