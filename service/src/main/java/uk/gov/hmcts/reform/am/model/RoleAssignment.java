package uk.gov.hmcts.reform.am.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class RoleAssignment {

    @Builder.Default
    private String actorIdType = "IDAM";

    @Builder.Default
    private Map<String, Object> attributes = Map.of();

    @Builder.Default
    private List<String> authorisations = List.of();

    @Builder.Default
    private List<String> notes = List.of();

    @Builder.Default
    private ZonedDateTime beginTime = ZonedDateTime.now();

    @Builder.Default
    private ZonedDateTime endTime = ZonedDateTime.now().plusYears(10);

    @Builder.Default
    private ZonedDateTime created = ZonedDateTime.now();

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
