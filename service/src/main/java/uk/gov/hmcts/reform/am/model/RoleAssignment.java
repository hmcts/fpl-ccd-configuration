package uk.gov.hmcts.reform.am.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class RoleAssignment {

    private String id;

    @Builder.Default
    private String actorIdType = "IDAM";

    private Map<String, Object> attributes;

    private List<String> authorisations;

    private List<String> notes;

    private ZonedDateTime beginTime;

    private ZonedDateTime endTime;

    private ZonedDateTime created;

    @Builder.Default
    private String status = "CREATE_REQUESTED";

    @Builder.Default
    private String classification = "PUBLIC";

    private String actorId;
    private GrantType grantType;
    private RoleCategory roleCategory;
    private String roleName;
    private RoleType roleType;
    private boolean readOnly;

}
