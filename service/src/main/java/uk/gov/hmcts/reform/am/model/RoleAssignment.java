package uk.gov.hmcts.reform.am.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAssignment {

    private String id;

    private String actorIdType;

    private Map<String, Object> attributes;

    private List<String> authorisations;

    private List<String> notes;

    private ZonedDateTime beginTime;

    private ZonedDateTime endTime;

    private ZonedDateTime created;

    private String status;

    private String classification;

    private String actorId;
    private GrantType grantType;
    private RoleCategory roleCategory;
    private String roleName;
    private RoleType roleType;
    private boolean readOnly;

}
